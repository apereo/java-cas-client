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
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractConfigurationFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignoreInitConfiguration = false;

    /**
     * Retrieves the property from the FilterConfig.  First it checks the FilterConfig's initParameters to see if it
     * has a value.
     * If it does, it returns that, otherwise it retrieves the ServletContext's initParameters and returns that value if any.
     * <p>
     * Finally, it will check JNDI if all other methods fail.  All the JNDI properties should be stored under either java:comp/env/cas/SHORTFILTERNAME/{propertyName}
     * or java:comp/env/cas/{propertyName}
     * <p>
     * Essentially the documented order is:
     * <ol>
     *     <li>FilterConfig.getInitParameter</li>
     *     <li>ServletContext.getInitParameter</li>
     *     <li>java:comp/env/cas/SHORTFILTERNAME/{propertyName}</li>
     *     <li>java:comp/env/cas/{propertyName}</li>
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
        final String value = filterConfig.getInitParameter(propertyName);

        if (CommonUtils.isNotBlank(value)) {
            if ("renew".equals(propertyName)) {
                throw new IllegalArgumentException(
                        "Renew MUST be specified via context parameter or JNDI environment to avoid misconfiguration.");
            }
            logger.info("Property [{}] loaded from FilterConfig.getInitParameter with value [{}]", propertyName, value);
            return value;
        }

        final String value2 = filterConfig.getServletContext().getInitParameter(propertyName);

        if (CommonUtils.isNotBlank(value2)) {
            logger.info("Property [{}] loaded from ServletContext.getInitParameter with value [{}]", propertyName,
                    value2);
            return value2;
        }
        InitialContext context;
        try {
            context = new InitialContext();
        } catch (final NamingException e) {
            logger.warn(e.getMessage(), e);
            return defaultValue;
        }

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
}
