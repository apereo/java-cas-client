/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstract validator implementation for tickets that must be validated against a server.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractUrlBasedTicketValidator implements TicketValidator {

    /**
     * Commons Logging instance.
     */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Prefix for the CAS server.   Should be everything up to the url endpoint, including the /.
     *
     * i.e. https://cas.rutgers.edu/
     */
    private final String casServerUrlPrefix;

    /**
     * Whether the request include a renew or not.
     */
    private boolean renew;

    /**
     * A map containing custom parameters to pass to the validation url.
     */
    private Map customParameters;

    /**
     * Constructs a new TicketValidator with the casServerUrlPrefix.
     *
     * @param casServerUrlPrefix the location of the CAS server.
     */
    protected AbstractUrlBasedTicketValidator(final String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
        CommonUtils.assertNotNull(this.casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
    }

    /**
     * Template method for ticket validators that need to provide additional parameters to the validation url.
     *
     * @param urlParameters the map containing the parameters.
     */
    protected void populateUrlAttributeMap(final Map urlParameters) {
        // nothing to do
    }

    /**
     * The endpoint of the validation URL.  Should be relative (i.e. not start with a "/").  I.e. validate or serviceValidate.
     * @return the endpoint of the validation URL.
     */
    protected abstract String getUrlSuffix();

    /**
     * Constructs the URL to send the validation request to.
     *
     * @param ticket the ticket to be validated.
     * @param serviceUrl the service identifier.
     * @return the fully constructed URL.
     */
    protected final String constructValidationUrl(final String ticket, final String serviceUrl) {
        final Map urlParameters = new HashMap();

        log.debug("Placing URL parameters in map.");
        urlParameters.put("ticket", ticket);
        urlParameters.put("service", encodeUrl(serviceUrl));

        if (this.renew) {
            urlParameters.put("renew", "true");
        }

        log.debug("Calling template URL attribute map.");
        populateUrlAttributeMap(urlParameters);

        log.debug("Loading custom parameters from configuration.");
        if (this.customParameters != null) {
            urlParameters.putAll(this.customParameters);
        }

        final String suffix = getUrlSuffix();
        final StringBuffer buffer = new StringBuffer(urlParameters.size()*10 + this.casServerUrlPrefix.length() + suffix.length() +1);

        int i = 0;
        synchronized (buffer) {
            buffer.append(this.casServerUrlPrefix);
            if (!this.casServerUrlPrefix.endsWith("/")) {
            	buffer.append("/");	
            }
            buffer.append(suffix);

            for (final Iterator iter = urlParameters.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry entry = (Map.Entry) iter.next();
                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();

                if (value != null) {
                    buffer.append(i++ == 0 ? "?" : "&");
	                buffer.append(key);
	                buffer.append("=");
	                buffer.append(value);
                }
            }

            return buffer.toString();
        }
    }

    /**
     * Encodes a URL using the URLEncoder format.
     *
     * @param url the url to encode.
     * @return the encoded url, or the original url if "UTF-8" character encoding could not be found.                       
     */
    protected final String encodeUrl(final String url) {
    	if (url == null) {
    		return null;
    	}
    	
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return url;
        }
    }

    /**
     * Parses the response from the server into a CAS Assertion.
     *
     * @param response the response from the server, in any format.
     * @return the CAS assertion if one could be parsed from the response.
     * @throws TicketValidationException if an Assertion could not be created.
     *
     */
    protected abstract Assertion parseResponseFromServer(final String response) throws TicketValidationException;

    /**
     * Contacts the CAS Server to retrieve the response for the ticket validation.
     *
     * @param validationUrl the url to send the validation request to.
     * @param ticket the ticket to validate.
     * @return the response from the CAS server.
     */

    protected abstract String retrieveResponseFromServer(URL validationUrl, String ticket);

    public Assertion validate(final String ticket, final String service) throws TicketValidationException {


        final String validationUrl = constructValidationUrl(ticket, service);
        if (log.isDebugEnabled()) {
            log.debug("Constructing validation url: " + validationUrl);
        }

        try {
        	log.debug("Retrieving response from server.");
            final String serverResponse = retrieveResponseFromServer(new URL(validationUrl), ticket);

            if (serverResponse == null) {
                throw new TicketValidationException("The CAS server returned no response.");
            }
            
            if (log.isDebugEnabled()) {
            	log.debug("Server response: " + serverResponse);
            }

            return parseResponseFromServer(serverResponse);
        } catch (final MalformedURLException e) {
            throw new TicketValidationException(e);
        }
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    public void setCustomParameters(final Map customParameters) {
        this.customParameters = customParameters;
    }
}