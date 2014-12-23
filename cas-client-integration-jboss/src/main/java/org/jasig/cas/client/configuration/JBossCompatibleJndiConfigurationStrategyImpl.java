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
package org.jasig.cas.client.configuration;

/**
 * Simple extension to the {@link org.jasig.cas.client.configuration.JndiConfigurationStrategyImpl} to provide a JBoss 7 compatible prefix.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class JBossCompatibleJndiConfigurationStrategyImpl extends JndiConfigurationStrategyImpl {

    private static final String ENVIRONMENT_PREFIX = "java:/comp/env/cas/";

    public JBossCompatibleJndiConfigurationStrategyImpl() {
        super(ENVIRONMENT_PREFIX);
    }
}
