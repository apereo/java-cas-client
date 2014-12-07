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
package org.jasig.cas.client.validation;

import java.util.Timer;
import java.util.TimerTask;
import junit.framework.TestCase;
import org.jasig.cas.client.proxy.CleanUpTimerTask;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.util.MethodFlag;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

/**
 * Unit test for {@link org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter}
 *
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class Cas20ProxyReceivingTicketValidationFilterTests extends TestCase {

    private final Timer defaultTimer = new Timer(true);

    private final ProxyGrantingTicketStorage storage = new ProxyGrantingTicketStorageImpl();

    private final CleanUpTimerTask defaultTimerTask = new CleanUpTimerTask(storage);

    public void testStartsThreadAtStartup() throws Exception {
        final MethodFlag scheduleMethodFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final Timer timer = new Timer(true) {
            public void schedule(TimerTask task, long delay, long period) {
                scheduleMethodFlag.setCalled();
            }
        };

        filter.setMillisBetweenCleanUps(1);
        filter.setProxyGrantingTicketStorage(storage);
        filter.setTimer(timer);
        filter.setTimerTask(defaultTimerTask);

        filter.init();
        assertTrue(scheduleMethodFlag.wasCalled());
    }

    public void testShutsDownTimerThread() throws Exception {
        final MethodFlag cancelMethodFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final Timer timer = new Timer(true) {
            public void cancel() {
                cancelMethodFlag.setCalled();
                super.cancel();
            }
        };

        filter.setProxyGrantingTicketStorage(storage);
        filter.setMillisBetweenCleanUps(1);
        filter.setTimer(timer);
        filter.setTimerTask(defaultTimerTask);
        filter.init();
        filter.destroy();

        assertTrue(cancelMethodFlag.wasCalled());
    }

    public void testCallsCleanAllOnSchedule() throws Exception {
        final MethodFlag timerTaskFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTaskFlag.setCalled();
            }
        };

        final int millisBetweenCleanUps = 250;
        filter.setProxyGrantingTicketStorage(storage);
        filter.setTimerTask(timerTask);
        filter.setTimer(defaultTimer);
        filter.setMillisBetweenCleanUps(millisBetweenCleanUps);

        filter.init();

        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);

        assertTrue(timerTaskFlag.wasCalled());
        filter.destroy();
    }

    public void testDelaysFirstCleanAll() throws Exception {
        final MethodFlag timerTaskFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTaskFlag.setCalled();
            }
        };

        final int millisBetweenCleanUps = 250;
        filter.setProxyGrantingTicketStorage(storage);
        filter.setMillisBetweenCleanUps(millisBetweenCleanUps);
        filter.setTimer(defaultTimer);
        filter.setTimerTask(timerTask);

        filter.init();

        assertFalse(timerTaskFlag.wasCalled());

        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);

        assertTrue(timerTaskFlag.wasCalled());

        filter.destroy();
    }

    public void testThrowsForNullStorage() throws Exception {
        Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();
        filter.setProxyGrantingTicketStorage(null);

        try {
            filter.init();
            fail("expected an exception due to null ProxyGrantingTicketStorage");
        } catch (IllegalArgumentException exception) {
            // test passes
        }
    }

    public void testGetTicketValidator() throws Exception {
        Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        // Test case #1
        final MockFilterConfig config1 = new MockFilterConfig();
        config1.addInitParameter("allowedProxyChains", "https://a.example.com");
        config1.addInitParameter("casServerUrlPrefix", "https://cas.jasig.org/");
        config1.addInitParameter("service", "http://www.jasig.org");
        filter.init(config1);
        assertNotNull(filter.getTicketValidator(config1));
    }

    @Test
    public void getTicketValidatorWithProxyChains() throws Exception {
        Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();
        // Test case #2
        final MockFilterConfig config2 = new MockFilterConfig();
        config2.addInitParameter("allowedProxyChains", "https://a.example.com https://b.example.com");
        config2.addInitParameter("casServerUrlPrefix", "https://cas.jasig.org/");
        config2.addInitParameter("service", "http://www.jasig.org");
        filter.init(config2);
        assertNotNull(filter.getTicketValidator(config2));
    }


    @Test
    public void getTIcketValidatorWithProxyChainsAndLineBreak() throws Exception {
        Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        // Test case #3
        final MockFilterConfig config3 = new MockFilterConfig();
        config3.addInitParameter("allowedProxyChains",
                "https://a.example.com https://b.example.com\nhttps://c.example.com");
        config3.addInitParameter("casServerUrlPrefix", "https://cas.jasig.org/");
        config3.addInitParameter("service", "http://www.jasig.org");
        filter.init(config3);
        assertNotNull(filter.getTicketValidator(config3));
    }

    public void testRenewInitParamThrows() throws Exception {
        final Cas20ProxyReceivingTicketValidationFilter f = new Cas20ProxyReceivingTicketValidationFilter();
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerUrlPrefix", "https://cas.example.com");
        config.addInitParameter("renew", "true");
        try {
            f.init(config);
            fail("Should have thrown IllegalArgumentException.");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Renew MUST"));
        }
    }

    public void testAllowsRenewContextParam() throws Exception {
        final Cas20ProxyReceivingTicketValidationFilter f = new Cas20ProxyReceivingTicketValidationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerUrlPrefix", "https://cas.example.com");
        context.addInitParameter("renew", "true");
        context.addInitParameter("service", "http://www.jasig.org");
        final MockFilterConfig config = new MockFilterConfig(context);
        f.init(config);
        final TicketValidator validator = f.getTicketValidator(config);
        assertTrue(validator instanceof AbstractUrlBasedTicketValidator);
        assertTrue(((AbstractUrlBasedTicketValidator) validator).isRenew());
    }

    /**
     * construct a working {@link org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter}
     */
    private Cas20ProxyReceivingTicketValidationFilter newCas20ProxyReceivingTicketValidationFilter() {
        final Cas20ProxyReceivingTicketValidationFilter filter = new Cas20ProxyReceivingTicketValidationFilter();
        filter.setServerName("localhost");
        filter.setTicketValidator(new Cas20ProxyTicketValidator(""));

        return filter;
    }
}
