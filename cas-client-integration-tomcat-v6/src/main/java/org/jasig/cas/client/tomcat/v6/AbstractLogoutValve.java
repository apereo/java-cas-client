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
package org.jasig.cas.client.tomcat.v6;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jasig.cas.client.tomcat.LogoutHandler;

/**
 * Abstract base class for Container-managed log out.  Removes the attributes
 * from the session.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractLogoutValve extends AbstractLifecycleValve {
    public final void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (getLogoutHandler().isLogoutRequest(request)) {
            getLogoutHandler().logout(request, response);
            // Do not proceed up valve chain
            return;
        }

        logger.debug("URI is not a logout request: {}", request.getRequestURI());
        getNext().invoke(request, response);
    }

    protected abstract LogoutHandler getLogoutHandler();
}
