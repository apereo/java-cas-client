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
package org.jasig.cas.client.http.servlet;

import org.jasig.cas.client.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * An {@link HttpResponse} that delegates to an {@link HttpServletResponse}.
 *
 * @author Carl Harris
 */
public class DelegatingHttpResponse implements HttpResponse {

    private final HttpServletResponse delegate;

    public DelegatingHttpResponse(final HttpServletResponse delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate is required");
        }
        this.delegate = delegate;
    }

    @Override
    public Writer getWriter() throws IOException {
        return delegate.getWriter();
    }

    @Override
    public String encodeURL(final String url) {
        return delegate.encodeURL(url);
    }

    @Override
    public void sendRedirect(final String url) throws IOException {
        delegate.sendRedirect(url);
    }

}
