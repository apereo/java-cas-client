/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.util.CommonUtils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Abstract class that knows the protocol for validating a CAS ticket.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractCasProtocolUrlBasedTicketValidator extends AbstractUrlBasedTicketValidator {

    protected AbstractCasProtocolUrlBasedTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    /**
     * Retrieves the response from the server by opening a connection and merely reading the response.
     */
    protected final String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        return CommonUtils.getResponseFromServer(validationUrl);               
    }
}
