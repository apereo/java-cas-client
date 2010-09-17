/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.tomcat.CasRealm;
import org.jasig.cas.client.tomcat.PropertiesCasRealmDelegate;

/**
 * Tomcat <code>Realm</code> that implements {@link CasRealm} backed by properties file
 * containing usernames/and roles of the following format:
 * <pre>
 * username1=role1,role2,role3
 * username2=role1
 * username3=role2,role3
 * </pre>
 * User authentication succeeds if the name of the given principal exists as
 * a username in the properties file.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class PropertiesCasRealm extends AbstractCasRealm {
    private final PropertiesCasRealmDelegate delegate = new PropertiesCasRealmDelegate();

    /**
     * @param path Path to properties file container username/role data.
     */
    public void setPropertiesFilePath(final String path) {
        delegate.setPropertiesFilePath(path);
    }
    
    /** {@inheritDoc} */
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        delegate.readProperties();
    }

    /** {@inheritDoc} */
    protected CasRealm getDelegate() {
        return delegate;
    }

}
