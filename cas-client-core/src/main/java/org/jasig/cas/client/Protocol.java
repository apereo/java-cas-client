package org.jasig.cas.client;

/**
 * Created by battags on 10/14/14.
 */
public enum Protocol {



    CAS1("ticket", "service"), CAS2(CAS1.getArtifactParameterName(), CAS1.getServiceParameterName()), SAML11("SAMLart", "TARGET");

    private final String artifactParameterName;

    private final String serviceParameterName;

    private Protocol(final String artifactParameterName, final String serviceParameterName) {
        this.artifactParameterName = artifactParameterName;
        this.serviceParameterName = serviceParameterName;
    }

    public String getArtifactParameterName() {
        return this.artifactParameterName;
    }

    public String getServiceParameterName() {
        return this.serviceParameterName;
    }
}
