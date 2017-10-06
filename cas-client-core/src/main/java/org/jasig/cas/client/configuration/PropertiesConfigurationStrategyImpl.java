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
import java.util.Properties;

/**
 * This implement support loading properties file from classpath or normal file system.
 * 1. loading from classpath (classpath:/some_path/cas-java-client.properties)
 * 2. loading from file system (/etc/cas-java-client.properties)
 * @author Scott Battaglia
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
     * Indicate which system properties should be defined as context-aware
     */
    private static final String ENV_PROFILE_REF_KEY = "envProfileKey";

    private static final String DEFAULT_ENV_PROFILE_KEY = "spring.profiles.active";

    private static final String DEFAULT_SPRING_ENV_PROFILE_KEY = "spring.profiles.default";

    private String envProfile;

    /**
     * The classpath file prefix. While file name starts with this, read properties from classpath 
     */
    private static final String CLASSPATH_PREFIX = "classpath:";

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

        initEnvProfile(filterConfig);

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

    private void initEnvProfile(FilterConfig filterConfig) {
        String envProfileRefKey = filterConfig.getInitParameter(ENV_PROFILE_REF_KEY);
        if(envProfileRefKey == null){
            envProfileRefKey = DEFAULT_ENV_PROFILE_KEY;
        }

        this.envProfile = System.getProperty(envProfileRefKey);
        if(this.envProfile == null){
            //load from context param for spring.profiles.default
            this.envProfile = filterConfig.getServletContext().getInitParameter(DEFAULT_SPRING_ENV_PROFILE_KEY);
        }

        if(this.envProfile == null){
            throw new RuntimeException("environment profile key can't be null. such as spring.profiles.active ?");
        }
    }

    private boolean loadPropertiesFromFile(final String file) {
        if (CommonUtils.isEmpty(file)) {
            return false;
        }

        //filter config file name
        String filteredFile = filterFileName(file);

        if (filteredFile.startsWith(CLASSPATH_PREFIX)) {
            String classpathFile = filteredFile.substring(CLASSPATH_PREFIX.length());
            try {
                properties.load(this.getClass().getClassLoader().getResourceAsStream(classpathFile));
                return true;
            } catch (IOException e) {
                LOGGER.warn("Unable to load properties for file {}", filteredFile, e);
                return false;
            }
        } else {
            FileInputStream fis = null;
                try {
                    fis = new FileInputStream(filteredFile);
                    this.properties.load(fis);
                    return true;
                } catch (final IOException e) {
                    LOGGER.warn("Unable to load properties for file {}", filteredFile, e);
                    return false;
                } finally {
                    CommonUtils.closeQuietly(fis);
                }
        }
    }

    private String filterFileName(String file) {
        return String.format(file,this.envProfile);
    }
}
