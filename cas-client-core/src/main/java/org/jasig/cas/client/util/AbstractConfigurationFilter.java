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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstracts out the ability to configure the filters from the initial properties provided.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public abstract class AbstractConfigurationFilter implements Filter {

    protected static final String PARAM_NAME_CONFIG_FILE = "configFile";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignoreInitConfiguration = false;
    
    /** The configuration properties that are loaded from the "configFile" specified. 
     * @see #retrieveParameterValueFromConfigFile(FilterConfig, String)
     * @since 3.3.1
     **/
    private Properties configurationProperties = null;
    
    /**
     * Retrieves the property from the FilterConfig.  First it checks the FilterConfig's initParameters to see if it
     * has a value.
     * If it does, it returns that, otherwise it retrieves the ServletContext's initParameters and returns that value if any.
     * <p>
     * Finally, it will check JNDI if all other methods fail. 
     * All the JNDI properties should be stored under either <code>java:comp/env/cas/SHORTFILTERNAME/{propertyName}</code>
     * or <code>java:comp/env/cas/{propertyName}</code>.
     * <p>If JNDI lookups fail, it will attempt to load the parameter value from a configuration properties file
     * that is specified via the parameter <code>"configFile"</code>. This parameter can be specified via any
     * of the above methods.
     * <p>
     * Essentially the documented order is:
     * <ol>
     *     <li>FilterConfig.getInitParameter</li>
     *     <li>ServletContext.getInitParameter</li>
     *     <li>java:comp/env/cas/SHORTFILTERNAME/{propertyName}</li>
     *     <li>java:comp/env/cas/{propertyName}</li>
     *     <li>Configuration properties file (i.e. /etc/client.properties)</li>
     *     <li>Default Value</li>
     * </ol>
     *
     *
     * @param filterConfig the Filter Configuration.
     * @param propertyName the property to retrieve.
     * @param defaultValue the default value if the property is not found.
     * @return the property value, following the above conventions.  It will always return the more specific value (i.e.
     *  filter vs. context).
     */
    protected final String getPropertyFromInitParams(final FilterConfig filterConfig, final String propertyName,
            final String defaultValue) {
        
        String value = retrieveParameterValueFromWebOrJNDIConfiguration(filterConfig, propertyName);
        if (CommonUtils.isNotBlank(value)) {
            return value;
        }
        
        value = retrieveParameterValueFromConfigFile(filterConfig, propertyName, "/etc/cas/client.properties");
        if (CommonUtils.isNotBlank(value)) {
            return value;
        }

        logger.info("Property [{}] not found.  Using default value [{}]", propertyName, defaultValue);
        return defaultValue;
    }

    protected final boolean parseBoolean(final String value) {
        return ((value != null) && value.equalsIgnoreCase("true"));
    }

    protected final String loadFromContext(final InitialContext context, final String path) {
        try {
            return (String) context.lookup(path);
        } catch (final NamingException e) {
            return null;
        }
    }

    public final void setIgnoreInitConfiguration(boolean ignoreInitConfiguration) {
        this.ignoreInitConfiguration = ignoreInitConfiguration;
    }

    protected final boolean isIgnoreInitConfiguration() {
        return this.ignoreInitConfiguration;
    }
    
    private String retrieveParameterValueFromFilterConfiguration(final FilterConfig filterConfig, final String propertyName) {
        final String value = filterConfig.getInitParameter(propertyName);

        if (CommonUtils.isNotBlank(value)) {
            if ("renew".equals(propertyName)) {
                throw new IllegalArgumentException(
                        "Renew MUST be specified via context parameter or JNDI environment to avoid misconfiguration.");
            }
            logger.info("Property [{}] loaded from FilterConfig.getInitParameter with value [{}]", propertyName, value);
            return value;
        }
        return null;
    }
    
    private String retrieveParameterValueFromServletContextConfiguration(final FilterConfig filterConfig, final String propertyName) {
        final String value2 = filterConfig.getServletContext().getInitParameter(propertyName);

        if (CommonUtils.isNotBlank(value2)) {
            logger.info("Property [{}] loaded from ServletContext.getInitParameter with value [{}]", propertyName,
                    value2);
            return value2;
        }
        return null;
    }
    
    private String retrieveParameterValueFromJNDIConfiguration(final String propertyName) {
        InitialContext context = null;
        try {
            context = new InitialContext();
            final String shortName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
            final String value3 = loadFromContext(context, "java:comp/env/cas/" + shortName + "/" + propertyName);
            
            if (CommonUtils.isNotBlank(value3)) {
                logger.info("Property [{}] loaded from JNDI Filter Specific Property with value [{}]", propertyName, value3);
                return value3;
            }

            final String value4 = loadFromContext(context, "java:comp/env/cas/" + propertyName);

            if (CommonUtils.isNotBlank(value4)) {
                logger.info("Property [{}] loaded from JNDI with value [{}]", propertyName, value4);
                return value4;
            }
            
        } catch (final NamingException e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (final NamingException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return null;
    }
    
    private String retrieveParameterValueFromWebOrJNDIConfiguration(final FilterConfig filterConfig, final String propertyName) {
        String value = retrieveParameterValueFromFilterConfiguration(filterConfig, propertyName);
        if (CommonUtils.isNotBlank(value)) {
            return value;
        }
        
        value = retrieveParameterValueFromServletContextConfiguration(filterConfig, propertyName);
        if (CommonUtils.isNotBlank(value)) {
            return value;
        }
        
        return retrieveParameterValueFromJNDIConfiguration(propertyName);
    }
    
    /**
     * Initialize the configuration file, if not done already. Locate the property in the properties next.
     * @see Properties#getProperty(String)
     */
    private String retrieveParameterValueFromConfigFile(final FilterConfig filterConfig, final String propertyName,
            final String defaultFile) {
        if (this.configurationProperties == null) {
            initializeConfigurationProperties(filterConfig);
        }
        
        if (this.configurationProperties != null) {
            final String value = this.configurationProperties.getProperty(propertyName);
            if (CommonUtils.isNotBlank(value)) {
                logger.info("Property [{}] loaded from configuration file with value [{}]", propertyName, value);
                return value;
            }
        }
        
        return defaultFile;
    }
    
    /**
     * Retrieve the configuration from a config file that is provided by the parameter <code>configFile</code>.
     * The parameter <code>configFile</code> itself can be specified by web or JNDI configuration.
     * @param filterConfig
     */
    private void initializeConfigurationProperties(final FilterConfig filterConfig) {
        
        final String configFileProps = retrieveParameterValueFromWebOrJNDIConfiguration(filterConfig, PARAM_NAME_CONFIG_FILE);
        if (CommonUtils.isNotBlank(configFileProps)) {
            final File configFile = new File(configFileProps);
            
            if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
                this.configurationProperties = new Properties();
    
                FileReader reader = null;
                BufferedReader bufferedReader = null;
    
                try {
                    logger.info("Loading configuration file from {}", configFile.getCanonicalPath());
    
                    reader = new FileReader(configFile);
                    bufferedReader = new BufferedReader(reader);
    
                    this.configurationProperties.load(bufferedReader);
    
                    logger.info("Loaded {} properties from configuration file", this.configurationProperties.size());
    
                } catch (final IOException e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    CommonUtils.closeQuietly(bufferedReader);
                    CommonUtils.closeQuietly(reader);
                }
            } else {
                logger.debug("Configuration file cannot be loaded from {}", configFile.getPath());
            }
        }
    }
}
