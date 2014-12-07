package org.jasig.cas.client.configuration;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to provide most of the boiler-plate code (i.e. checking for proper values, returning defaults, etc.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public abstract class BaseConfigurationStrategy implements ConfigurationStrategy {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final boolean getBoolean(final ConfigurationKey<Boolean> configurationKey) {
        return getValue(configurationKey, new Parser<Boolean>() {
            public Boolean parse(final String value) {
                return BooleanUtils.toBoolean(value);
            }
        });
    }

    public final long getLong(final ConfigurationKey<Long> configurationKey) {
        return getValue(configurationKey, new Parser<Long>() {
            public Long parse(final String value) {
                return NumberUtils.toLong(value, configurationKey.getDefaultValue());
            }
        });
    }

    public final int getInt(final ConfigurationKey<Integer> configurationKey) {
        return getValue(configurationKey, new Parser<Integer>() {
            public Integer parse(final String value) {
                return NumberUtils.toInt(value, configurationKey.getDefaultValue());
            }
        });
    }

    public final String getString(final ConfigurationKey<String> configurationKey) {
        return getValue(configurationKey, new Parser<String>() {
            public String parse(final String value) {
                return value;
            }
        });
    }

    public <T> Class<? extends T> getClass(final ConfigurationKey<Class<? extends T>> configurationKey) {
        return getValue(configurationKey, new Parser<Class<? extends T>>() {
            public Class<? extends T> parse(final String value) {
                try {
                    return ReflectUtils.loadClass(value);
                } catch (final IllegalArgumentException e) {
                    return configurationKey.getDefaultValue();
                }
            }
        });
    }

    private <T> T getValue(final ConfigurationKey<T> configurationKey, final Parser<T> parser) {
        final String value = getWithCheck(configurationKey);

        if (CommonUtils.isBlank(value)) {
            logger.trace("No value found for property {}, returning default {}", configurationKey.getName(), configurationKey.getDefaultValue());
            return configurationKey.getDefaultValue();
        } else {
            logger.trace("Loaded property {} with value {}", configurationKey.getName(), configurationKey.getDefaultValue());
        }

        return parser.parse(value);
    }

    private String getWithCheck(final ConfigurationKey configurationKey) {
        CommonUtils.assertNotNull(configurationKey, "configurationKey cannot be null");

        return get(configurationKey);
    }

    /**
     * Retrieve the String value for this key.  Returns null if there is no value.
     *
     * @param configurationKey the key to retrieve.  MUST NOT BE NULL.
     * @return the String if its found, null otherwise.
     */
    protected abstract String get(ConfigurationKey configurationKey);

    private interface Parser<T> {

        T parse(String value);
    }
}
