package org.jasig.cas.client.boot.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * Callback interface to be implemented by {@link org.springframework.context.annotation.Configuration Configuration} classes annotated with
 * {@link EnableCasClient} that wish or need to
 * explicitly configure or customize CAS client filters created by {@link CasClientConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
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
