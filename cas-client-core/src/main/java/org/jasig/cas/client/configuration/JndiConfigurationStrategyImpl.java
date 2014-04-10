package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.CommonUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * Loads configuration information from JNDI, using the <code>defaultValue</code> if it can't.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class JndiConfigurationStrategyImpl extends BaseConfigurationStrategy {

    private InitialContext context;

    private String simpleFilterName;

    @Override
    protected String get(ConfigurationKey configurationKey) {
        if (context == null) {
            return null;
        }

        final String propertyName = configurationKey.getSimpleName();
        final String value3 = loadFromContext(context, "java:comp/env/cas/" + this.simpleFilterName + "/" + propertyName);

        if (CommonUtils.isNotBlank(value3)) {
            logger.info("Property [{}] loaded from JNDI Filter Specific Property with value [{}]", propertyName, value3);
            return value3;
        }

        final String value4 = loadFromContext(context, "java:comp/env/cas/" + propertyName);

        if (CommonUtils.isNotBlank(value4)) {
            logger.info("Property [{}] loaded from JNDI with value [{}]", propertyName, value4);
            return value4;
        }

        return null;
    }

    private String loadFromContext(final InitialContext context, final String path) {
        try {
            return (String) context.lookup(path);
        } catch (final NamingException e) {
            return null;
        }
    }


    public void init(final FilterConfig filterConfig, final Class<? extends Filter> clazz) {
        this.simpleFilterName = clazz.getSimpleName();
        try {
            this.context = new InitialContext();
        } catch (final NamingException e) {
            logger.error("Unable to create InitialContext. No properties can be loaded via JNDI.", e);
        }
    }
}
