package org.jasig.cas.client.authentication;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of the {@link AuthenticationRedirectStrategy} class that preserves the original behavior that existed prior to 3.3.0.
 *
 * @author Scott Battaglia
 * @since 3.3.0
 */
public final class DefaultAuthenticationRedirectStrategy implements AuthenticationRedirectStrategy {

    public void redirect(final HttpServletRequest request, final HttpServletResponse response,
            final String potentialRedirectUrl) throws IOException {
        response.sendRedirect(potentialRedirectUrl);
    }
}
