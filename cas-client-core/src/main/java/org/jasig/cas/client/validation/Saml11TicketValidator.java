/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.validation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.CommonUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml1.core.*;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TicketValidator that can understand validating a SAML artifact.  This includes the SOAP request/response.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class Saml11TicketValidator extends AbstractUrlBasedTicketValidator {

    static {
        try {
            // we really only need to do this once, so this is why its here.
            DefaultBootstrap.bootstrap();
        } catch (final ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /** Time tolerance to allow for time drifting. */
    private long tolerance = 1000L;

    private final BasicParserPool basicParserPool;

    private final IdentifierGenerator identifierGenerator;

    public Saml11TicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        this.basicParserPool = new BasicParserPool();
        this.basicParserPool.setNamespaceAware(true);

        try {
            this.identifierGenerator = new SecureRandomIdentifierGenerator();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getUrlSuffix() {
        return "samlValidate";
    }

    protected void populateUrlAttributeMap(final Map<String, String> urlParameters) {
        final String service = urlParameters.get("service");
        urlParameters.remove("service");
        urlParameters.remove("ticket");
        urlParameters.put("TARGET", service);
    }

    @Override
    protected void setDisableXmlSchemaValidation(final boolean disabled) {
        if (disabled) {
            this.basicParserPool.setSchema(null);
        }
    }

    protected byte[] getBytes(final String text) {
        try {
            return CommonUtils.isNotBlank(getEncoding()) ? text.getBytes(getEncoding()) : text.getBytes();
        } catch (final Exception e) {
            return text.getBytes();
        }
    }

    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        try {

            final Document responseDocument = this.basicParserPool.parse(new ByteArrayInputStream(getBytes(response)));
            final Element responseRoot = responseDocument.getDocumentElement();
            final UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(responseRoot);
            final Envelope envelope = (Envelope) unmarshaller.unmarshall(responseRoot);
            final Response samlResponse = (Response) envelope.getBody().getOrderedChildren().get(0);

            final List<org.opensaml.saml1.core.Assertion> assertions = samlResponse.getAssertions();
            if (assertions.isEmpty()) {
                throw new TicketValidationException("No assertions found.");
            }

            for (final org.opensaml.saml1.core.Assertion assertion : assertions) {

                if (!isValidAssertion(assertion)) {
                    continue;
                }

                final AuthenticationStatement authenticationStatement = getSAMLAuthenticationStatement(assertion);

                if (authenticationStatement == null) {
                    throw new TicketValidationException("No AuthentiationStatement found in SAML Assertion.");
                }
                final Subject subject = authenticationStatement.getSubject();

                if (subject == null) {
                    throw new TicketValidationException("No Subject found in SAML Assertion.");
                }

                final List<Attribute> attributes = getAttributesFor(assertion, subject);
                final Map<String, Object> personAttributes = new HashMap<String, Object>();
                for (final Attribute samlAttribute : attributes) {
                    final List<?> values = getValuesFrom(samlAttribute);

                    personAttributes.put(samlAttribute.getAttributeName(), values.size() == 1 ? values.get(0) : values);
                }

                final AttributePrincipal principal = new AttributePrincipalImpl(subject.getNameIdentifier()
                        .getNameIdentifier(), personAttributes);

                final Map<String, Object> authenticationAttributes = new HashMap<String, Object>();
                authenticationAttributes.put("samlAuthenticationStatement::authMethod",
                        authenticationStatement.getAuthenticationMethod());

                final DateTime notBefore = assertion.getConditions().getNotBefore();
                final DateTime notOnOrAfter = assertion.getConditions().getNotOnOrAfter();
                final DateTime authenticationInstant = authenticationStatement.getAuthenticationInstant();
                return new AssertionImpl(principal, notBefore.toDate(), notOnOrAfter.toDate(),
                        authenticationInstant.toDate(), authenticationAttributes);
            }
        } catch (final UnmarshallingException e) {
            throw new TicketValidationException(e);
        } catch (final XMLParserException e) {
            throw new TicketValidationException(e);
        }

        throw new TicketValidationException(
                "No Assertion found within valid time range.  Either there's a replay of the ticket or there's clock drift. Check tolerance range, or server/client synchronization.");
    }

    private boolean isValidAssertion(final org.opensaml.saml1.core.Assertion assertion) {
        final DateTime notBefore = assertion.getConditions().getNotBefore();
        final DateTime notOnOrAfter = assertion.getConditions().getNotOnOrAfter();

        if (notBefore == null || notOnOrAfter == null) {
            logger.debug("Assertion has no bounding dates. Will not process.");
            return false;
        }

        final DateTime currentTime = new DateTime(DateTimeZone.UTC);
        final Interval validityRange = new Interval(notBefore.minus(this.tolerance), notOnOrAfter.plus(this.tolerance));

        if (validityRange.contains(currentTime)) {
            logger.debug("Current time is within the interval validity.");
            return true;
        }

        if (currentTime.isBefore(validityRange.getStart())) {
            logger.debug("skipping assertion that's not yet valid...");
            return false;
        }

        logger.debug("skipping expired assertion...");
        return false;
    }

    private AuthenticationStatement getSAMLAuthenticationStatement(final org.opensaml.saml1.core.Assertion assertion) {
        final List<AuthenticationStatement> statements = assertion.getAuthenticationStatements();

        if (statements.isEmpty()) {
            return null;
        }

        return statements.get(0);
    }

    private List<Attribute> getAttributesFor(final org.opensaml.saml1.core.Assertion assertion, final Subject subject) {
        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (final AttributeStatement attribute : assertion.getAttributeStatements()) {
            if (subject.getNameIdentifier().getNameIdentifier()
                    .equals(attribute.getSubject().getNameIdentifier().getNameIdentifier())) {
                attributes.addAll(attribute.getAttributes());
            }
        }

        return attributes;
    }

    private List<?> getValuesFrom(final Attribute attribute) {
        final List<Object> list = new ArrayList<Object>();
        for (final Object o : attribute.getAttributeValues()) {
            if (o instanceof XSAny) {
                list.add(((XSAny) o).getTextContent());
            } else if (o instanceof XSString) {
                list.add(((XSString) o).getValue());
            } else {
                list.add(o.toString());
            }
        }
        return list;
    }

    protected String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        final String MESSAGE_TO_SEND = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"  MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\""
                + this.identifierGenerator.generateIdentifier()
                + "\" IssueInstant=\""
                + CommonUtils.formatForUtcTime(new Date())
                + "\">"
                + "<samlp:AssertionArtifact>"
                + ticket
                + "</samlp:AssertionArtifact></samlp:Request></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        HttpURLConnection conn = null;
        DataOutputStream out = null;
        BufferedReader in = null;

        try {
            conn = this.getURLConnectionFactory().buildHttpURLConnection(validationUrl.openConnection());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("Content-Length", Integer.toString(MESSAGE_TO_SEND.length()));
            conn.setRequestProperty("SOAPAction", "http://www.oasis-open.org/committees/security");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(MESSAGE_TO_SEND);
            out.flush();

            in = new BufferedReader(CommonUtils.isNotBlank(getEncoding()) ? new InputStreamReader(
                    conn.getInputStream(), Charset.forName(getEncoding())) : new InputStreamReader(
                    conn.getInputStream()));
            final StringBuilder buffer = new StringBuilder(256);

            String line;

            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            CommonUtils.closeQuietly(out);
            CommonUtils.closeQuietly(in);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void setTolerance(final long tolerance) {
        this.tolerance = tolerance;
    }
}
