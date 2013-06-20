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

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2.0
 */
public class EhCacheBackedProxyGrantingTicketStorageImplTests extends TestCase {

    public void testEncryptionMechanisms() throws Exception {
        final Cache ehcache = new Cache("name", 100, false, false, 500, 500);
        CacheManager.getInstance().addCache(ehcache);
        final EhcacheBackedProxyGrantingTicketStorageImpl cache = new EhcacheBackedProxyGrantingTicketStorageImpl(
                ehcache);
        //        cache.setSecretKey("thismustbeatleast24charactersandcannotbelessthanthat1234");

        assertNull(cache.retrieve(null));
        assertNull(cache.retrieve("foobar"));

        cache.save("proxyGrantingTicketIou", "proxyGrantingTicket");
        assertEquals("proxyGrantingTicket", cache.retrieve("proxyGrantingTicketIou"));
        assertTrue("proxyGrantingTicket".equals(ehcache.get("proxyGrantingTicketIou").getValue()));
    }
}
