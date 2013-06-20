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
package org.jasig.cas.client.ssl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A factory to prepare and configure {@link java.net.URLConnection} instances. 
 *
 * @author Misagh Moayyed
 * @since 3.3
 */
public interface HttpURLConnectionFactory {

    /**
     * Receives a {@link URLConnection} instance typically as a result of a {@link URL}
     * opening a connection to a remote resource. The received url connection is then
     * configured and prepared appropriately depending on its type and is then returned to the caller
     * to accommodate method chaining.
     *  
     * @param url The url connection that needs to be configured
     * @return The configured {@link HttpURLConnection} instance
     * 
     * @see {@link HttpsURLConnectionFactory}
     */
    HttpURLConnection buildHttpURLConnection(final URLConnection url);
}
