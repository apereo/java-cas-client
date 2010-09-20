/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;

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
 *
 */
public class PropertiesCasRealmDelegate implements CasRealm  {
    /** Log instance */
    private final Log log = LogFactory.getLog(getClass());
    
    /** Path to backing properties file */
    private String propertiesFilePath;
    
    /** Map of usernames to roles */
    private Map roleMap;
    
    /**
     * @param path Path to properties file container username/role data.
     */
    public void setPropertiesFilePath(final String path) {
        propertiesFilePath = path;
    }

    /** {@inheritDoc} */
    public void readProperties()
    {
        CommonUtils.assertNotNull(propertiesFilePath, "PropertiesFilePath not set.");
        File file = new File(propertiesFilePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("catalina.base"), propertiesFilePath);
        }
        CommonUtils.assertTrue(file.exists(), "File not found " + file);        
        CommonUtils.assertTrue(file.canRead(), "Cannot read " + file);
        log.debug("Loading users/roles from " + file);
        final Properties properties = new Properties();
        try {
            properties.load(new BufferedInputStream(new FileInputStream(file)));
        } catch (IOException e) {
            throw new IllegalStateException("Error loading users/roles from " + file, e);
        }
        roleMap = new HashMap(properties.size());
        final Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            final String user = (String) keys.next();
            // Use TreeSet to sort roles
            final Set roleSet = new HashSet();
            final String[] roles = properties.getProperty(user).split(",\\s*");
            for (int i = 0; i < roles.length; i++) {
                roleSet.add(roles[i]);
            }
            roleMap.put(user, roleSet); 
        }
    }

    /** {@inheritDoc} */
    public Principal authenticate(final Principal p) {
        if (roleMap.get(p.getName()) != null) {
            return p;
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String[] getRoles(final Principal p) {
        final Set roleSet = (Set) roleMap.get(p.getName());
        final String[] roles = new String[roleSet.size()];
        roleSet.toArray(roles);
        return roles;
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        final Set roles = (Set) roleMap.get(principal.getName());
        if (roles != null) {
            return roles.contains(role);
        } else {
            return false;
        }
    }
}
