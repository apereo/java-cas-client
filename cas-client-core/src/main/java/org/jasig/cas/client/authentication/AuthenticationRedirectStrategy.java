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
package org.jasig.cas.client.authentication;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
