package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.util.CommonUtils;

/**
 * ThreadLocal container that exposes the service to lower layers in the authentication stack.  Because of the ISecurityCcontext
 * API this service url is not normally available.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceHolder {

    private static final ThreadLocal threadLocal = new ThreadLocal();

    public static void setService(final Service service) {
        CommonUtils.assertNotNull(service, "service cannot be null.");
        threadLocal.set(service);
    }

    /**
     * Method to retrieve the service from the ThreadLocal
     *
     * @return the service.  Should not ever be null.
     */
    public static Service getService() {
        return (Service) threadLocal.get();
    }

    /**
     * Reset the context to clear it out.
     */
    public static void clearContext() {
        threadLocal.set(null);
    }
}
