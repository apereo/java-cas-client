package org.jasig.cas.client.http;

/**
 * An abstraction of the essential properties of an HTTP request necessary for
 * CAS protocol handling.
 *
 * @author Carl Harris
 */
public interface HttpRequest {

    /**
     * Gets the HTTP method name.
     * @return method name
     */
    String getMethod();

    /**
     * Gets the URL the client used to make the request.
     * @return URL
     */
    String getRequestURL();

    /**
     * Gets the part of this request's URL that appears in the first line of the HTTP request, following the method
     * name and before the query string (if present) and HTTP protocol name.
     * <p>
     * In particular, the return value does not include the scheme or authority (server name and port) portion of the
     * URL.
     * @return request URI
     */
    String getRequestURI();

    /**
     * Gets the query string specified in first line of the HTTP request.
     * @return query string or {@code null} if no query string was specified
     */
    String getQueryString();

    /**
     * Gets the value associated with the first appearance of a parameter in the request.
     * @param name name of the subject parameter
     * @return parameter value or {@code null} if no parameter exists with the given {@code name}
     */
    String getParameter(String name);

    /**
     * Gets the MIME media type of the body of the request.
     * @return MIME media type or {@code null} if not known
     */
    String getContentType();

    /**
     * Gets the value associated with the first appearance of a header in the request.
     * @param name name of the subject header (case insensitive)
     * @return header value or {@code null} if no header exists with the given {@code name}
     */
    String getHeader(String name);

    /**
     * Gets an existing client session associated with this request.
     * @return client session or {@code null} if no session exists
     */
    ClientSession getSession();

    /**
     * Gets the client session associated with this request, creating it if necessary.
     * @return client session
     */
    ClientSession getOrCreateSession();

    /**
     * Tests whether this request was received over a secure transport.
     * @return {@code true} if request was received over a secure transport
     */
    boolean isSecure();

    /**
     * Gets the server (TCP) port over which this request was received.
     * @return server port
     */
    int getServerPort();

    /**
     * Notifies the recipient (typically a servlet container) that the user associated
     * with the client session should be logged out.
     * <p>
     * Invoking this method does not necessarily have any effect on an associated client
     * session.
     */
    void logout();

}
