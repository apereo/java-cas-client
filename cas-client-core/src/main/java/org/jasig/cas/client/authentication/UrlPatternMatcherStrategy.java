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
package org.jasig.cas.client.authentication;
/**
 * Defines an abstraction by which request urls can be matches against a given pattern.
 *
 * @author Misagh Moayyed
 * @since 3.3.1
 */
public interface UrlPatternMatcherStrategy {
    /**
     * Execute the match between the given pattern and the url
     * @param url the request url typically with query strings included
     * @return true if match is successful
     */
    boolean matches(final String url);
    
    /**
     * The pattern against which the url is compared
     * @param pattern
     */
    void setPattern(final String pattern);
}
