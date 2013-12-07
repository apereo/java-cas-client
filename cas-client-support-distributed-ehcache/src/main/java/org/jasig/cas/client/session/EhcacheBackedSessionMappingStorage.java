package org.jasig.cas.client.session;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.SessionUtils;

import javax.servlet.http.HttpSession;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-11
 * Time: 上午11:08
 * To change this template use File | Settings | File Templates.
 */
public class EhcacheBackedSessionMappingStorage implements SessionMappingStorage, Closeable {
    private final Log LOGGER = LogFactory.getLog(getClass());
    private CacheManager cacheManager;
    private final String SESSIONID_TO_MAPPINGID_CACHE_NAME = "sessionId2MappingIdCache";
    private final Ehcache sessionId2MappingIdCache;
    private final String MAPPINGID_TO_SESSIONID_CACHE_NAME = "mappingId2SessionIdCache";
    private final Ehcache mappingId2SessionIdCache;

    public EhcacheBackedSessionMappingStorage() {
        this(null);
    }

    public EhcacheBackedSessionMappingStorage(String configPath) {
        this.cacheManager = createCacheManager(configPath);
        this.sessionId2MappingIdCache = retrieveEhcache(this.cacheManager, SESSIONID_TO_MAPPINGID_CACHE_NAME);
        this.mappingId2SessionIdCache = retrieveEhcache(this.cacheManager, MAPPINGID_TO_SESSIONID_CACHE_NAME);
//        new SessionMappingStorageMonitor(sessionId2MappingIdCache, mappingId2SessionIdCache).start();
    }
//
//    private static class SessionMappingStorageMonitor extends Thread {
//        private final Log LOG = LogFactory.getLog(getClass());
//        private final Ehcache sessionId2MappingIdCache;
//        private final Ehcache mappingId2SessionIdCache;
//        public volatile boolean isStop = false;
//
//        public SessionMappingStorageMonitor(Ehcache sessionId2MappingIdCache, Ehcache mappingId2SessionIdCache) {
//            this.sessionId2MappingIdCache = sessionId2MappingIdCache;
//            this.mappingId2SessionIdCache = mappingId2SessionIdCache;
//        }
//
//        public void run() {
//            LOG.debug("------------------------------------------------------------");
//            LOG.debug("start SessionMappingStorageMonitor ....");
//            LOG.debug("------------------------------------------------------------");
//            while (isStop) {
//                try {
//                    LOG.debug("******** " + mappingId2SessionIdCache.getStatistics() + "******** ");
//                    LOG.debug("******** " + sessionId2MappingIdCache.getStatistics() + "******** ");
//                    Thread.sleep(100000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//        }
//    }

    private Ehcache retrieveEhcache(CacheManager manager, String cacheName) {
        Ehcache cache = manager.getCache(cacheName);
        if (cache == null) {
            manager.addCache(cacheName);
        }
        cache = manager.getEhcache(cacheName);
        return cache;
    }

    private CacheManager createCacheManager(String configPath) {
        CacheManager manager = null;
        if (configPath != null) {
            URL configURL = getClass().getResource(configPath);
            if (LOGGER.isDebugEnabled()) {
                if (configURL == null) {
                    LOGGER.debug("Not found the ehcache configuration file on " + configPath + " using the default one");
                } else {
                    LOGGER.debug("Using the ehcache configuration file: " + configURL);
                }
            }
            manager = new CacheManager(configURL);
        }
        if (manager == null) {
            manager = CacheManager.getInstance();
        }
        return manager;
    }


    public synchronized HttpSession removeSessionByMappingId(String mappingId) {
        Element element = mappingId2SessionIdCache.get(mappingId);
        if (element != null) {
            String sessionId = (String) element.getObjectValue();
            HttpSession session = SessionStorage.getInstance().get(sessionId);
            if (session != null) {
                removeBySessionById(session.getId());
            }
            LOGGER.info(String.format("Found mapping(mappingId: %s) for session", mappingId));
            return session;
        } else {
            LOGGER.info(String.format("No mapping(mappingId: %s) session found ", mappingId));
            return null;
        }
    }

    public synchronized void removeBySessionById(String sessionId) {
        String stripSessionId = SessionUtils.stripSessionIdPostfix(sessionId);
        final Element element = sessionId2MappingIdCache.get(stripSessionId);

        if (element != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Found mapping for (sessionId: %s).  Session Removed.", sessionId));
            }
            String mappingId = (String) element.getObjectValue();
            mappingId2SessionIdCache.remove(mappingId);
            sessionId2MappingIdCache.remove(stripSessionId);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("No mapping for (sessionId: %s) found.  Ignoring.", sessionId));
            }
        }
    }

    public synchronized void addSessionById(String mappingId, HttpSession session) {
        if (mappingId == null || session == null) {
            throw new IllegalArgumentException("mappingId and HttpSession can not be null");
        }
        String sessionId = SessionUtils.stripSessionIdPostfix(session.getId());
        sessionId2MappingIdCache.put(new Element(sessionId, mappingId));
        mappingId2SessionIdCache.put(new Element(mappingId, sessionId));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("==== map (sessionId: %s) to (mappingId: %s) , and vice versa====",
                    session.getId(), mappingId));
        }
    }

    public void close() throws IOException {
        cacheManager.shutdown();
    }


}
