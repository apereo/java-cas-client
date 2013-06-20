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
package org.jasig.cas.client.tomcat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all logout handlers.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public abstract class AbstractLogoutHandler implements LogoutHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String redirectUrl;

    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /** {@inheritDoc} */
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        logger.debug("Processing logout request from CAS server.");

        final Assertion assertion;
        final HttpSession httpSession = request.getSession(false);
        if (httpSession != null
                && (assertion = (Assertion) httpSession.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)) != null) {
            httpSession.removeAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            logger.info("Successfully logged out {}", assertion.getPrincipal());
        } else {
            logger.info("Session already ended.");
        }

        final String redirectUrl = constructRedirectUrl(request);
        if (redirectUrl != null) {
            logger.debug("Redirecting to {}", redirectUrl);
            CommonUtils.sendRedirect(response, redirectUrl);
        }
    }

    /**
     * Constructs a url to redirect to.
     *
     * @param request the original request.
     * @return the url to redirect to. CAN be NULL.
     */
    protected String constructRedirectUrl(final HttpServletRequest request) {
        return this.redirectUrl;
    }
}
