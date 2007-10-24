package org.jasig.cas.client.validation;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The filter that handles all the work of validating ticket requests.
 * <p>
 * This filter can be configured with the following values:
 * <ul>
 * <li><code>redirectAfterValidation</code> - redirect the CAS client to the same URL without the ticket.</li>
 * <li><code>exceptionOnValidationFailure</code> - throw an exception if the validation fails.  Otherwise, continue
 *  processing.</li>
 * <li><code>useSession</code> - store any of the useful information in a session attribute.</li>
 * </ul>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractTicketValidationFilter extends AbstractCasFilter {

    /** The TicketValidator we will use to validate tickets. */
    private TicketValidator ticketValidator;

    /**
     * Specify whether the filter should redirect the user agent after a
     * successful validation to remove the ticket parameter from the query
     * string.
     */
    private boolean redirectAfterValidation = false;

    /** Determines whether an exception is thrown when there is a ticket validation failure. */
    private boolean exceptionOnValidationFailure = true;

    private boolean useSession = true;

    /**
     * Template method to return the appropriate validator.
     *
     * @param filterConfig the FilterConfiguration that may be needed to construct a validator.
     * @return the ticket validator.
     */
    protected TicketValidator getTicketValidator(FilterConfig filterConfig) {
        return this.ticketValidator;
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        setExceptionOnValidationFailure(Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "exceptionOnValidationFailure", "true")));
        setRedirectAfterValidation(Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "redirectAfterValidation", "false")));
        setUseSession(Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "useSession", "true")));
        setTicketValidator(getTicketValidator(filterConfig));
    }

    /**
     * Pre-process the request before the normal filter process starts.  This could be useful for pre-empting code.
     *
     * @param servletRequest The servlet request.
     * @param servletResponse The servlet response.
     * @param filterChain the filter chain.
     * @return true if processing should continue, false otherwise.
     * @throws IOException if there is an I/O problem
     * @throws ServletException if there is a servlet problem.
     */
    protected boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        return true;
    }

    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        if (!preFilter(servletRequest, servletResponse, filterChain)) {
            return;
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String ticket = request.getParameter(getArtifactParameterName());

        if (CommonUtils.isNotBlank(ticket)) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to validate ticket: " + ticket);
            }

            try {
                final Assertion assertion = this.ticketValidator.validate(
                        ticket, constructServiceUrl(request,
                        response));

                if (log.isDebugEnabled()) {
                    log.debug("Successfully authenticated user: "
                            + assertion.getPrincipal().getName());
                }

                request.setAttribute(CONST_CAS_ASSERTION, assertion);

                if (this.useSession) {
                    request.getSession().setAttribute(CONST_CAS_ASSERTION,
                            assertion);
                }
            } catch (final TicketValidationException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                log.warn(e, e);

                if (this.exceptionOnValidationFailure) {
                    throw new ServletException(e);
                }
            }

            if (this.redirectAfterValidation) {
                response.sendRedirect(response
                        .encodeRedirectURL(constructServiceUrl(request, response)));
                return;
            }
        }

        filterChain.doFilter(request, response);

    }

    public final void setTicketValidator(final TicketValidator ticketValidator) {
    this.ticketValidator = ticketValidator;
}

    public final void setRedirectAfterValidation(final boolean redirectAfterValidation) {
        this.redirectAfterValidation = redirectAfterValidation;
    }

    public final void setExceptionOnValidationFailure(final boolean exceptionOnValidationFailure) {
        this.exceptionOnValidationFailure = exceptionOnValidationFailure;
    }

    public final void setUseSession(final boolean useSession) {
        this.useSession = useSession;
    }
}
