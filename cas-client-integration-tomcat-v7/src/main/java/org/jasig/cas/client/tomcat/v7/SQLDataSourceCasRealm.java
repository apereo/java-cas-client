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
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.tomcat.CasRealm;
import org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate;

/**
 * Tomcat <code>Realm</code> that implements {@link CasRealm} backed by tables
 * contained in a relational database, accessible through a javax.sql.Datasource.
 * 
 * @see SQLDataSourceCasRealmDelegate
 * 
 * @author gluppi (at) comune.modena.it October 2017
 * @version $Revision$
 * @since 3.1.12
 */
public class SQLDataSourceCasRealm extends AbstractCasRealm
{
    private final SQLDataSourceCasRealmDelegate delegate = new SQLDataSourceCasRealmDelegate();

    public void setJndiName(final String jndiName)
    {
        this.delegate.setJndiName(jndiName);
    }

    public void setRolesTable(final String rolesTable)
    {
        this.delegate.setRolesTable(rolesTable);
    }

    public void setRolesTableRoleColumn(final String rolesTableRoleColumn)
    {
        this.delegate.setRolesTableRoleColumn(rolesTableRoleColumn);
    }

    public void setRolesTableUidColumn(final String rolesTableUidColumn)
    {
        this.delegate.setRolesTableUidColumn(rolesTableUidColumn);
    }

    public void setUsersTable(final String usersTable)
    {
        this.delegate.setUsersTable(usersTable);
    }

    public void setUsersTableEnabledColumn(final String usersTableEnabledColumn)
    {
        this.delegate.setUsersTableEnabledColumn(usersTableEnabledColumn);
    }

    public void setUsersTableUidColumn(final String usersTableUidColumn)
    {
        this.delegate.setUsersTableUidColumn(usersTableUidColumn);
    }

    /** {@inheritDoc} */
    protected CasRealm getDelegate()
    {
        return this.delegate;
    }

    /** {@inheritDoc} */
    protected void startInternal() throws LifecycleException
    {
        super.startInternal();
    }
}
