/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Implementation of the {@link AuthenticationRedirectStrategy} class that preserves the original behavior that existed prior to 3.3.0.
 *
 * @author Scott Battaglia
 * @since 3.3.0
 */
public final class DefaultAuthenticationRedirectStrategy implements AuthenticationRedirectStrategy {

    @Override
    public void redirect(final HttpServletRequest request, final HttpServletResponse response,
                         final String potentialRedirectUrl) throws IOException {
        response.sendRedirect(potentialRedirectUrl);
    }
}
