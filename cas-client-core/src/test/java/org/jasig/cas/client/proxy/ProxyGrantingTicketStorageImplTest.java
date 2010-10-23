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

package org.jasig.cas.client.proxy;

import junit.framework.TestCase;

/**
 * Unit test for {@link ProxyGrantingTicketStorageImpl}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class ProxyGrantingTicketStorageImplTest extends TestCase {
    public void testCleanUp() throws Exception {
        String proxyGrantingTicketIou = "proxyGrantingTicketIou";
        
        int timeout = 250;
        ProxyGrantingTicketStorageImpl storage = new ProxyGrantingTicketStorageImpl(timeout);
        storage.save(proxyGrantingTicketIou, "proxyGrantingTicket");
        
        // sleep long enough for the ticket to timeout
        Thread.sleep(timeout * 2);
        
        storage.cleanUp();
        
        assertNull(storage.retrieve(proxyGrantingTicketIou));
    }
}
