/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.apereo.cas.client.boot.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * Callback interface to be implemented by {@link org.springframework.context.annotation.Configuration Configuration} classes annotated with
 * {@link EnableCasClient} that wish or need to
 * explicitly configure or customize CAS client filters created by {@link CasClientConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 3.6.0
 */
public interface CasClientConfigurer {

    /**
     * Configure or customize CAS authentication filter.
     *
     * @param authenticationFilter the authentication filter
     */
    default void configureAuthenticationFilter(final FilterRegistrationBean authenticationFilter) {
    }

    /**
     * Configure or customize CAS validation filter.
     *
     * @param validationFilter the validation filter
     */
    default void configureValidationFilter(final FilterRegistrationBean validationFilter) {
    }

    /**
     * Configure or customize CAS http servlet wrapper filter.
     *
     * @param httpServletRequestWrapperFilter the http servlet request wrapper filter
     */
    default void configureHttpServletRequestWrapperFilter(final FilterRegistrationBean httpServletRequestWrapperFilter) {
    }

    /**
     * Configure or customize CAS assertion thread local filter.
     *
     * @param assertionThreadLocalFilter the assertion thread local filter
     */
    default void configureAssertionThreadLocalFilter(final FilterRegistrationBean assertionThreadLocalFilter) {
    }
}
