package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.jasig.portal.security.ISecurityContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ThreadLocalAwareCasSecurityContextFactoryTests extends TestCase {

    private ThreadLocalAwareCasSecurityContextFactory factory;

    protected void setUp() throws Exception {
        this.factory = new ThreadLocalAwareCasSecurityContextFactory();
    }

    public void testGetter() {
        ISecurityContext context = this.factory.getSecurityContext();

        assertNotNull(context);
        assertTrue(context instanceof ThreadLocalAwareCasSecurityContext);
    }
}
