/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.integration.uportal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.portal.security.InitialSecurityContextFactory;
import org.jasig.portal.spring.PortalApplicationContextFacade;

/**
 * Abstract implementation of a SecurityContextFactory that can load all the dependences if a
 * CasSecurityCcontext.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractCasSecurityContextFactory extends
        InitialSecurityContextFactory {


    /**
     * Spring Bean ID for the Ticket Validator.
     */
    public static final String CONST_CAS_TICKET_VALIDATOR = "casTicketValidator";

    /**
     * Spring Bean ID for the Proxy Retriever.
     */
    public static final String CONST_CAS_PROXY_RETRIEVER = "casProxyRetriever";

    /**
     * Spring Bean ID for the Service.
     */
    public static final String CONST_CAS_SERVICE = "casService";

    /**
     * Spring Bean ID for the ProxyGrantingTicketStorage.
     */
    public static final String CONST_CAS_PROXY_GRANTING_TICKET_STORAGE = "casProxyGrantingTicketStorage";

    /**
     * Instance of Commons Logging.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * The Ticket Validator referenced by the constant
     * <code>CONST_CAS_TICKET_VALIDATOR</code>.
     */
    protected final TicketValidator ticketValidator;

    /**
     * The ProxyRetriever referenced by the constant
     * <code>CONST_CAS_PROXY_RETRIEVER</code>.
     */
    protected final ProxyRetriever proxyRetriever;

    /**
     * The Service referenced by the constant <code>CONST_CAS_SERVICE</code>.
     */
    protected final Service service;

    /**
     * Default constructor retrieves and caches results from looking up entries
     * in the PortalApplicationContext for the Ticket Validator, Proxy Retriever
     * and Service.
     */
    public AbstractCasSecurityContextFactory() {
        this.ticketValidator = (TicketValidator) PortalApplicationContextFacade
                .getPortalApplicationContext().getBean(CONST_CAS_TICKET_VALIDATOR);
        if (PortalApplicationContextFacade.getPortalApplicationContext()
                .containsBean(CONST_CAS_PROXY_RETRIEVER)) {
            this.proxyRetriever = (ProxyRetriever) PortalApplicationContextFacade
                    .getPortalApplicationContext().getBean(
                    CONST_CAS_PROXY_RETRIEVER);
        } else {
            this.proxyRetriever = null;
            log
                    .warn("No Proxy Retriever found in PortalApplicationFacade.  No Proxying capabilities will be provided by CAS.");
        }
        this.service = (Service) PortalApplicationContextFacade
                .getPortalApplicationContext().getBean(CONST_CAS_SERVICE);
    }

}
