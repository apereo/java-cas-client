package org.jasig.cas.client.configuration;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * Abstraction to allow for pluggable methods for retrieving filter configuration.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public interface ConfigurationStrategy {


    /**
     * Retrieves the value for the provided {@param configurationKey}, falling back to the {@param defaultValue} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @param defaultValue     the default value to use if the key cannot be found.
     * @return the configured value, or the default value.
     */
    boolean getBoolean(ConfigurationKey configurationKey, boolean defaultValue);

    /**
     * Retrieves the value for the provided {@param configurationKey}, falling back to the {@param defaultValue} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @param defaultValue     the default value to use if the key cannot be found.
     * @return the configured value, or the default value.
     */
    String getString(ConfigurationKey configurationKey, String defaultValue);

    /**
     * Retrieves the value for the provided {@param configurationKey}, falling back to the {@param defaultValue} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @param defaultValue     the default value to use if the key cannot be found.
     * @return the configured value, or the default value.
     */
    long getLong(ConfigurationKey configurationKey, long defaultValue);

    /**
     * Retrieves the value for the provided {@param configurationKey}, falling back to the {@param defaultValue} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @param defaultValue     the default value to use if the key cannot be found.
     * @return the configured value, or the default value.
     */
    int getInt(ConfigurationKey configurationKey, int defaultValue);

    /**
     * Retrieves the value for the provided {@param configurationKey}, falling back to the {@param defaultValue} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @param defaultValue     the default value to use if the key cannot be found.
     * @return the configured value, or the default value.
     */
    <T> Class<T> getClass(ConfigurationKey configurationKey, Class<T> defaultValue);

    /**
     * Initializes the strategy.  This must be called before calling {@link #getString(ConfigurationKey, String)} or {@link #getBoolean(ConfigurationKey, boolean)}.
     *
     * @param filterConfig the filter configuration object.
     * @param filterClazz  the filter
     */
    void init(FilterConfig filterConfig, Class<? extends Filter> filterClazz);
}
