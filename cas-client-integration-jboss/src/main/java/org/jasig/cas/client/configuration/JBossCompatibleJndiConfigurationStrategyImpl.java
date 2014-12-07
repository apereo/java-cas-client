package org.jasig.cas.client.configuration;

/**
 * Simple extension to the {@link org.jasig.cas.client.configuration.JndiConfigurationStrategyImpl} to provide a JBoss 7 compatible prefix.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class JBossCompatibleJndiConfigurationStrategyImpl extends JndiConfigurationStrategyImpl {

    private static final String ENVIRONMENT_PREFIX = "java:/comp/env/cas/";

    public JBossCompatibleJndiConfigurationStrategyImpl() {
        super(ENVIRONMENT_PREFIX);
    }
}
