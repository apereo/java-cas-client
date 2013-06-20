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
package org.jasig.cas.client.tomcat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CasRealm} implementation with users and roles defined by a properties
 * file with the following format:
 * <pre>
 * username1=role1,role2,role3
 * username2=role1
 * username3=role2,role3
 * </pre>
 * User authentication succeeds if the name of the given principal exists as
 * a username in the properties file.
 *
 * @author Middleware
 * @version $Revision$
 * @since 3.1.12
 *
 */
public class PropertiesCasRealmDelegate implements CasRealm {

    /** Log instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Path to backing properties file */
    private String propertiesFilePath;

    /** Map of usernames to roles */
    private Map<String, Set<String>> roleMap;

    /**
     * @param path Path to properties file container username/role data.
     */
    public void setPropertiesFilePath(final String path) {
        propertiesFilePath = path;
    }

    /** {@inheritDoc} */
    public void readProperties() {
        CommonUtils.assertNotNull(propertiesFilePath, "PropertiesFilePath not set.");
        File file = new File(propertiesFilePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("catalina.base"), propertiesFilePath);
        }
        CommonUtils.assertTrue(file.exists(), "File not found " + file);
        CommonUtils.assertTrue(file.canRead(), "Cannot read " + file);
        logger.debug("Loading users/roles from {}", file);
        final Properties properties = new Properties();
        try {
            properties.load(new BufferedInputStream(new FileInputStream(file)));
        } catch (final IOException e) {
            throw new IllegalStateException("Error loading users/roles from " + file, e);
        }
        this.roleMap = new HashMap<String, Set<String>>(properties.size());

        for (final Object key : properties.keySet()) {
            final String user = (String) key;
            // Use TreeSet to sort roles
            final Set<String> roleSet = new HashSet<String>();
            final String[] roles = properties.getProperty(user).split(",\\s*");
            roleSet.addAll(Arrays.asList(roles));
            roleMap.put(user, roleSet);
        }
    }

    /** {@inheritDoc} */
    public Principal authenticate(final Principal p) {
        if (this.roleMap.containsKey(p.getName())) {
            return p;
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String[] getRoles(final Principal p) {
        final Set<String> roleSet = this.roleMap.get(p.getName());
        return roleSet.toArray(new String[roleSet.size()]);
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        if ("*".equals(role)) {
            return true;
        }

        final Set<String> roles = this.roleMap.get(principal.getName());

        return roles != null && roles.contains(role);
    }
}
