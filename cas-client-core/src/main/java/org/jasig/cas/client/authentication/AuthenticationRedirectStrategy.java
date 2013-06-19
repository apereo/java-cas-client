package org.jasig.cas.client.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by battags on 6/18/13.
 */
public interface AuthenticationRedirectStrategy {

    void redirect(HttpServletRequest request, HttpServletResponse response, String potentialRedirectUrl) throws IOException;

}
