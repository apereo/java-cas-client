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
public class JndiConfigurationStrategyImpl extends BaseConfigurationStrategy {

    private static final String ENVIRONMENT_PREFIX = "java:comp/env/cas/";

    private final String environmentPrefix;

    private InitialContext context;

    private String simpleFilterName;

    public JndiConfigurationStrategyImpl() {
        this(ENVIRONMENT_PREFIX);
    }

    public JndiConfigurationStrategyImpl(final String environmentPrefix) {
        this.environmentPrefix = environmentPrefix;
    }

    @Override
    protected final String get(final ConfigurationKey configurationKey) {
        if (context == null) {
            return null;
        }

        final String propertyName = configurationKey.getName();
        final String filterValue = loadFromContext(context, this.environmentPrefix + this.simpleFilterName + "/" + propertyName);

        if (CommonUtils.isNotBlank(filterValue)) {
            logger.info("Property [{}] loaded from JNDI Filter Specific Property with value [{}]", propertyName, filterValue);
            return filterValue;
        }

        final String rootValue = loadFromContext(context, this.environmentPrefix + propertyName);

        if (CommonUtils.isNotBlank(rootValue)) {
            logger.info("Property [{}] loaded from JNDI with value [{}]", propertyName, rootValue);
            return rootValue;
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


    public final void init(final FilterConfig filterConfig, final Class<? extends Filter> clazz) {
        this.simpleFilterName = clazz.getSimpleName();
        try {
            this.context = new InitialContext();
        } catch (final NamingException e) {
            logger.error("Unable to create InitialContext. No properties can be loaded via JNDI.", e);
        }
    }
}
