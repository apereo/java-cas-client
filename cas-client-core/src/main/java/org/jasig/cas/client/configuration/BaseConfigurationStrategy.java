package org.jasig.cas.client.configuration;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
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

    public final boolean getBoolean(final ConfigurationKey configurationKey, final boolean defaultValue) {
        return getValue(configurationKey, Boolean.class, defaultValue, new Parser<Boolean>() {
            public Boolean parse(final String value) {
                return BooleanUtils.toBoolean(value);
            }
        });
    }

    public final long getLong(final ConfigurationKey configurationKey, final long defaultValue) {
        return getValue(configurationKey, Long.class, defaultValue, new Parser<Long>() {
            public Long parse(final String value) {
                return NumberUtils.toLong(value, defaultValue);
            }
        });
    }

    public final int getInt(final ConfigurationKey configurationKey, final int defaultValue) {
        return getValue(configurationKey, Integer.class, defaultValue, new Parser<Integer>() {
            public Integer parse(final String value) {
                return NumberUtils.toInt(value, defaultValue);
            }
        });
    }

    public final String getString(final ConfigurationKey configurationKey, final String defaultValue) {
        return getValue(configurationKey, String.class, defaultValue, new Parser<String>() {
            public String parse(final String value) {
                return value;
            }
        });
    }

    public <T> Class<T> getClass(final ConfigurationKey configurationKey, final Class<T> defaultValue) {
        return getValue(configurationKey, Class.class, defaultValue, new Parser<Class<T>>() {
            public Class<T> parse(final String value) {
                try {
                    return ReflectUtils.loadClass(value);
                } catch (final IllegalArgumentException e) {
                    return defaultValue;
                }
            }
        });
    }

    private <T> T getValue(final ConfigurationKey configurationKey, final Class clazzType, final T defaultValue, final Parser<T> parser) {
        final String value = getWithCheck(configurationKey, clazzType);

        if (CommonUtils.isBlank(value)) {
            return defaultValue;
        }

        return parser.parse(value);
    }

    private String getWithCheck(final ConfigurationKey configurationKey, final Class<?> clazz) {
        CommonUtils.assertNotNull(configurationKey, "configurationKey cannot be null");
        CommonUtils.assertTrue(configurationKey.getPropertyType().equals(clazz), "ConfigurationKey is not of the required type.");

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
