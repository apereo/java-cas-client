package org.jasig.cas.client.authentication;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/**
 * Extension to the standard Java Principal that includes a way to retrieve proxy tickets for a particular user
 * and attributes.
 * <p>
 * Developer's who don't want their code tied to CAS merely need to work with the Java Principal then.  Working with
 * the CAS-specific features requires knowledge of the AttributePrincipal class.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface AttributePrincipal extends Principal, Serializable {

    /**
     * Retrieves a CAS proxy ticket for this specific principal.
     *
     * @param service the service we wish to proxy this user to.
     * @return a String representing the proxy ticket.
     */
    String getProxyTicketFor(String service);

    /**
     * The Map of key/value pairs associated with this principal.
     * @return the map of key/value pairs associated with this principal.
     */
    Map getAttributes();

}
