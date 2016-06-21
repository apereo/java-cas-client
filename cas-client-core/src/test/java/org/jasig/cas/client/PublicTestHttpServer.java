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
package org.jasig.cas.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Scott Battaglia
 * @version $Revision: 11721 $ $Date: 2007-08-09 15:17:44 -0400 (Wed, 09 Aug 2007) $
 * @since 3.0
 */
public final class PublicTestHttpServer extends Thread {

    private static PublicTestHttpServer httpServer;

    private static final Logger LOG = Logger.getLogger( PublicTestHttpServer.class );
    
    public byte[] content;

    private final byte[] header;

    private final int port;

    public final String encoding;

    private ServerSocket server;

    private static Map<Integer, PublicTestHttpServer> serverMap = new HashMap<Integer, PublicTestHttpServer>();

    private PublicTestHttpServer(String data, String encoding, String MIMEType, int port)
            throws UnsupportedEncodingException {
        this(data.getBytes(encoding), encoding, MIMEType, port);
    }

    private PublicTestHttpServer(byte[] data, String encoding, String MIMEType, int port)
            throws UnsupportedEncodingException {
        this.content = data;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.0 200 OK\r\n" + "Server: OneFile 1.0\r\n" + "Content-type: " + MIMEType + "\r\n\r\n";
        this.header = header.getBytes("ASCII");
    }

    public static synchronized PublicTestHttpServer instance(final int port) {
        if (serverMap.containsKey(port)) {
            return serverMap.get(port);
        }

        try {
            final PublicTestHttpServer server = new PublicTestHttpServer("test", "ASCII", "text/plain", port);
            server.start();
            serverMap.put(port, server);
            Thread.yield();
            return server;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        LOG.info("Shutting down connection on port " + server.getLocalPort());
        try {
            this.server.close();
        } catch (final Exception e) {
            LOG.error("Error on shutdown", e);
        }

        httpServer = null;
    }

    public void run() {

        try {
            this.server = new ServerSocket(this.port);
            LOG.info("Accepting connections on port " + server.getLocalPort());
            while (true) {

                Socket connection = null;
                try {
                    connection = server.accept();
                    final OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    final InputStream in = new BufferedInputStream(connection.getInputStream());
                    // read the first line only; that's all we need
                    final StringBuffer request = new StringBuffer(80);
                    while (true) {
                        int c = in.read();
                        if (c == '\r' || c == '\n' || c == -1)
                            break;
                        request.append((char) c);
                    }

                    if (request.toString().startsWith("STOP")) {
                        connection.close();
                        break;
                    }
                    if (request.toString().indexOf("HTTP/") != -1) {
                        out.write(this.header);
                    }
                    out.write(this.content);
                    out.flush();
                } // end try
                catch (final IOException e) {
                    // nothing to do with this IOException
                } finally {
                    if (connection != null)
                        connection.close();
                }

            } // end while
        } // end try
        catch (final IOException e) {
            LOG.error("Could not start server. Port Occupied.", e);
        }

    } // end run
}
