import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2.0
 */
public class EhCacheBackedProxyGrantingTicketStorageImplTests extends TestCase {

    public void testEncryptionMechanisms() throws Exception {
        final Cache ehcache = new Cache("name", 100,false, false, 500, 500);
        CacheManager.getInstance().addCache(ehcache);
        final EhcacheBackedProxyGrantingTicketStorageImpl cache = new EhcacheBackedProxyGrantingTicketStorageImpl(ehcache);
        cache.setSecretKey("thismustbeatleast24charactersandcannotbelessthanthat");

        assertNull(cache.retrieve(null));
        assertNull(cache.retrieve("foobar"));

        cache.save("proxyGrantingTicketIou", "proxyGrantingTicket");
        assertEquals("proxyGrantingTicket", cache.retrieve("proxyGrantingTicketIou"));
        assertFalse("proxyGrantingTicket".equals(ehcache.get("proxyGrantingTicketIou").getValue()));
    }
}
