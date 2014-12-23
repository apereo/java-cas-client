package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration to map simple names to the underlying classes so that deployers can reference the simple name in the
 * <code>web.xml</code> instead of the fully qualified class name.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public enum ConfigurationStrategyName {

    DEFAULT(LegacyConfigurationStrategyImpl.class), JNDI(JndiConfigurationStrategyImpl.class), WEB_XML(WebXmlConfigurationStrategyImpl.class), PROPERTY_FILE(PropertiesConfigurationStrategyImpl.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationStrategyName.class);

    private final Class<? extends ConfigurationStrategy> configurationStrategyClass;

    private ConfigurationStrategyName(final Class<? extends ConfigurationStrategy> configurationStrategyClass) {
        this.configurationStrategyClass = configurationStrategyClass;
    }

    /**
     * Static helper method that will resolve a simple string to either an enum value or a {@link org.jasig.cas.client.configuration.ConfigurationStrategy} class.
     *
     * @param value the value to attempt to resolve.
     * @return the underlying class that this maps to (either via simple name or fully qualified class name).
     */
    public static Class<? extends ConfigurationStrategy> resolveToConfigurationStrategy(final String value) {
        if (CommonUtils.isBlank(value)) {
            return DEFAULT.configurationStrategyClass;
        }

        for (final ConfigurationStrategyName csn : values()) {
            if (csn.name().equalsIgnoreCase(value)) {
                return csn.configurationStrategyClass;
            }
        }

        try {
            final Class<?> clazz = Class.forName(value);

            if (clazz.isAssignableFrom(ConfigurationStrategy.class)) {
                return (Class<? extends ConfigurationStrategy>) clazz;
            }
        }   catch (final ClassNotFoundException e) {
            LOGGER.error("Unable to locate strategy {} by name or class name.  Using default strategy instead.", value, e);
        }

        return DEFAULT.configurationStrategyClass;
    }
}
