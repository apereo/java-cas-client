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

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Performs CAS logout when the request URI matches a regular expression.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.12
 *
 */
public final class RegexUriLogoutHandler extends AbstractLogoutHandler {

    private String logoutUriRegex;

    private Pattern logoutUriPattern;

    /**
     * @param regex Logout URI regular expression.  CANNOT be null.
     */
    public void setLogoutUriRegex(final String regex) {
        this.logoutUriRegex = regex;
    }

    /**
     * Initializes the component for use.
     */
    public void init() {
        CommonUtils.assertNotNull(this.logoutUriRegex, "A logout URI regular expression is required.");
        this.logoutUriPattern = Pattern.compile(this.logoutUriRegex);
    }

    /** {@inheritDoc} */
    public boolean isLogoutRequest(final HttpServletRequest request) {
        return this.logoutUriPattern.matcher(request.getRequestURI()).matches();
    }
}
