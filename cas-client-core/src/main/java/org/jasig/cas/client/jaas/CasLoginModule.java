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
package org.jasig.cas.client.jaas;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.jasig.cas.client.authentication.SimpleGroup;
import org.jasig.cas.client.authentication.SimplePrincipal;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAAS login module that delegates to a CAS {@link TicketValidator} component
 * for authentication, and on success populates a {@link Subject} with principal
 * data including NetID and principal attributes.  The module expects to be provided
 * with the CAS ticket (required) and service (optional) parameters via
 * {@link PasswordCallback} and {@link NameCallback}, respectively, by the
 * {@link CallbackHandler} that is part of the JAAS framework in which the servlet 
 * resides.
 *
 * <p>
 * Module configuration options:
 * <ul>
 * <li>ticketValidatorClass - Fully-qualified class name of CAS ticket validator class.</li>
 * <li>casServerUrlPrefix - URL to root of CAS Web application context.</li>
 * <li>service (optional) - CAS service parameter that may be overridden by callback handler.
 * NOTE: service must be specified by at least one component such that it is available at
 * service ticket validation time</li>
 * <li>defaultRoles (optional) - Comma-delimited list of static roles applied to all
 * authenticated principals.</li>
 * <li>roleAttributeNames (optional) - Comma-delimited list of attribute names that describe
 * role data delivered to CAS in the service-ticket validation response that should be
 * applied to the current authenticated principal.</li>
 * <li>principalGroupName (optional) - The name of a group principal containing the
 * primary principal name of the current JAAS subject.  The default value is "CallerPrincipal",
 * which is suitable for JBoss.</li>
 * <li>roleGroupName (optional) - The name of a group principal containing all role data.
 * The default value is "Roles", which is suitable for JBoss.</li>
 * <li>cacheAssertions (optional) - Flag to enable assertion caching.  This may be needed
 * for JAAS providers that attempt to periodically reauthenticate to renew principal.
 * Since CAS tickets are one-time-use, a cached assertion must be provided on reauthentication.</li>
 * <li>cacheTimeout (optional) - Assertion cache timeout in minutes.</li>
 * <li>cacheTimeoutUnit (optional) - Assertion cache timeout unit.  Must be one of {@link TimeUnit} enumeration
 *     names, e.g. DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS. Default unit is MINUTES.</li>
 * </ul>
 *
 * <p>
 * Module options not explicitly listed above are treated as attributes of the
 * given ticket validator class, e.g. <code>tolerance</code> in the following example.
 *
 * <p>
 * Sample jaas.config file entry for this module:
 * <pre>
 * cas {
 *   org.jasig.cas.client.jaas.CasLoginModule required
 *     ticketValidatorClass="org.jasig.cas.client.validation.Saml11TicketValidator"
 *     casServerUrlPrefix="https://cas.example.com/cas"
 *     tolerance="20000"
 *     service="https://webapp.example.com/webapp"
 *     defaultRoles="admin,operator"
 *     roleAttributeNames="memberOf,eduPersonAffiliation"
 *     principalGroupName="CallerPrincipal"
 *     roleGroupName="Roles";
 * }
 * </pre>
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.11
 *
 */
public class CasLoginModule implements LoginModule {
    /** Constant for login name stored in shared state. */
    public static final String LOGIN_NAME = "javax.security.auth.login.name";

    /**
     * Default group name for storing caller principal.
     * The default value supports JBoss, but is configurable to hopefully
     * support other JEE containers.
     */
    public static final String DEFAULT_PRINCIPAL_GROUP_NAME = "CallerPrincipal";

    /**
     * Default group name for storing role membership data.
     * The default value supports JBoss, but is configurable to hopefully
     * support other JEE containers.
     */
    public static final String DEFAULT_ROLE_GROUP_NAME = "Roles";

    /**
     * Default assertion cache timeout in minutes.  Default is 8 hours.
     */
    public static final int DEFAULT_CACHE_TIMEOUT = 480;

    /** Default assertion cache timeout unit is minutes. */
    public static final TimeUnit DEFAULT_CACHE_TIMEOUT_UNIT = TimeUnit.MINUTES;

    /**
     * Stores mapping of ticket to assertion to support JAAS providers that
     * attempt to periodically re-authenticate to renew principal.  Since
     * CAS tickets are one-time-use, a cached assertion must be provided on
     * re-authentication.
     */
    protected static final Map<TicketCredential, Assertion> ASSERTION_CACHE = new HashMap<TicketCredential, Assertion>();

    /** Logger instance */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** JAAS authentication subject */
    protected Subject subject;

    /** JAAS callback handler */
    protected CallbackHandler callbackHandler;

    /** CAS ticket validator */
    protected TicketValidator ticketValidator;

    /** CAS service parameter used if no service is provided via TextCallback on login */
    protected String service;

    /** CAS assertion */
    protected Assertion assertion;

    /** CAS ticket credential */
    protected TicketCredential ticket;

    /** Login module shared state */
    protected Map<String, Object> sharedState;

    /** Roles to be added to all authenticated principals by default */
    protected String[] defaultRoles;

    /** Names of attributes in the CAS assertion that should be used for role data */
    protected Set<String> roleAttributeNames = new HashSet<String>();

    /** Name of JAAS Group containing caller principal */
    protected String principalGroupName = DEFAULT_PRINCIPAL_GROUP_NAME;

    /** Name of JAAS Group containing role data */
    protected String roleGroupName = DEFAULT_ROLE_GROUP_NAME;

    /** Enables or disable assertion caching */
    protected boolean cacheAssertions;

    /** Assertion cache timeout in minutes */
    protected int cacheTimeout = DEFAULT_CACHE_TIMEOUT;

    /** Units of cache timeout. */
    protected TimeUnit cacheTimeoutUnit = DEFAULT_CACHE_TIMEOUT_UNIT;

    /**
     * Initializes the CAS login module.
     *
     * @param subject Authentication subject.
     * @param handler Callback handler.
     * @param state Shared state map.
     * @param options Login module options.  The following are supported:
     * <ul>
     *  <li>service - CAS service URL used for service ticket validation.</li>
     *  <li>ticketValidatorClass - fully-qualified class name of service ticket validator component.</li>
     *  <li>defaultRoles (optional) - comma-delimited list of roles to be added to all authenticated principals.</li>
     *  <li>roleAttributeNames (optional) - comma-delimited list of attributes in the CAS assertion that contain role data.</li>
     *  <li>principalGroupName (optional) - name of JAAS Group containing caller principal.</li>
     *  <li>roleGroupName (optional) - name of JAAS Group containing role data</li>
     *  <li>cacheAssertions (optional) - whether or not to cache assertions.
     *      Some JAAS providers attempt to reauthenticate users after an indeterminate
     *      period of time.  Since the credential used for authentication is a CAS ticket,
     *      which by default are single use, reauthentication fails.  Assertion caching addresses this
     *      behavior.</li>
     *  <li>cacheTimeout (optional) - assertion cache timeout in minutes.</li>
     *  <li>cacheTimeoutUnit (optional) - Assertion cache timeout unit.  Must be one of {@link TimeUnit} enumeration
     *      names, e.g. DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS. Default unit is MINUTES.</li>
     * </ul>
     */
    public final void initialize(final Subject subject, final CallbackHandler handler, final Map<String, ?> state,
            final Map<String, ?> options) {

        this.assertion = null;
        this.callbackHandler = handler;
        this.subject = subject;
        this.sharedState = (Map<String, Object>) state;
        this.sharedState = new HashMap<String, Object>(state);

        String ticketValidatorClass = null;

        for (final String key : options.keySet()) {
            logger.trace("Processing option {}", key);
            if ("service".equals(key)) {
                this.service = (String) options.get(key);
                logger.debug("Set service={}", this.service);
            } else if ("ticketValidatorClass".equals(key)) {
                ticketValidatorClass = (String) options.get(key);
                logger.debug("Set ticketValidatorClass={}", ticketValidatorClass);
            } else if ("defaultRoles".equals(key)) {
                final String roles = (String) options.get(key);
                logger.trace("Got defaultRoles value {}", roles);
                this.defaultRoles = roles.split(",\\s*");
                logger.debug("Set defaultRoles={}", Arrays.asList(this.defaultRoles));
            } else if ("roleAttributeNames".equals(key)) {
                final String attrNames = (String) options.get(key);
                logger.trace("Got roleAttributeNames value {}", attrNames);
                final String[] attributes = attrNames.split(",\\s*");
                this.roleAttributeNames.addAll(Arrays.asList(attributes));
                logger.debug("Set roleAttributeNames={}", this.roleAttributeNames);
            } else if ("principalGroupName".equals(key)) {
                this.principalGroupName = (String) options.get(key);
                logger.debug("Set principalGroupName={}", this.principalGroupName);
            } else if ("roleGroupName".equals(key)) {
                this.roleGroupName = (String) options.get(key);
                logger.debug("Set roleGroupName={}", this.roleGroupName);
            } else if ("cacheAssertions".equals(key)) {
                this.cacheAssertions = Boolean.parseBoolean((String) options.get(key));
                logger.debug("Set cacheAssertions={}", this.cacheAssertions);
            } else if ("cacheTimeout".equals(key)) {
                this.cacheTimeout = Integer.parseInt((String) options.get(key));
                logger.debug("Set cacheTimeout={}", this.cacheTimeout);
            } else if ("cacheTimeoutUnit".equals(key)) {
                this.cacheTimeoutUnit = Enum.valueOf(TimeUnit.class, (String) options.get(key));
                logger.debug("Set cacheTimeoutUnit={}", this.cacheTimeoutUnit);
            }
        }

        if (this.cacheAssertions) {
            cleanCache();
        }

        CommonUtils.assertNotNull(ticketValidatorClass, "ticketValidatorClass is required.");
        this.ticketValidator = createTicketValidator(ticketValidatorClass, options);
    }

    /**
     * Operations to perform before doing login.
     *
     * @return true if you'd like login to continue, false otherwise.
     */
    protected boolean preLogin() {
        return true;
    }

    /**
     * This occurs after logout is processed.
     *
     * @param result the result from the login attempt.
     */
    protected void postLogin(final boolean result) {
        // template method
    }

    public final boolean login() throws LoginException {
        logger.debug("Performing login.");

        if (!preLogin()) {
            logger.debug("preLogin failed.");
            return false;
        }

        final NameCallback serviceCallback = new NameCallback("service");
        final PasswordCallback ticketCallback = new PasswordCallback("ticket", false);
        boolean result = false;
        try {
            try {
                this.callbackHandler.handle(new Callback[] { ticketCallback, serviceCallback });
            } catch (final IOException e) {
                logger.info("Login failed due to IO exception in callback handler: {}", e);
                throw (LoginException) new LoginException("IO exception in callback handler: " + e).initCause(e);
            } catch (final UnsupportedCallbackException e) {
                logger.info("Login failed due to unsupported callback: {}", e);
                throw (LoginException) new LoginException(
                        "Callback handler does not support PasswordCallback and TextInputCallback.").initCause(e);
            }

            if (ticketCallback.getPassword() != null) {
                this.ticket = new TicketCredential(new String(ticketCallback.getPassword()));
                final String service = CommonUtils.isNotBlank(serviceCallback.getName()) ? serviceCallback.getName()
                        : this.service;

                if (this.cacheAssertions) {
                    this.assertion = ASSERTION_CACHE.get(ticket);
                    if (this.assertion != null) {
                        logger.debug("Assertion found in cache.");
                    }
                }

                if (this.assertion == null) {
                    logger.debug("CAS assertion is null; ticket validation required.");
                    if (CommonUtils.isBlank(service)) {
                        logger.info("Login failed because required CAS service parameter not provided.");
                        throw new LoginException(
                                "Neither login module nor callback handler provided required service parameter.");
                    }
                    try {
                        logger.debug("Attempting ticket validation with service={}  and ticket={}", service,
                                this.ticket);
                        this.assertion = this.ticketValidator.validate(this.ticket.getName(), service);

                    } catch (final Exception e) {
                        logger.info("Login failed due to CAS ticket validation failure: {}", e);
                        throw (LoginException) new LoginException("CAS ticket validation failed: " + e).initCause(e);
                    }
                }
                logger.info("Login succeeded.");
            } else {
                logger.info("Login failed because callback handler did not provide CAS ticket.");
                throw new LoginException("Callback handler did not provide CAS ticket.");
            }
            result = true;
        } finally {
            postLogin(result);
        }
        return result;
    }

    public final boolean abort() throws LoginException {
        if (this.ticket != null) {
            this.ticket = null;
        }
        if (this.assertion != null) {
            this.assertion = null;
        }
        return true;
    }

    /**
     * Operations to perform before doing commit.
     *
     * @return true if you'd like commit to continue, false otherwise.
     */
    protected boolean preCommit() {
        return true;
    }

    /**
     * This occurs after commit is processed.
     *
     * @param result the result from the login attempt.
     */
    protected void postCommit(final boolean result) {
        // template method
    }

    public final boolean commit() throws LoginException {

        if (!preCommit()) {
            return false;
        }
        boolean result = false;
        try {
            if (this.assertion != null) {
                if (this.ticket != null) {
                    this.subject.getPrivateCredentials().add(this.ticket);
                } else {
                    throw new LoginException("Ticket credential not found.");
                }

                final AssertionPrincipal casPrincipal = new AssertionPrincipal(this.assertion.getPrincipal().getName(),
                        this.assertion);
                this.subject.getPrincipals().add(casPrincipal);

                // Add group containing principal as sole member
                // Supports JBoss JAAS use case
                final Group principalGroup = new SimpleGroup(this.principalGroupName);
                principalGroup.addMember(casPrincipal);
                this.subject.getPrincipals().add(principalGroup);

                // Add group principal containing role data
                final Group roleGroup = new SimpleGroup(this.roleGroupName);

                for (final String defaultRole : defaultRoles) {
                    roleGroup.addMember(new SimplePrincipal(defaultRole));
                }

                final Map<String, Object> attributes = this.assertion.getPrincipal().getAttributes();
                for (final String key : attributes.keySet()) {
                    if (this.roleAttributeNames.contains(key)) {
                        // Attribute value is Object if singular or Collection if plural
                        final Object value = attributes.get(key);
                        if (value instanceof Collection<?>) {
                            for (final Object o : (Collection<?>) value) {
                                roleGroup.addMember(new SimplePrincipal(o.toString()));
                            }
                        } else {
                            roleGroup.addMember(new SimplePrincipal(value.toString()));
                        }
                    }
                }
                this.subject.getPrincipals().add(roleGroup);

                // Place principal name in shared state for downstream JAAS modules (module chaining use case)
                this.sharedState.put(LOGIN_NAME, assertion.getPrincipal().getName());

                logger.debug("Created JAAS subject with principals: {}", subject.getPrincipals());

                if (this.cacheAssertions) {
                    logger.debug("Caching assertion for principal {}", this.assertion.getPrincipal());
                    ASSERTION_CACHE.put(this.ticket, this.assertion);
                }
            } else {
                // Login must have failed if there is no assertion defined
                // Need to clean up state
                if (this.ticket != null) {
                    this.ticket = null;
                }
            }
            result = true;
        } finally {
            postCommit(result);
        }
        return result;
    }

    public final boolean logout() throws LoginException {
        logger.debug("Performing logout.");

        if (!preLogout()) {
            return false;
        }

        // Remove cache entry if assertion caching is enabled
        if (this.cacheAssertions) {
            for (final TicketCredential ticket : this.subject.getPrivateCredentials(TicketCredential.class)) {
                logger.debug("Removing cached assertion for {}", ticket);
                ASSERTION_CACHE.remove(ticket);
            }
        }

        // Remove all CAS principals
        removePrincipalsOfType(AssertionPrincipal.class);
        removePrincipalsOfType(SimplePrincipal.class);
        removePrincipalsOfType(SimpleGroup.class);

        // Remove all CAS credentials
        removeCredentialsOfType(TicketCredential.class);

        logger.info("Logout succeeded.");

        postLogout();
        return true;
    }

    /**
     * Happens before logout occurs.
     *
     * @return true if we should continue, false otherwise.
     */
    protected boolean preLogout() {
        return true;
    }

    /**
     * Happens after logout.
     */
    protected void postLogout() {
        // template method
    }

    /**
     * Creates a {@link TicketValidator} instance from a class name and map of property name/value pairs.
     * @param className Fully-qualified name of {@link TicketValidator} concrete class.
     * @param propertyMap Map of property name/value pairs to set on validator instance.
     * @return Ticket validator with properties set.
     */
    private TicketValidator createTicketValidator(final String className, final Map<String, ?> propertyMap) {
        CommonUtils.assertTrue(propertyMap.containsKey("casServerUrlPrefix"),
                "Required property casServerUrlPrefix not found.");

        final Class<TicketValidator> validatorClass = ReflectUtils.loadClass(className);
        final TicketValidator validator = ReflectUtils.newInstance(validatorClass,
                propertyMap.get("casServerUrlPrefix"));

        try {
            final BeanInfo info = Introspector.getBeanInfo(validatorClass);

            for (final String property : propertyMap.keySet()) {
                if (!"casServerUrlPrefix".equals(property)) {
                    logger.debug("Attempting to set TicketValidator property {}", property);
                    final String value = (String) propertyMap.get(property);
                    final PropertyDescriptor pd = ReflectUtils.getPropertyDescriptor(info, property);
                    if (pd != null) {
                        ReflectUtils.setProperty(property, convertIfNecessary(pd, value), validator, info);
                        logger.debug("Set {} = {}", property, value);
                    } else {
                        logger.warn("Cannot find property {} on {}", property, className);
                    }
                }
            }
        } catch (final IntrospectionException e) {
            throw new RuntimeException("Error getting bean info for " + validatorClass, e);
        }

        return validator;
    }

    /**
     * Attempts to do simple type conversion from a string value to the type expected
     * by the given property.
     *
     * Currently only conversion to int, long, and boolean are supported.
     *
     * @param pd Property descriptor of target property to set.
     * @param value Property value as a string.
     * @return Value converted to type expected by property if a conversion strategy exists.
     */
    private static Object convertIfNecessary(final PropertyDescriptor pd, final String value) {
        if (String.class.equals(pd.getPropertyType())) {
            return value;
        } else if (boolean.class.equals(pd.getPropertyType())) {
            return Boolean.valueOf(value);
        } else if (int.class.equals(pd.getPropertyType())) {
            return new Integer(value);
        } else if (long.class.equals(pd.getPropertyType())) {
            return new Long(value);
        } else {
            throw new IllegalArgumentException("No conversion strategy exists for property " + pd.getName()
                    + " of type " + pd.getPropertyType());
        }
    }

    /**
     * Removes all principals of the given type from the JAAS subject.
     * @param clazz Type of principal to remove.
     */
    private void removePrincipalsOfType(final Class<? extends Principal> clazz) {
        this.subject.getPrincipals().removeAll(this.subject.getPrincipals(clazz));
    }

    /**
     * Removes all credentials of the given type from the JAAS subject.
     * @param clazz Type of principal to remove.
     */
    private void removeCredentialsOfType(final Class<? extends Principal> clazz) {
        this.subject.getPrivateCredentials().removeAll(this.subject.getPrivateCredentials(clazz));
    }

    /**
     * Removes expired entries from the assertion cache.
     */
    private void cleanCache() {
        logger.debug("Cleaning assertion cache of size {}", ASSERTION_CACHE.size());
        final Iterator<Map.Entry<TicketCredential, Assertion>> iter = ASSERTION_CACHE.entrySet().iterator();
        final Calendar cutoff = Calendar.getInstance();
        cutoff.setTimeInMillis(System.currentTimeMillis() - this.cacheTimeoutUnit.toMillis(this.cacheTimeout));
        while (iter.hasNext()) {
            final Assertion assertion = iter.next().getValue();
            final Calendar created = Calendar.getInstance();
            created.setTime(assertion.getValidFromDate());
            if (created.before(cutoff)) {
                logger.debug("Removing expired assertion for principal {}", assertion.getPrincipal());
                iter.remove();
            }
        }
    }
}
