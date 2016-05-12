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
package org.jasig.cas.client.http;

import java.io.IOException;
import java.io.Writer;

/**
 * An abstraction of the essential properties of an HTTP response necessary for
 * CAS protocol handling.
 *
 * @author Carl Harris
 */
public interface HttpResponse {

    /**
     * Gets a writer that can be used to send character text to the connected client.
     * @return writer
     * @throws IOException
     */
    Writer getWriter() throws IOException;

    /**
     * Encodes the specified URL by including the session ID, or, if encoding is not needed, returns the URL unchanged.
     * @param url the subject URL
     * @return encoded URL
     */
    String encodeURL(String url);

    /**
     * Sends a temporary redirect response to the client using the specified redirect location URL.
     * <p>
     * An implementation should return a 302 response with a {@code Location} header containing
     * the specified URL.
     *
     * @param url the URL to include in the {@code Location} header of the response.
     * @throws IOException
     */
    void sendRedirect(String url) throws IOException;

}
