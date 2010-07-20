package org.jasig.cas.client.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

/**
 * Created by IntelliJ IDEA.
 * User: scottbattaglia
 * Date: Jul 19, 2010
 * Time: 11:11:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CasRealm implements Realm {

    // &lt;description&gt;/&lt;version&gt;

    private static final String INFO = "org.jasig.cas.client.tomcat.CasRealm/1.0";

    private Container container;

    public Container getContainer() {
        return this.container;
    }

    public void setContainer(final Container container) {
        this.container = container;
    }

    public String getInfo() {
        return INFO;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal authenticate(String s, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal authenticate(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal authenticate(X509Certificate[] x509Certificates) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void backgroundProcess() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SecurityConstraint[] findSecurityConstraints(Request request, Context context) {
        return new SecurityConstraint[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasResourcePermission(Request request, Response response, SecurityConstraint[] securityConstraints, Context context) throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasRole(Principal principal, String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasUserDataPermission(Request request, Response response, SecurityConstraint[] securityConstraints) throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
