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
package org.jasig.cas.client.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;

/**
 * Implementation of the {@link org.jasig.cas.client.proxy.ProxyGrantingTicketStorage} interface that is backed by
 * Memcache for distributed web applications.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.9
 */
public final class MemcachedBackedProxyGrantingTicketStorageImpl extends
        AbstractEncryptedProxyGrantingTicketStorageImpl {

    protected final MemcachedClientIF client;

    /**
     * Default constructor reads from the /casclient_memcached_hosts.txt in the classpath.  Each line should be a host:port
     * combination of memcached servers.
     */
    public MemcachedBackedProxyGrantingTicketStorageImpl() {
        this(getHostsFromClassPath());
    }

    protected static String[] getHostsFromClassPath() {
        final InputStream inputStream = MemcachedBackedProxyGrantingTicketStorageImpl.class
                .getResourceAsStream("/cas/casclient_memcached_hosts.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final List<String> hosts = new ArrayList<String>();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                hosts.add(line);
            }

            return hosts.toArray(new String[hosts.size()]);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                // nothing to do
            }

            try {
                inputStream.close();
            } catch (final IOException e) {
                // do nothing
            }
        }
    }

    public MemcachedBackedProxyGrantingTicketStorageImpl(final String[] hostnamesAndPorts) {
        final List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

        for (final String hostname : hostnamesAndPorts) {
            final String[] hostPort = hostname.split(":");
            addresses.add(new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1])));
        }

        try {
            this.client = new MemcachedClient(addresses);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveInternal(final String proxyGrantingTicketIou, final String proxyGrantingTicket) {
        handleSynchronousRequest(this.client.add(proxyGrantingTicketIou, 120, proxyGrantingTicket));
    }

    public String retrieveInternal(final String proxyGrantingTicketIou) {
        return (String) this.client.get(proxyGrantingTicketIou);
    }

    public void cleanUp() {
        // we actually don't have anything to do here, yay!
    }

    private void handleSynchronousRequest(final Future<?> f) {
        try {
            f.get();
        } catch (final Exception e) {
            // ignore these.
        }
    }
}
