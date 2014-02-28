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
package org.jasig.cas.client.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.*;

public class ConfigurationFilterTests {

    private static final String CONFIG_FILE_PATH = "src/test/resources/client.properties";
    
    @Test
    public void testGetValueConfigurationFile() {
        final TestConfigurationFilter filter = new TestConfigurationFilter();
        final MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(AbstractConfigurationFilter.PARAM_NAME_CONFIG_FILE, CONFIG_FILE_PATH);
        
        assertEquals(filter.getPropertyFromInitParams(filterConfig, "serverName", null), "https://localhost:8443");
    }

    @Test
    public void testGetValueFromFilterConfig() {
        final TestConfigurationFilter filter = new TestConfigurationFilter();
        
        final MockServletContext ctx = new MockServletContext();
        ctx.addInitParameter("service", "http://a.service.com");
        
        final MockFilterConfig filterConfig = new MockFilterConfig(ctx);
        filterConfig.addInitParameter(AbstractConfigurationFilter.PARAM_NAME_CONFIG_FILE, CONFIG_FILE_PATH);
        filterConfig.addInitParameter("serverName", "http://foo.com");

        assertEquals(filter.getPropertyFromInitParams(filterConfig, "serverName", null), "http://foo.com");
        assertNotNull(filter.getPropertyFromInitParams(filterConfig, "service", null));
    }
    
    private static class TestConfigurationFilter extends AbstractConfigurationFilter {
        public void destroy() {}
        public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {}
        public void init(FilterConfig arg0) throws ServletException {}        
    }
}
