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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This implement support loading properties file from classpath or normal file system.<br/>
 * <li> Loading from classpath (classpath:/some_path/cas-java-client.properties).</li>
 * <li> Loading from file system (/etc/cas-java-client.properties).</li>
 *
 * @author Scott Battaglia
 * @author Luo Peng
 * @since 3.4.0
 */
public final class PropertiesConfigurationStrategyImpl extends BaseConfigurationStrategy {

    /**
     * Property name we'll use in the {@link javax.servlet.FilterConfig} and {@link javax.servlet.ServletConfig} to try and find where
     * you stored the configuration file.
     */
    private static final String CONFIGURATION_FILE_LOCATION = "configFileLocation";

    /**
     * Default location of the configuration file.  Mostly for testing/demo.  You will most likely want to configure an alternative location.
     */
    private static final String DEFAULT_CONFIGURATION_FILE_LOCATION = "/etc/java-cas-client.properties";

    /**
     * The classpath file prefix. While file name starts with this, read properties from classpath
     */
    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final int CLASSPATH_PLACEHOLDER_SIZE = CLASSPATH_PREFIX.length();

    private static final ClassLoader CLASS_LOADER = PropertiesConfigurationStrategyImpl.class.getClassLoader();

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfigurationStrategyImpl.class);

    private String simpleFilterName;

    private Properties properties = new Properties();

    @Override
    protected String get(final ConfigurationKey configurationKey) {
        final String property = configurationKey.getName();
        final String filterSpecificProperty = this.simpleFilterName + "." + property;

        final String filterSpecificValue = this.properties.getProperty(filterSpecificProperty);

        if (CommonUtils.isNotEmpty(filterSpecificValue)) {
            return filterSpecificValue;
        }

        return this.properties.getProperty(property);
    }

    public void init(final FilterConfig filterConfig, final Class<? extends Filter> filterClazz) {
        this.simpleFilterName = filterClazz.getSimpleName();
        final String fileLocationFromFilterConfig = filterConfig.getInitParameter(CONFIGURATION_FILE_LOCATION);
        final boolean filterConfigFileLoad = loadPropertiesFromFile(fileLocationFromFilterConfig);

        if (!filterConfigFileLoad) {
            final String fileLocationFromServletConfig = filterConfig.getServletContext().getInitParameter(CONFIGURATION_FILE_LOCATION);
            final boolean servletContextFileLoad = loadPropertiesFromFile(fileLocationFromServletConfig);

            if (!servletContextFileLoad) {
                final boolean defaultConfigFileLoaded = loadPropertiesFromFile(DEFAULT_CONFIGURATION_FILE_LOCATION);
                CommonUtils.assertTrue(defaultConfigFileLoaded, "unable to load properties to configure CAS client");
            }
        }
    }

    private boolean loadPropertiesFromFile(final String file) {
        if (CommonUtils.isEmpty(file)) {
            return false;
        }

        InputStream is = null;
        try {
            if (file.startsWith(CLASSPATH_PREFIX)) {
                String classpathFile = file.substring(CLASSPATH_PLACEHOLDER_SIZE);
                is = CLASS_LOADER.getResourceAsStream(classpathFile);
            } else {
                is = new FileInputStream(file);
            }

            this.properties.load(is);
            return true;
        } catch (final IOException e) {
            LOGGER.warn("Unable to load properties for file {}", file, e);
            return false;
        } finally {
            CommonUtils.closeQuietly(is);
        }
    }
}
