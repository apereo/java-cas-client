package org.jasig.cas.client.validation;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.AttributePrincipal;
import org.jasig.cas.authentication.principal.SimpleAttributePrincipal;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLAttributeStatement;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml10TicketValidator extends AbstractUrlBasedTicketValidator {

    /**
     * Number of milliseconds the client/server clocks can be off by.
     */
    private final long tolerance;

    public Saml10TicketValidator(final String casServerUrl, final HttpClient httpClient) {
       super(casServerUrl, false, httpClient);
        this.tolerance = 1000L;
    }

    public Saml10TicketValidator(final String casServerUrl, final HttpClient httpClient, final long tolerance) {
       super(casServerUrl, false, httpClient);
        this.tolerance = tolerance;
    }


    protected String constructURL(final String ticketId, final Service service) {
        return getCasServerUrl() + "/samlValidate?SAMLart=" + ticketId + "&TARGET=" + getEncodedService(service);
    }

    protected Assertion parseResponse(final String response) throws ValidationException {
        try {
            log.debug(response);
            final SAMLResponse samlResponse = new SAMLResponse(new ByteArrayInputStream(response.getBytes()));

            // check to see if we have any assertions
            if (!samlResponse.getAssertions().hasNext()) {
                throw new ValidationException("No assertions found.");
            }

            for (final Iterator iter = samlResponse.getAssertions(); iter.hasNext();) {
                final SAMLAssertion assertion = (SAMLAssertion) iter.next();

                if (!isValidAssertion(assertion)) {
                    continue;
                }

                final SAMLAuthenticationStatement authenticationStatement = getSAMLAuthenticationStatement(assertion);

                if (authenticationStatement == null) {
                    throw new ValidationException("No AuthentiationStatement found in SAML Assertion.");
                }
                final SAMLSubject subject = authenticationStatement.getSubject();

                if (subject == null) {
                    throw new ValidationException("No Subject found in SAML Assertion.");
                }

                final SAMLAttribute[] attributes = getAttributesFor(assertion, subject);

                final Map personAttributes = new HashMap();

                for (int i = 0; i < attributes.length; i++) {
                    final SAMLAttribute samlAttribute = attributes[i];
                    final List values = getValuesFrom(samlAttribute);

                    personAttributes.put(samlAttribute.getName(), values.size() == 1 ? values.get(0) : values);
                }

                final AttributePrincipal principal = new SimpleAttributePrincipal(subject.getNameIdentifier().getName(), personAttributes);


                final Map authenticationAttributes = new HashMap();
                authenticationAttributes.put("samlAuthenticationStatement::authMethod", authenticationStatement.getAuthMethod());
                
                final Assertion casAssertion = new AssertionImpl(principal, authenticationAttributes);
                return casAssertion;
            }
       } catch (final SAMLException e) {
            log.error(e,e);
            throw new ValidationException(e);
        }

        throw new ValidationException("No valid assertions from the SAML response found.");
    }

    private boolean isValidAssertion(final SAMLAssertion assertion) {
        final Date notBefore = assertion.getNotBefore();
        final Date notOnOrAfter = assertion.getNotOnOrAfter();

        if (assertion.getNotBefore() == null || assertion.getNotOnOrAfter() == null) {
            log.debug("Assertion has no bounding dates. Will not process.");
            return false;
        }

        final long currentTime = new Date().getTime();

        if (currentTime + tolerance < notBefore.getTime()) {
            log.debug("skipping assertion that's not yet valid...");
            return false;
        }

        if (notOnOrAfter.getTime() <= currentTime - tolerance) {
            log.debug("skipping expired assertion...");
            return false;
        }

        return true;
    }

    private SAMLAuthenticationStatement getSAMLAuthenticationStatement(final SAMLAssertion assertion) {
        for (final Iterator iter = assertion.getStatements(); iter.hasNext();) {
            final SAMLStatement statement = (SAMLStatement) iter.next();

            if (statement instanceof SAMLAuthenticationStatement) {
                return (SAMLAuthenticationStatement) statement;
            }
        }

        return null;
    }

    private SAMLAttribute[] getAttributesFor(final SAMLAssertion assertion, final SAMLSubject subject) {
        final List attributes = new ArrayList();
        for (final Iterator iter = assertion.getStatements(); iter.hasNext();) {
            final SAMLStatement statement = (SAMLStatement) iter.next();

            if (statement instanceof SAMLAttributeStatement) {
                final SAMLAttributeStatement attributeStatement = (SAMLAttributeStatement) statement;

                if (subject.equals(attributeStatement.getSubject())) {
                    for (final Iterator iter2 = attributeStatement.getAttributes(); iter2.hasNext();)
                    attributes.add(iter2.next());
                }
            }
        }

        return (SAMLAttribute[]) attributes.toArray(new SAMLAttribute[attributes.size()]);
    }

    private List getValuesFrom(final SAMLAttribute attribute) {
        final List list = new ArrayList();
        for (final Iterator iter = attribute.getValues(); iter.hasNext();) {
            list.add(iter.next());
        }

        return list;
    }
}
