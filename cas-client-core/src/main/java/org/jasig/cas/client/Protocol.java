package org.jasig.cas.client;

/**
 * Simple enumeration to hold/capture some of the standard request parameters used by the various protocols.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public enum Protocol {

    CAS1("ticket", "service"), CAS2(CAS1), SAML11("SAMLart", "TARGET");

    private final String artifactParameterName;

    private final String serviceParameterName;

    private Protocol(final String artifactParameterName, final String serviceParameterName) {
        this.artifactParameterName = artifactParameterName;
        this.serviceParameterName = serviceParameterName;
    }

    private Protocol(final Protocol protocol) {
        this(protocol.getArtifactParameterName(), protocol.getServiceParameterName());
    }

    public String getArtifactParameterName() {
        return this.artifactParameterName;
    }

    public String getServiceParameterName() {
        return this.serviceParameterName;
    }
}
