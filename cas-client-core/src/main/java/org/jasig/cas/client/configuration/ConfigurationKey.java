package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.CommonUtils;

/**
 * Holder class to represent a particular configuration key and its optional default value.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class ConfigurationKey<E> {

    private final String name;

    private final E defaultValue;

    public ConfigurationKey(final String name) {
        this(name, null);
    }

    public ConfigurationKey(final String name, final E defaultValue) {
        CommonUtils.assertNotNull(name, "name must not be null.");
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * The referencing name of the configuration key (i.e. what you would use to look it up in your configuration strategy)
     *
     * @return the name.  MUST NOT BE NULL.
     */
    public String getName() {
        return this.name;
    }


    /**
     * The (optional) default value to use when this configuration key is not set.  If a value is provided it should be used. A <code>null</code> value indicates that there is no default.
     *
     * @return the default value or null.
     */
    public E getDefaultValue() {
        return this.defaultValue;
    }
}
