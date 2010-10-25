/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
  $Id$

  Copyright (C) 2008-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.jasig.cas.client.ssl;

import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Validates an SSL peer's hostname using a regular expression that a candidate
 * host must match in order to be verified.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class RegexHostnameVerifier implements HostnameVerifier {
    /** Allowed hostname pattern */
    private Pattern pattern;
    
    
    /**
     * Creates a new instance using the given regular expression.
     * 
     * @param regex Regular expression describing allowed hosts.
     */
    public RegexHostnameVerifier(final String regex) {
        this.pattern = Pattern.compile(regex);
    }


    /** {@inheritDoc} */
    public boolean verify(final String hostname, final SSLSession session) {
        return pattern.matcher(hostname).matches();
    }
}
