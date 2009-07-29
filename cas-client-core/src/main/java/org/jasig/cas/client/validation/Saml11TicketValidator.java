/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.opensaml.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * TicketValidator that can understand validating a SAML artifact.  This includes the SOAP request/response.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class Saml11TicketValidator extends AbstractUrlBasedTicketValidator {

    /** Time tolerance to allow for time drifting. */
    private long tolerance = 1000L;

    public Saml11TicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    protected String getUrlSuffix() {
        return "samlValidate";
    }

    protected void populateUrlAttributeMap(final Map urlParameters) {
        final String service = (String) urlParameters.get("service");
        urlParameters.remove("service");
        urlParameters.remove("ticket");
        urlParameters.put("TARGET", service);
    }

    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        try {
        	final String removeStartOfSoapBody = response.substring(response.indexOf("<SOAP-ENV:Body>") + 15);
        	final String removeEndOfSoapBody = removeStartOfSoapBody.substring(0, removeStartOfSoapBody.indexOf("</SOAP-ENV:Body>"));
            final SAMLResponse samlResponse = new SAMLResponse(new ByteArrayInputStream(removeEndOfSoapBody.getBytes()));

            if (!samlResponse.getAssertions().hasNext()) {
                throw new TicketValidationException("No assertions found.");
            }

            for (final Iterator iter = samlResponse.getAssertions(); iter.hasNext();) {
                final SAMLAssertion assertion = (SAMLAssertion) iter.next();

                if (!isValidAssertion(assertion)) {
                    continue;
                }

                final SAMLAuthenticationStatement authenticationStatement = getSAMLAuthenticationStatement(assertion);

                if (authenticationStatement == null) {
                    throw new TicketValidationException("No AuthentiationStatement found in SAML Assertion.");
                }
                final SAMLSubject subject = authenticationStatement.getSubject();

                if (subject == null) {
                    throw new TicketValidationException("No Subject found in SAML Assertion.");
                }

                final SAMLAttribute[] attributes = getAttributesFor(assertion, subject);

                final Map personAttributes = new HashMap();

                for (int i = 0; i < attributes.length; i++) {
                    final SAMLAttribute samlAttribute = attributes[i];
                    final List values = getValuesFrom(samlAttribute);

                    personAttributes.put(samlAttribute.getName(), values.size() == 1 ? values.get(0) : values);
                }

                final AttributePrincipal principal = new AttributePrincipalImpl(subject.getNameIdentifier().getName(), personAttributes);


                final Map authenticationAttributes = new HashMap();
                authenticationAttributes.put("samlAuthenticationStatement::authMethod", authenticationStatement.getAuthMethod());

                return new AssertionImpl(principal, authenticationAttributes);
            }
       } catch (final SAMLException e) {
            throw new TicketValidationException(e);
        }

        throw new TicketValidationException("No valid assertions from the SAML response found.");
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
                // used because SAMLSubject does not implement equals
                if (subject.getNameIdentifier().getName().equals(attributeStatement.getSubject().getNameIdentifier().getName())) {
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

    private static String getFormattedDateAndTime(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateFormat.format(date);
    }


    protected String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        final String MESSAGE_TO_SEND = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"  MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"" + UUID.randomUUID().toString() + "\" IssueInstant=\"" + getFormattedDateAndTime(new Date()) + "\">"
                + "<samlp:AssertionArtifact>" + ticket
                + "</samlp:AssertionArtifact></samlp:Request></SOAP-ENV:Body></SOAP-ENV:Envelope>";

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) validationUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml"); 
            conn.setRequestProperty("Content-Length", Integer.toString(MESSAGE_TO_SEND.length()));
            conn.setRequestProperty("SOAPAction", "http://www.oasis-open.org/committees/security");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            final DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(MESSAGE_TO_SEND);
            out.flush();
            out.close();

            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer buffer = new StringBuffer(256);

            synchronized (buffer) {
                String line;

                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);       
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void setTolerance(final long tolerance) {
        this.tolerance = tolerance;
    }
}
