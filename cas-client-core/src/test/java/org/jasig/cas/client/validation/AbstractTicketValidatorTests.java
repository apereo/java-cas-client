/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import junit.framework.TestCase;

/**
 * Base class for all TicketValidator tests to inherit from.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractTicketValidatorTests extends TestCase {

    protected static final String CONST_CAS_SERVER_URL = "http://localhost:8085/";

    protected static final String CONST_USERNAME = "username";
}
