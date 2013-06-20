package org.jasig.cas.client.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Interface to abstract the authentication strategy for redirecting.  The traditional method was to always just redirect,
 * but due to AJAX, etc. we may need to support other strategies.  This interface is designed to hold that logic such that
 * authentication filter class does not get crazily complex.
 *
 * @author Scott Battaglia
 * @since 3.3.0
 */
public interface AuthenticationRedirectStrategy {

    /**
     * Method name is a bit of a misnomer.  This method handles "redirection" for a localized version of redirection (i.e. AJAX might mean an XML fragment that contains the url to go to).
     *
     * @param request the original HttpServletRequest.   MAY NOT BE NULL.
     * @param response the original HttpServletResponse.  MAY NOT BE NULL.
     * @param potentialRedirectUrl the url that might be used (there are no guarantees of course!)
     * @throws IOException the exception to throw if there is some type of error.  This will bubble up through the filter.
     */
    void redirect(HttpServletRequest request, HttpServletResponse response, String potentialRedirectUrl)
            throws IOException;

}
