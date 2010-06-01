/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Callback handler that provides the CAS service and ticket to a
 * {@link NameCallback} and {@link PasswordCallback} respectively,
 * which meets the requirements of the {@link CasLoginModule} JAAS module.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 *
 */
public class ServiceAndTicketCallbackHandler implements CallbackHandler {

    /** CAS service URL */
    private final String service;
   
    /** CAS service ticket */
    private final String ticket;
   
    /**
     * Creates a new instance with the given service and ticket.
     *
     * @param service CAS service URL.
     * @param ticket CAS service ticket.
     */
    public ServiceAndTicketCallbackHandler(final String service, final String ticket) {
        this.service = service;
        this.ticket = ticket;
    }

    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback) callbacks[i]).setName(this.service);
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) callbacks[i]).setPassword(this.ticket.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Callback not supported.");
            }
        }
    }

}
