package org.jasig.cas.client;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class PublicTestHttpServer extends Thread {

    private static PublicTestHttpServer httpServer;

    public byte[] content;

    private byte[] header;

    private int port = 80;

    public String encoding;

    private PublicTestHttpServer(String data, String encoding, String MIMEType,
                                 int port) throws UnsupportedEncodingException {
        this(data.getBytes(encoding), encoding, MIMEType, port);
    }

    private PublicTestHttpServer(byte[] data, String encoding, String MIMEType,
                                 int port) throws UnsupportedEncodingException {

        this.content = data;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.0 200 OK\r\n" + "Server: OneFile 1.0\r\n"
                // + "Content-length: " + this.content.length + "\r\n"
                + "Content-type: " + MIMEType + "\r\n\r\n";
        this.header = header.getBytes("ASCII");

    }

    public static synchronized PublicTestHttpServer instance() {
        if (httpServer == null) {
            try {
                httpServer = new PublicTestHttpServer("test", "ASCII",
                        "text/plain", 8085);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            httpServer.start();
            Thread.yield();
        }

        return httpServer;
    }

    public void run() {

        try {
            ServerSocket server = new ServerSocket(this.port);
            System.out.println("Accepting connections on port "
                    + server.getLocalPort());
            while (true) {

                Socket connection = null;
                try {
                    connection = server.accept();
                    OutputStream out = new BufferedOutputStream(connection
                            .getOutputStream());
                    InputStream in = new BufferedInputStream(connection
                            .getInputStream());
                    // read the first line only; that's all we need
                    StringBuffer request = new StringBuffer(80);
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
                catch (IOException e) {
                    // nothing to do with this IOException
                } finally {
                    if (connection != null)
                        connection.close();
                }

            } // end while
        } // end try
        catch (IOException e) {
            System.err.println("Could not start server. Port Occupied");
        }

    } // end run
}
