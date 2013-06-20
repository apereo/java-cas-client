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
package org.jasig.cas.client.tomcat.v6;

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
 * @since 3.1.12
 *
 */
public class PropertiesCasRealm extends AbstractCasRealm {

    private final PropertiesCasRealmDelegate delegate = new PropertiesCasRealmDelegate();

    /**
     * @param path Path to properties file container username/role data.
     */
    public void setPropertiesFilePath(final String path) {
        this.delegate.setPropertiesFilePath(path);
    }

    /** {@inheritDoc} */
    public void start() throws LifecycleException {
        super.start();
        this.delegate.readProperties();
        logger.info("Startup completed.");
    }

    /** {@inheritDoc} */
    protected CasRealm getDelegate() {
        return this.delegate;
    }

}
