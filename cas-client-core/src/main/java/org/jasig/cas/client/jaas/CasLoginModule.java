/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.jaas;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.security.acl.Group;
import java.util.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.authentication.SimpleGroup;
import org.jasig.cas.client.authentication.SimplePrincipal;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;

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
 * @version $Revision$
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
   
    /** Logger instance */
    protected final Log log = LogFactory.getLog(getClass());
    
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
   
    /** Login module shared state */
    protected Map sharedState;
   
    /** Roles to be added to all authenticated principals by default */
    protected String[] defaultRoles;
   
    /** Names of attributes in the CAS assertion that should be used for role data */
    protected Set roleAttributeNames = new HashSet();
   
    /** Name of JAAS Group containing caller principal */
    protected String principalGroupName = DEFAULT_PRINCIPAL_GROUP_NAME;
   
    /** Name of JAAS Group containing role data */
    protected String roleGroupName = DEFAULT_ROLE_GROUP_NAME;


    /**
     * Initializes the CAS login module.
     * @param subject Authentication subject.
     * @param handler Callback handler.
     * @param state Shared state map.
     * @param options Login module options.  The following are supported:
     * <ul>
     *  <li>service - CAS service URL used for service ticket validation</li>
     *  <li>ticketValidatorClass - fully-qualified class name of service ticket validator component</li>
     *  <li>defaultRoles (optional) - comma-delimited list of roles to be added to all authenticated principals</li>
     *  <li>roleAttributeNames (optional) - comma-delimited list of attributes in the CAS assertion that contain role data</li>
     *  <li>principalGroupName (optional) - name of JAAS Group containing caller principal</li>
     *  <li>roleGroupName (optional) - name of JAAS Group containing role data</li>
     * </ul>
     */
    public void initialize(final Subject subject, final CallbackHandler handler, final Map state, final Map options) {
        this.callbackHandler = handler;
        this.subject = subject;
        this.sharedState = state;
      
        String ticketValidatorClass = null;
        final Iterator iter = options.keySet().iterator();
        while (iter.hasNext()) {
            final Object key = iter.next();
            log.trace("Processing option " + key);
            if ("service".equals(key)) {
                this.service = (String) options.get(key);
                log.debug("Set service=" + this.service);
            } else if ("ticketValidatorClass".equals(key)) {
                ticketValidatorClass = (String) options.get(key);
                log.debug("Set ticketValidatorClass=" + ticketValidatorClass);
            } else if ("defaultRoles".equals(key)) {
                final String roles = (String) options.get(key);
                log.trace("Got defaultRoles value " + roles);
                this.defaultRoles = roles.split(",\\s*");
                log.debug("Set defaultRoles=" + Arrays.asList(this.defaultRoles));
            } else if ("roleAttributeNames".equals(key)) {
                final String attrNames = (String) options.get(key);
                log.trace("Got roleAttributeNames value " + attrNames);
                final String[] attributes = attrNames.split(",\\s*");
                this.roleAttributeNames.addAll(Arrays.asList(attributes));
                log.debug("Set roleAttributeNames=" + this.roleAttributeNames);
            } else if ("principalGroupName".equals(key)) {
                this.principalGroupName = (String) options.get(key);
                log.debug("Set principalGroupName=" + this.principalGroupName);
            } else if ("roleGroupName".equals(key)) {
                this.roleGroupName = (String) options.get(key);
                log.debug("Set roleGroupName=" + this.roleGroupName);
            }
        }

        CommonUtils.assertNotNull(ticketValidatorClass, "ticketValidatorClass is required.");
        this.ticketValidator = createTicketValidator(ticketValidatorClass, options);
    }

    public boolean login() throws LoginException {
        log.debug("Performing login.");
        final NameCallback serviceCallback = new NameCallback("service");
        final PasswordCallback ticketCallback = new PasswordCallback("ticket", false);
        try {
            this.callbackHandler.handle(new Callback[] { ticketCallback, serviceCallback });
        } catch (final IOException e) {
            log.info("Login failed due to IO exception in callback handler: " + e);
            throw new LoginException("IO exception in callback handler: " + e);
        } catch (final UnsupportedCallbackException e) {
            log.info("Login failed due to unsupported callback: " + e);
            throw new LoginException("Callback handler does not support PasswordCallback and TextInputCallback.");
        }
        if (ticketCallback.getPassword() != null) {
            final String ticket = new String(ticketCallback.getPassword());
            final String service = CommonUtils.isNotBlank(serviceCallback.getName()) ? serviceCallback.getName() : this.service;

            if (CommonUtils.isBlank(service)) {
	            log.info("Login failed because required CAS service parameter not provided.");
                throw new LoginException("Neither login module nor callback handler provided required service parameter.");
            }
	        try {
	            log.debug("Attempting ticket validation with service=" + service + " and ticket=" + ticket);
		        this.assertion = this.ticketValidator.validate(ticket, service);
	        } catch (final Exception e) {
	            log.info("Login failed due to CAS ticket validation failure: " + e);
	            throw new LoginException("CAS ticket validation failed: " + e);
	        }
        } else {
            log.info("Login failed because callback handler did not provide CAS ticket.");
            throw new LoginException("Callback handler did not provide CAS ticket.");
        }
        log.info("Login succeeded.");
        return true;
    }

    public boolean abort() throws LoginException {
        if (this.assertion != null) {
            this.assertion = null;
            return true;
        }
        return false;
    }

    public boolean commit() throws LoginException {
        if (this.assertion != null) {
            final AssertionPrincipal casPrincipal = new AssertionPrincipal(this.assertion.getPrincipal().getName(), this.assertion);
            this.subject.getPrincipals().add(casPrincipal);

            // Add group containing principal as sole member
            // Supports JBoss JAAS use case
            final Group principalGroup = new SimpleGroup(this.principalGroupName);
            principalGroup.addMember(casPrincipal);
            this.subject.getPrincipals().add(principalGroup);
            
            // Add group principal containing role data
            final Group roleGroup = new SimpleGroup(this.roleGroupName);
            for (int i = 0; i < defaultRoles.length; i++) {
                roleGroup.addMember(new SimplePrincipal(defaultRoles[i]));
            }
            final Map attributes = this.assertion.getPrincipal().getAttributes();
            final Iterator nameIterator = attributes.keySet().iterator();
            while (nameIterator.hasNext()) {
                final Object key = nameIterator.next();
                if (this.roleAttributeNames.contains(key)) {
                    // Attribute value is Object if singular or Collection if plural
                    final Object value = attributes.get(key);
                    if (value instanceof Collection) {
                        final Iterator valueIterator = ((Collection) value).iterator();
                        while (valueIterator.hasNext()) {
                            roleGroup.addMember(new SimplePrincipal(valueIterator.next().toString()));
                        }
                    } else {
                        roleGroup.addMember(new SimplePrincipal(value.toString()));
                    }
                }
            }
            this.subject.getPrincipals().add(roleGroup);
            
            // Place principal name in shared state for downstream JAAS modules (module chaining use case)
            this.sharedState.put(LOGIN_NAME, casPrincipal.getName());
            
            if (log.isDebugEnabled()) {
                log.debug("Created JAAS subject with principals: " + subject.getPrincipals());
            }
            return true;
        }
        return false;
    }

    public boolean logout() throws LoginException {
        if (this.assertion != null) {
	        log.debug("Performing logout.");
            this.subject.getPrincipals().remove(this.assertion.getPrincipal());
            // Remove all SimpleGroup principals
            final Iterator iter = this.subject.getPrincipals().iterator();
            while (iter.hasNext()) {
                if (iter.next() instanceof SimpleGroup) {
                    iter.remove();
                }
            }
            this.assertion = null;
            log.info("Logout succeeded.");
            return true;
        }
        return false;
    }


    /**
     * Creates a {@link TicketValidator} instance from a class name and map of property name/value pairs.
     * @param className Fully-qualified name of {@link TicketValidator} concrete class.
     * @param propertyMap Map of property name/value pairs to set on validator instance.
     * @return Ticket validator with properties set.
     */
    private TicketValidator createTicketValidator(final String className, final Map propertyMap) {
        CommonUtils.assertTrue(propertyMap.containsKey("casServerUrlPrefix"), "Required property casServerUrlPrefix not found.");

        final Class validatorClass = ReflectUtils.loadClass(className);
        final TicketValidator validator = (TicketValidator) ReflectUtils.newInstance(validatorClass, new Object[] {propertyMap.get("casServerUrlPrefix")});

        try {
            final BeanInfo info = Introspector.getBeanInfo(validatorClass);
            final Iterator iter = propertyMap.keySet().iterator();
            while (iter.hasNext()) {
                final String property = (String) iter.next();
                if (!"casServerUrlPrefix".equals(property)) {
                    log.debug("Attempting to set TicketValidator property " + property);
                    final String value = (String) propertyMap.get(property);
                    final PropertyDescriptor pd = ReflectUtils.getPropertyDescriptor(info, property);
                    if (pd != null) {
	                    ReflectUtils.setProperty(property, convertIfNecessary(pd, value), validator, info);
	                    log.debug("Set " + property + "=" + value);
                    } else {
                        log.warn("Cannot find property " + property + " on " + className);
                    }
                }
            }
        } catch (final IntrospectionException e) {
            throw new RuntimeException("Error getting bean info for " + validatorClass);
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
            throw new IllegalArgumentException(
                    "No conversion strategy exists for property " + pd.getName()
                        + " of type " + pd.getPropertyType());
        }
    }
}
