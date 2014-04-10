package org.jasig.cas.client.configuration;

import org.junit.Test;

import static org.junit.Assert.*;

public final class ConfigurationStrategyNameTests {

    @Test
    public void stringToClass() {
        assertEquals(JndiConfigurationStrategyImpl.class, ConfigurationStrategyName.resolveToConfigurationStrategy(ConfigurationStrategyName.JNDI.name()));
        assertEquals(WebXmlConfigurationStrategyImpl.class, ConfigurationStrategyName.resolveToConfigurationStrategy(ConfigurationStrategyName.WEB_XML.name()));
        assertEquals(LegacyConfigurationStrategyImpl.class, ConfigurationStrategyName.resolveToConfigurationStrategy(ConfigurationStrategyName.DEFAULT.name()));
        assertEquals(LegacyConfigurationStrategyImpl.class, ConfigurationStrategyName.resolveToConfigurationStrategy("bleh!"));
    }
}
