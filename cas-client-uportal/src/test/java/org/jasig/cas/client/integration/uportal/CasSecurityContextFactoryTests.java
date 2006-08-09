package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.jasig.portal.security.ISecurityContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CasSecurityContextFactoryTests extends TestCase {

    private CasSecurityContextFactory casSecurityContextFactory;


    protected void setUp() throws Exception {
        this.casSecurityContextFactory = new CasSecurityContextFactory();
    }

    public void testGetter() {
        final ISecurityContext casSecurityContext = this.casSecurityContextFactory.getSecurityContext();
        assertNotNull(casSecurityContext);
    }
}
