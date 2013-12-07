import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl;
import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2.0
 */
public class EhCacheBackedProxyGrantingTicketStorageImplTests  {

    @Test
    @Ignore
    public void testEncryptionMechanisms() throws Exception {
        final Cache ehcache = new Cache("name", 100,false, false, 500, 500);
        CacheManager.getInstance().addCache(ehcache);
        final EhcacheBackedProxyGrantingTicketStorageImpl cache = new EhcacheBackedProxyGrantingTicketStorageImpl(ehcache);
        cache.setSecretKey("thismustbeatleast24charactersandcannotbelessthanthat");

        Assert.assertNull(cache.retrieve(null));
        Assert.assertNull(cache.retrieve("foobar"));

        cache.save("proxyGrantingTicketIou", "proxyGrantingTicket");
        Assert.assertEquals("proxyGrantingTicket", cache.retrieve("proxyGrantingTicketIou"));
        Assert.assertFalse("proxyGrantingTicket".equals(ehcache.get("proxyGrantingTicketIou").getValue()));
    }
}
