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
package org.jasig.cas.client.tomcat.v85;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles watching a url for the proxy callback.
 * <p>
 * Because its tough to share state between valves, we expose the storage mechanism via a static variable.
 * <p>
 * This valve should be ordered before the authentication valves.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class ProxyCallbackValve extends ValveBase {

    private static ProxyGrantingTicketStorage PROXY_GRANTING_TICKET_STORAGE;

    /** Logger instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String proxyGrantingTicketStorageClass;

    private String proxyCallbackUrl;

    public static ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        return PROXY_GRANTING_TICKET_STORAGE;
    }

    public void setProxyGrantingTicketStorageClass(final String proxyGrantingTicketStorageClass) {
        this.proxyGrantingTicketStorageClass = proxyGrantingTicketStorageClass;
    }

    public void setProxyCallbackUrl(final String proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();

        try {
            CommonUtils.assertNotNull(this.proxyCallbackUrl, "the proxy callback url cannot  be null");
            CommonUtils.assertTrue(this.proxyCallbackUrl.startsWith("/"), "proxy callback url must start with \"/\"");

            PROXY_GRANTING_TICKET_STORAGE = ReflectUtils.newInstance(proxyGrantingTicketStorageClass);
        } catch (final Exception e) {
            throw new LifecycleException(e);
        }
        logger.info("Startup completed.");
    }

    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (this.proxyCallbackUrl.equals(request.getRequestURI())) {
            logger.debug("Processing proxy callback request.");
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, PROXY_GRANTING_TICKET_STORAGE);
            return;
        }

        getNext().invoke(request, response);
    }
}
