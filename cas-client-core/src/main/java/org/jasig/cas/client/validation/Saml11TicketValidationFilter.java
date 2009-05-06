/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import javax.servlet.FilterConfig;

/**
 * Implementation of TicketValidationFilter that can instanciate a SAML 1.1 Ticket Validator.
 * <p>
 * Deployers can provide the "casServerUrlPrefix" and "tolerance" properties of the Saml11TicketValidator via the
 * context or filter init parameters.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml11TicketValidationFilter extends AbstractTicketValidationFilter {

    public Saml11TicketValidationFilter() {
        setArtifactParameterName("SAMLart");
        setServiceParameterName("TARGET");
    }

    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final Saml11TicketValidator validator = new Saml11TicketValidator(getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null));
        final String tolerance = getPropertyFromInitParams(filterConfig, "tolerance", "1000");
        validator.setTolerance(Long.parseLong(tolerance));
        validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));
        return validator;
    }
}
