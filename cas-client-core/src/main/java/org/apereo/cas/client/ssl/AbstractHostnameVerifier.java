package org.apereo.cas.client.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Adding this abstract super class giving flexibility to the user to add more common methods to super class at later point of time
 *
 * @author Abhinav Singh
 */
public abstract class AbstractHostnameVerifier implements HostnameVerifier {

    @Override
    public abstract boolean verify(final String hostname, final SSLSession session);

}
