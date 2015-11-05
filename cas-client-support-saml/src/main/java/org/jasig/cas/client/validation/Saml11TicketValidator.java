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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;

/**
 * TicketValidator that can understand validating a SAML artifact.  This includes the SOAP request/response.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public final class Saml11TicketValidator extends AbstractUrlBasedTicketValidator {

    /** Authentication attribute containing SAML AuthenticationMethod attribute value. */
    public static final String AUTH_METHOD_ATTRIBUTE = "samlAuthenticationStatement::authMethod";

    /** SAML 1.1 request template. */
    private static final String SAML_REQUEST_TEMPLATE;

    /** SAML 1.1. namespace context. */
    private static final NamespaceContext NS_CONTEXT = new MapNamespaceContext(
            "soap->http://schemas.xmlsoap.org/soap/envelope/",
            "sa->urn:oasis:names:tc:SAML:1.0:assertion",
            "sp->urn:oasis:names:tc:SAML:1.0:protocol");

    /** XPath expression to extract Assertion validity start date. */
    private static final ThreadLocalXPathExpression XPATH_ASSERTION_DATE_START =
            new ThreadLocalXPathExpression("//sa:Assertion/sa:Conditions/@NotBefore", NS_CONTEXT);

    /** XPath expression to extract Assertion validity end date. */
    private static final ThreadLocalXPathExpression XPATH_ASSERTION_DATE_END =
            new ThreadLocalXPathExpression("//sa:Assertion/sa:Conditions/@NotOnOrAfter", NS_CONTEXT);

    /** XPath expression to extract NameIdentifier. */
    private static final ThreadLocalXPathExpression XPATH_NAME_ID =
            new ThreadLocalXPathExpression("//sa:AuthenticationStatement/sa:Subject/sa:NameIdentifier", NS_CONTEXT);

    /** XPath expression to extract authentication method. */
    private static final ThreadLocalXPathExpression XPATH_AUTH_METHOD =
            new ThreadLocalXPathExpression("//sa:AuthenticationStatement/@AuthenticationMethod", NS_CONTEXT);

    /** XPath expression to extract attributes. */
    private static final ThreadLocalXPathExpression XPATH_ATTRIBUTES =
            new ThreadLocalXPathExpression("//sa:AttributeStatement/sa:Attribute", NS_CONTEXT);

    private static final String HEX_CHARS = "0123456789abcdef";

    /** Time tolerance to allow for time drifting. */
    private long tolerance = 1000L;

    private final Random random;


    /** Class initializer. */
    static {
        try {
            SAML_REQUEST_TEMPLATE = IOUtils.readString(
                    Saml11TicketValidator.class.getResourceAsStream("/META-INF/cas/samlRequestTemplate.xml"));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load SAML request template from classpath", e);
        }

    }

    public Saml11TicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot find required SHA1PRNG algorithm");
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

    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        try {
            final Document document = XmlUtils.newDocument(response);
            final Date assertionValidityStart = SamlUtils.parseUtcDate(
                    XPATH_ASSERTION_DATE_START.evaluateAsString(document));
            final Date assertionValidityEnd = SamlUtils.parseUtcDate(
                    XPATH_ASSERTION_DATE_END.evaluateAsString(document));
            if (!isValidAssertion(assertionValidityStart, assertionValidityEnd)) {
                throw new TicketValidationException("Invalid SAML assertion");
            }
            final String nameId = XPATH_NAME_ID.evaluateAsString(document);
            if (nameId == null) {
                throw new TicketValidationException("SAML assertion does not contain NameIdentifier element");
            }
            final String authMethod = XPATH_AUTH_METHOD.evaluateAsString(document);
            final NodeList attributes = XPATH_ATTRIBUTES.evaluateAsNodeList(document);
            final Map<String, Object> principalAttributes = new HashMap<String, Object>(attributes.getLength());
            Element attribute;
            NodeList values;
            String name;
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = (Element) attributes.item(i);
                name = attribute.getAttribute("AttributeName");
                logger.trace("Processing attribute {}", name);
                values = attribute.getElementsByTagNameNS("*", "AttributeValue");
                if (values.getLength() == 1) {
                    principalAttributes.put(name, values.item(0).getTextContent());
                } else {
                    final Collection<Object> items = new ArrayList<Object>(values.getLength());
                    for (int j = 0; j < values.getLength(); j++) {
                        items.add(values.item(j).getTextContent());
                    }
                    principalAttributes.put(name, items);
                }
            }
            return new AssertionImpl(
                    new AttributePrincipalImpl(nameId, principalAttributes),
                    assertionValidityStart,
                    assertionValidityEnd,
                    new Date(),
                    Collections.singletonMap(AUTH_METHOD_ATTRIBUTE, (Object) authMethod));
        } catch (final Exception e) {
            throw new TicketValidationException("Error processing SAML response", e);
        }
    }

    private boolean isValidAssertion(final Date notBefore, final Date notOnOrAfter) {
        if (notBefore == null || notOnOrAfter == null) {
            logger.debug("Assertion is not valid because it does not have bounding dates.");
            return false;
        }

        final DateTime currentTime = new DateTime(DateTimeZone.UTC);
        final Interval validityRange = new Interval(
                new DateTime(notBefore).minus(this.tolerance),
                new DateTime(notOnOrAfter).plus(this.tolerance));

        if (validityRange.contains(currentTime)) {
            logger.debug("Current time is within the interval validity.");
            return true;
        }

        if (currentTime.isBefore(validityRange.getStart())) {
            logger.debug("Assertion is not yet valid");
        } else {
            logger.debug("Assertion is expired");
        }
        return false;
    }

    protected String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        final String request = String.format(
                SAML_REQUEST_TEMPLATE,
                generateId(),
                SamlUtils.formatForUtcTime(new Date()),
                ticket);
        HttpURLConnection conn = null;
        try {
            conn = this.getURLConnectionFactory().buildHttpURLConnection(validationUrl.openConnection());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("SOAPAction", "http://www.oasis-open.org/committees/security");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            final Charset charset = CommonUtils.isNotBlank(getEncoding()) ?
                    Charset.forName(getEncoding()) : IOUtils.UTF8;
            conn.getOutputStream().write(request.getBytes(charset));
            return IOUtils.readString(conn.getInputStream(), charset);
        } catch (final IOException e) {
            throw new RuntimeException("IO error sending HTTP request to /samlValidate", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void setTolerance(final long tolerance) {
        this.tolerance = tolerance;
    }

    private String generateId() {
        final byte[] data = new byte[16];
        random.nextBytes(data);
        final StringBuilder id = new StringBuilder(33);
        id.append('_');
        for (int i = 0; i < data.length; i++) {
            id.append(HEX_CHARS.charAt((data[i] & 0xF0) >> 4));
            id.append(HEX_CHARS.charAt(data[i] & 0x0F));
        }
        return id.toString();
    }
}
