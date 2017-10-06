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

import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CasRealm} implementation with users and roles stored in relational
 * tables accessible through a javax.sql.DataSource. This implementation of
 * Realm has the advantage that users and roles can be dynamically defined at
 * runtime updating the database tables.
 * 
 * <br/>
 * The tables must have the minimal characteristics listed below. Chances are
 * that the DB might already have similar tables: just make sure it has the
 * required columns. The names of the tables and columns are configurable via
 * the attributes of the {@code<Realm>} tag.
 * 
 * <pre>
 * {@code
 * create table USERS (
 *    USERID        varchar2(8)   not null,
 *    ENABLED       number(1)     default 1 not null check (ENABLED in (0,1)),
 *    constraint    PK_USERS      primary key (USERID)
 * );
 *
 * create table USER_ROLES (
 *    USERID        varchar2(8)   not null,
 *    ROLE          varchar2(10)  not null,
 *    constraint    PK_USER_ROLES primary key (USERID, ROLE)
 * );
 * }
 * </pre>
 * 
 * The {@code<Realm>} tag must include all these attributes:
 * <ul>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#jndiName
 * jndiName}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTable
 * usersTable}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTableUidColumn
 * usersTableUidColumn}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTableEnabledColumn
 * usersTableEnabledColumn}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTable
 * rolesTable}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableUidColumn
 * rolesTableUidColumn}</li>
 * <li>{@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableRoleColumn
 * rolesTableRoleColumn}</li>
 * </ul>
 * 
 * <p>
 * This class is used in conjunction with the specific Realm implementation for
 * the deployed Tomcat and it is not meant to be used directly in the
 * {@code<Realm>} configuration.
 * </p>
 *
 * <p>
 * This is as an example of a complete {@code<Realm>} tag, based on the tables
 * above, that must be placed in the context descriptor (context.xml). It is
 * assumed that the {@code<Resource>} tag is already defined, because it is not
 * subject of explanation here.
 * </p>
 * 
 * <pre>
 * {@code
 * <Realm
 *    className="org.jasig.cas.client.tomcat.v7.SQLDataSourceCasRealm"
 *    jndiName="java:comp/env/jdbc/theDbDataSourceName"
 *    usersTable="USERS"
 *    usersTableUidColumn="USERID"
 *    usersTableEnabledColumn="ENABLED"
 *    rolesTable="USER_ROLES"
 *    rolesTableUidColumn="USERID"
 *    rolesTableRoleColumn="ROLE"
 * />
 * }
 * </pre>
 *
 * <p>
 * User authentication succeeds if the name of the given principal exists in the
 * users table and the enabled column is set to 1.
 * </p>
 *
 * @author gluppi (at) comune.modena.it October 2017
 * @version $Revision$
 * @since 3.1.12
 */
public class SQLDataSourceCasRealmDelegate implements CasRealm
{
    /** Log instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Qualified name of the jndi resource pointing to the java.sql.DataSource
     * containing users and roles. It must be in the form
     * "java:comp/env/jdbc/dataSourceName".
     */
    private String       jndiName;

    /** Name of the table containing users' roles. */
    private String       rolesTable;

    /** Name of the role column in the roles' table. */
    private String       rolesTableRoleColumn;

    /** Name of the userid column in the roles' table. */
    private String       rolesTableUidColumn;

    /** Name of the database table containing the application users. */
    private String       usersTable;

    /**
     * Name of the enabled flag column in the users' table. For the user to be
     * enabled, this value must be equal to 1.
     */
    private String       usersTableEnabledColumn;

    /** Name of the userid column in the users' table. */
    private String       usersTableUidColumn;

    /**
     * {@inheritDoc}<br/>
     * The principal name is searched in the table defined by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTable
     * usersTable} attribute of the {@code<Realm>} tag. The name must match
     * exactly the value stored in the column defined by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTableUidColumn
     * usersTableUidColumn} attribute. The search is case sensitive and spaces
     * are not trimmed.<br/>
     * If a row is found and the value of the column defined by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#usersTableEnabledColumn
     * usersTableEnabledColumn} attribute is equal to 1, then the authentication
     * is successful and a new Principal is returned.
     * 
     * @return A new Principal correspoding the the argument supplied if a match
     *         is found and the enabled flag is set to 1; {@code null} if no
     *         match is found or a match is found and the enabled flag is
     *         different from 1.
     */
    public Principal authenticate(final Principal principal)
    {
        // @formatter:off
        String sqlTemplate = 
           "SELECT ${usersTableEnabledColumn} " +
           "FROM   ${usersTable} " +
           "WHERE  ${usersTableUidColumn} = ''{0}''";
        // @formatter:on

        String sql = replaceVariables(sqlTemplate);
        sql = MessageFormat.format(sql, new Object[] { principal.getName() });

        boolean userEnabled = readUser(sql);

        return (userEnabled) ? principal : null;
    }

    /**
     * @return A java.sql.Connection obtained from the javax.sql.DataSource
     *         defined in the
     *         {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#jndiName
     *         jndiName} attribute of the {@code<Realm>} tag.
     * 
     * @throws java.lang.Error
     *             Upon any java.sql.SQLException when trying to get a
     *             connection from the datasource.
     * 
     * @throws java.lang.IllegalArgumentException
     *             If the datasource is {@code null}.
     */
    public Connection getConnection()
    {
        DataSource datasource = getDataSource();
        CommonUtils.assertNotNull(datasource, "cannot get a connection from a null datasource.");

        Connection conn = null;

        try
        {
            logger.debug("Opening connection to database...");
            conn = datasource.getConnection();
        }
        catch (SQLException ex)
        {
            throw new Error("Cannot open connection.", ex);
        }

        return conn;
    }

    /**
     * @return A javax.sql.DataSource instance pointing to the resource defined
     *         in the
     *         {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#jndiName
     *         jndiName} attribute of the {@code<Realm>} tag. The resource is
     *         searched in the javax.naming.InitialContext of the webapp.
     *
     * @throws java.lang.Error
     *             Upon javax.naming.NamingException raised when accessing the
     *             javax.naming.InitialContext.
     * 
     * @throws java.lang.IllegalArgumentException
     *             If the value of the
     *             {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#jndiName
     *             jndiName} attribute is {@code null}.
     */
    public DataSource getDataSource()
    {
        CommonUtils.assertNotNull(jndiName, "jndiName attribute not set.");

        DataSource datasource = null;
        Context initCtx = null;
        try
        {
            logger.debug("Initializing jndi context");
            initCtx = new InitialContext();
            logger.debug("Searching datasource in jndi context");
            datasource = (DataSource) initCtx.lookup(jndiName);
        }
        catch (NamingException ex)
        {
            logger.error("Error in jndi initialContext", ex);
            throw new Error(jndiName + " resource non found.", ex);
        }
        finally
        {
            if (initCtx != null)
            {
                try
                {
                    initCtx.close();
                }
                catch (NamingException ex)
                {
                    logger.error("Error closing jndi initial context", ex);
                }
            }
        }

        return datasource;
    }

    /**
     * {@inheritDoc}<br/>
     * The roles are read from the table defined in the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTable
     * rolesTable} attribute of the {@code<Realm>} tag. The roles returned are
     * the values stored in the column defined by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableRoleColumn
     * rolesTableRoleColumn} attribute for the rows whose column defined in the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableUidColumn
     * rolesTableUidColumn} attribute match exactly. The search is case
     * sensitive and spaces are not trimmed.
     */
    public String[] getRoles(final Principal principal)
    {
        // @formatter:off
        String sqlTemplate =
           "SELECT ${rolesTableRoleColumn} " + 
           "FROM   ${rolesTable} " +
           "WHERE  ${rolesTableUidColumn} = ''{0}''";
        // @formatter:on

        String sql = replaceVariables(sqlTemplate);
        sql = MessageFormat.format(sql, new Object[] { principal.getName() });

        final Set<String> roleSet = readRoles(sql);
        return roleSet.toArray(new String[roleSet.size()]);
    }

    /**
     * {@inheritDoc}<br/>
     * For the principal to have the desired role, a row in the table specified
     * by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTable
     * rolesTable} attribute must exist with the exact matching values in the
     * columns defined by the
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableUidColumn
     * rolesTableUidColumn}. and
     * {@link org.jasig.cas.client.tomcat.SQLDataSourceCasRealmDelegate#rolesTableUidColumn
     * rolesTableRoleColumn} attributes of the {@code<Realm>} tag.<br/>
     * A role argument with the value of "*" assumes that the principal is
     * granted any role and no search is performed on the database.
     */
    public boolean hasRole(final Principal principal, final String role)
    {
        if (role.equals("*"))
        {
            return true;
        }

        // @formatter:off
        String sqlTemplate = 
           "SELECT ${rolesTableRoleColumn} " + 
           "FROM   ${rolesTable} " +
           "WHERE  ${rolesTableUidColumn} = ''{0}'' " + 
           "AND    ${rolesTableRoleColumn} = ''{1}''";
        // @formatter:on

        String sql = replaceVariables(sqlTemplate);
        sql = MessageFormat.format(sql, new Object[] { principal.getName(), role });

        final Set<String> roleSet = readRoles(sql);
        return roleSet != null && roleSet.contains(role);
    }

    /**
     * Sets the qualified name of the javax.sql.DataSource. It must be in the
     * form "java:comp/env/jdbc/dataSourceName".
     * 
     * @param jndiName
     *            Name of the datasource containing the tables with
     *            username/role data. Edge spaces are trimmed from the string.
     */
    public void setJndiName(final String jndiName)
    {
        this.jndiName = (jndiName == null || jndiName.trim().isEmpty()) ? null : jndiName.trim();
    }

    /**
     * Sets the name of the database table containing users/roles value pairs.
     * 
     * @param rolesTable
     *            Name of the table. Edge spaces are trimmed from the string.
     */
    public void setRolesTable(final String rolesTable)
    {
        this.rolesTable = (rolesTable == null || rolesTable.trim().isEmpty()) ? null : rolesTable.trim();
    }

    /**
     * Sets the name of the column containing the role names.
     * 
     * @param rolesTableRoleColumn
     *            Name of the column. Edge spaces are trimmed from the string.
     */
    public void setRolesTableRoleColumn(final String rolesTableRoleColumn)
    {
        this.rolesTableRoleColumn = (rolesTableRoleColumn == null || rolesTableRoleColumn.trim().isEmpty()) ? null
                : rolesTableRoleColumn.trim();
    }

    /**
     * Sets the name of the column containing the userids.
     * 
     * @param rolesTableUidColumn
     *            Name of the columns. Edge spaces are trimmed from the string.
     */
    public void setRolesTableUidColumn(final String rolesTableUidColumn)
    {
        this.rolesTableUidColumn = (rolesTableUidColumn == null || rolesTableUidColumn.trim().isEmpty()) ? null
                : rolesTableUidColumn.trim();
    }

    /**
     * Sets the name of the users table.
     * 
     * @param usersTable
     *            Name of the table. Edge spaces are trimmed from the string.
     */
    public void setUsersTable(String usersTable)
    {
        this.usersTable = (usersTable == null || usersTable.trim().isEmpty()) ? null : usersTable.trim();
    }

    /**
     * Sets the name of the column in the users table of the enabled flag.
     * 
     * @param usersTableEnabledColumn
     *            Name of the column. Edge spaces are trimmed from the string.
     */
    public void setUsersTableEnabledColumn(String usersTableEnabledColumn)
    {
        this.usersTableEnabledColumn = (usersTableEnabledColumn == null || usersTableEnabledColumn.trim().isEmpty())
                ? null : usersTableEnabledColumn.trim();
    }

    /**
     * Sets the name of the userid column in the users table.
     * 
     * @param usersTableUidColumn
     *            Name of the column. Edge spaces are trimmed from the string.
     */
    public void setUsersTableUidColumn(String usersTableUidColumn)
    {
        this.usersTableUidColumn = (usersTableUidColumn == null || usersTableUidColumn.trim().isEmpty()) ? null
                : usersTableUidColumn.trim();
    }

    /**
     * Executes the specified query on the database for retrieving the users'
     * roles.
     * 
     * @param sqlStatement
     *            The query to run.
     * 
     * @return A java.util.Set of String with the role names, or an ampty Set if
     *         there's no matching user.
     */
    private Set<String> readRoles(String sqlStatement)
    {
        Set<String> roles = new HashSet<String>();

        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            conn = getConnection();
            logger.debug("Creating statement");
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            logger.debug("Executing sql statement: " + sqlStatement);
            resultSet = statement.executeQuery(sqlStatement);

            int rows = resultSet.last() ? resultSet.getRow() : 0;
            logger.info(rows + " roles found.");

            resultSet.beforeFirst();
            while (resultSet.next())
            {
                logger.debug("Reading next role.");
                roles.add(resultSet.getString(1));
            }
        }
        catch (SQLException ex)
        {
            logger.error("Error quering the database.", ex);
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    logger.debug("Closing resultset.");
                    resultSet.close();
                }
                if (statement != null)
                {
                    logger.debug("Closing statement.");
                    statement.close();
                }
                if (conn != null)
                {
                    logger.debug("Closing connection.");
                    conn.close();
                }
            }
            catch (SQLException ex)
            {
                logger.error("Error disconnecting from the database.", ex);
            }
        }

        return roles;
    }

    /**
     * Executes the specified query to determine if a users exists and has the
     * enabled flag set to 1.
     * 
     * @param sqlStatement
     *            The query to run.
     * 
     * @return True if the user exists and is enabled.
     */
    private boolean readUser(String sqlStatement)
    {
        boolean isEnabled = false;

        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            conn = getConnection();
            logger.debug("Creating statement");
            statement = conn.createStatement();

            logger.debug("Executing sql statement: " + sqlStatement);
            resultSet = statement.executeQuery(sqlStatement);

            if (resultSet.next())
            {
                isEnabled = resultSet.getBoolean(usersTableEnabledColumn);
                logger.debug(usersTableEnabledColumn + "=" + isEnabled);
            }
            else
            {
                logger.debug("No results for: " + sqlStatement);
            }
        }
        catch (SQLException ex)
        {
            logger.error("Error quering the database.", ex);
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    logger.debug("Closing resultset.");
                    resultSet.close();
                }
                if (statement != null)
                {
                    logger.debug("Closing statement.");
                    statement.close();
                }
                if (conn != null)
                {
                    logger.debug("Closing connection.");
                    conn.close();
                }
            }
            catch (SQLException ex)
            {
                logger.error("Error disconnecting from the database.", ex);
            }
        }

        return isEnabled;
    }

    /**
     * Changes the placeholder variables with the custom table names and columns
     * that have been defined by the attributes of the {@code<Realm>} tag.
     * 
     * @param sqlTemplate
     *            A query where table names end column names are represented by
     *            placeholders.
     * 
     * @return A query with custom table and column names.
     */
    private String replaceVariables(String sqlTemplate)
    {
        String result = null;

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("usersTable", this.usersTable);
        parameters.put("usersTableUidColumn", this.usersTableUidColumn);
        parameters.put("usersTableEnabledColumn", this.usersTableEnabledColumn);
        parameters.put("rolesTable", this.rolesTable);
        parameters.put("rolesTableUidColumn", this.rolesTableUidColumn);
        parameters.put("rolesTableRoleColumn", this.rolesTableRoleColumn);

        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(sqlTemplate);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            if (parameters.containsKey(matcher.group(1)))
            {
                String replacement = parameters.get(matcher.group(1));
                matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
            }
        }
        matcher.appendTail(buffer);
        result = buffer.toString();

        return result;
    }
}
