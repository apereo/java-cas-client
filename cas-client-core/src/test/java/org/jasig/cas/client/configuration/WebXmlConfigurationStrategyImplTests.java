package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class WebXmlConfigurationStrategyImplTests {

    private WebXmlConfigurationStrategyImpl impl;

    private MockFilterConfig filterConfig;

    @Before
    public void setUp() throws Exception {
        this.filterConfig = new MockFilterConfig();
        this.impl = new WebXmlConfigurationStrategyImpl();
        this.impl.init(this.filterConfig, AbstractCasFilter.class);
    }


    @Test
    public void noKeyFoundGetDefaultForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        assertEquals(key.getDefaultValue(), this.impl.getBoolean(key));
    }


    @Test
    public void noKeyFoundGetDefaultForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        assertEquals(key.getDefaultValue(), this.impl.getString(key));
    }


    @Test
    public void noKeyFoundGetDefaultForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        assertEquals(key.getDefaultValue().longValue(), this.impl.getLong(key));
    }


    @Test
    public void noKeyFoundGetDefaultForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        assertEquals(key.getDefaultValue().intValue(), this.impl.getInt(key));
    }

    @Test
    public void filterConfigValueForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        final Boolean value = Boolean.TRUE;
        this.filterConfig.addInitParameter(key.getName(), value.toString());
        assertEquals(value, this.impl.getBoolean(key));
    }


    @Test
    public void filterConfigValueForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        final String value = "foobar";
        this.filterConfig.addInitParameter(key.getName(), value);
        assertEquals(value, this.impl.getString(key));
    }


    @Test
    public void filterConfigValueForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        final long value = 1500;
        this.filterConfig.addInitParameter(key.getName(), Long.toString(value));
        assertEquals(value, this.impl.getLong(key));
    }


    @Test
    public void filterConfigValueForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        final int value = 1500;
        this.filterConfig.addInitParameter(key.getName(), Integer.toString(value));
        assertEquals(value, this.impl.getInt(key));
    }


    @Test
    public void servletConfigValueForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        final Boolean value = Boolean.TRUE;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), value.toString());
        assertEquals(value, this.impl.getBoolean(key));
    }


    @Test
    public void servletConfigValueForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        final String value = "foobar";
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), value);
        assertEquals(value, this.impl.getString(key));
    }


    @Test
    public void servletConfigValueForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        final long value = 1500;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), Long.toString(value));
        assertEquals(value, this.impl.getLong(key));
    }


    @Test
    public void servletConfigValueForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        final int value = 1500;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), Integer.toString(value));
        assertEquals(value, this.impl.getInt(key));
    }

}
