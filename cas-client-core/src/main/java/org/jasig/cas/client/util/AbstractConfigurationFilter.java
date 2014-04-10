/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.util;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.jasig.cas.client.configuration.ConfigurationKey;
import org.jasig.cas.client.configuration.ConfigurationStrategy;
import org.jasig.cas.client.configuration.ConfigurationStrategyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstracts out the ability to configure the filters from the initial properties provided.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractConfigurationFilter implements Filter {

    private static final String CONFIGURATION_STRATEGY_KEY = "configurationStrategy";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignoreInitConfiguration = false;

    private ConfigurationStrategy configurationStrategy;

    public void init(FilterConfig filterConfig) throws ServletException {
        final String configurationStrategyName = filterConfig.getServletContext().getInitParameter(CONFIGURATION_STRATEGY_KEY);
        this.configurationStrategy = ReflectUtils.newInstance(ConfigurationStrategyName.resolveToConfigurationStrategy(configurationStrategyName));
        this.configurationStrategy.init(filterConfig, getClass());
    }

    protected final boolean getBoolean(final ConfigurationKey<Boolean> configurationKey) {
        return this.configurationStrategy.getBoolean(configurationKey);
    }

    protected final String getString(final ConfigurationKey<String> configurationKey) {
        return this.configurationStrategy.getString(configurationKey);
    }

    protected final long getLong(final ConfigurationKey<Long> configurationKey) {
        return this.configurationStrategy.getLong(configurationKey);
    }

    protected final int getInt(final ConfigurationKey<Integer> configurationKey) {
        return this.configurationStrategy.getInt(configurationKey);
    }

    protected final <T> Class<? extends T> getClass(final ConfigurationKey<Class<? extends T>> configurationKey) {
        return this.configurationStrategy.getClass(configurationKey);
    }

    public final void setIgnoreInitConfiguration(final boolean ignoreInitConfiguration) {
        this.ignoreInitConfiguration = ignoreInitConfiguration;
    }

    protected final boolean isIgnoreInitConfiguration() {
        return this.ignoreInitConfiguration;
    }
}
