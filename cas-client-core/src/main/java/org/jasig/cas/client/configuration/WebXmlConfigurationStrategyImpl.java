package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * Implementation of the {@link org.jasig.cas.client.configuration.ConfigurationStrategy} that first checks the {@link javax.servlet.FilterConfig} and
 * then checks the {@link javax.servlet.ServletContext}, ultimately falling back to the <code>defaultValue</code>.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class WebXmlConfigurationStrategyImpl extends BaseConfigurationStrategy {

    private FilterConfig filterConfig;

    protected String get(final ConfigurationKey configurationKey) {
        final String value = this.filterConfig.getInitParameter(configurationKey.getSimpleName());

        if (CommonUtils.isNotBlank(value)) {
            CommonUtils.assertFalse(ConfigurationKey.RENEW.equals(configurationKey), "Renew MUST be specified via context parameter or JNDI environment to avoid misconfiguration.");
            logger.info("Property [{}] loaded from FilterConfig.getInitParameter with value [{}]", configurationKey, value);
            return value;
        }

        final String value2 = filterConfig.getServletContext().getInitParameter(configurationKey.getSimpleName());

        if (CommonUtils.isNotBlank(value2)) {
            logger.info("Property [{}] loaded from ServletContext.getInitParameter with value [{}]", configurationKey,
                    value2);
            return value2;
        }

        return null;
    }

    public void init(final FilterConfig filterConfig, final Class<? extends Filter> clazz) {
        this.filterConfig = filterConfig;
    }
}
