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
 * A pattern matcher that produces a successful match if the pattern
 * specified matches the given url exactly and equally.
 * 
 * @author Misagh Moayyed
 * @since 3.3.1
 */
public final class ExactUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {

    private String pattern;
    
    public boolean matches(final String url) {
        return url.equals(this.pattern);
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

}
