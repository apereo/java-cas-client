package org.jasig.cas.client.configuration;

import org.junit.Test;

import static org.junit.Assert.*;

public final class ConfigurationKeyTests {


    @Test
    public void gettersWithNoDefaultValue() {
        final String name = "name";
        final ConfigurationKey<Boolean> configurationKey = new ConfigurationKey<Boolean>(name);
        assertEquals(name, configurationKey.getName());
        assertNull(configurationKey.getDefaultValue());
    }


    @Test
    public void gettersWithDefaultValue() {
        final String name = "name";
        final Boolean defaultValue = Boolean.TRUE;
        final ConfigurationKey<Boolean> configurationKey = new ConfigurationKey<Boolean>(name, defaultValue);
        assertEquals(name, configurationKey.getName());
        assertEquals(defaultValue, configurationKey.getDefaultValue());
    }
}
