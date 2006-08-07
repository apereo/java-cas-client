/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implementation of a Filter that checks for a "ticket" and if one is found,
 * will attempt to validate the ticket. On a successful validation, it sets the
 * Assertion object into the session. On an unsuccessful validation attempt, it
 * sets the response code to 403.
 * <p>
 * This filter needs to be configured after the authentication filter (if that
 * filter exists in the chain).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see TicketValidator
 */
public final class CasValidationFilter extends AbstractCasFilter {

    /** Instance of the ticket validator. */
    private TicketValidator ticketValidator;

    /**
     * Specify whether the filter should redirect the user agent after a
     * successful validation to remove the ticket parameter from the query
     * string.
     */
    private boolean redirectAfterValidation;

    public void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain filterChain)
        throws IOException, ServletException {
        final String ticket = request.getParameter(PARAM_TICKET);

        if (CommonUtils.isNotBlank(ticket)) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to validate ticket: " + ticket);
            }

            try {
                final Assertion assertion = this.ticketValidator.validate(
                    ticket, new SimpleService(constructServiceUrl(request,
                        response)));

                if (log.isDebugEnabled()) {
                    log.debug("Successfully authenticated user: "
                        + assertion.getPrincipal().getId());
                }

                request.setAttribute(CONST_PRINCIPAL, assertion.getPrincipal());

                if (isUseSession()) {
                    request.getSession().setAttribute(CONST_ASSERTION,
                        assertion);
                }
            } catch (final ValidationException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                log.warn(e, e);
                throw new ServletException(e);
            }
        }

        if (this.redirectAfterValidation) {
            response.sendRedirect(response
                .encodeRedirectURL(constructServiceUrl(request, response)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sets the ticket validator for validating tickets.
     * 
     * @param ticketValidator the ticket validator instance we want to use.
     */
    public void setTicketValidator(final TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    /**
     * Sets the flag that tells the filter to redirect after the successful
     * validation.
     * 
     * @param redirectAfterValidation true if we want to redirect, false
     * otherwise.
     */
    public void setRedirectAfterValidation(boolean redirectAfterValidation) {
        this.redirectAfterValidation = redirectAfterValidation;
    }

    protected void afterPropertiesSetInternal() {
        CommonUtils.assertNotNull(this.ticketValidator,
            "ticketValidator cannot be null.");
    }
}
