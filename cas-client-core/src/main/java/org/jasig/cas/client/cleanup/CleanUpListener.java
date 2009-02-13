package org.jasig.cas.client.cleanup;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * One of two timer implementations that regularly calls
 * {@link CleanUpRegistry#cleanAll()}. This implementation
 * is configured in web.xml and does not require any Spring
 * configuration.
 * <p>
 * {@link CleanUpJob}, the other implementation, does require
 * Spring, but does not require changes to web.xml
 * <p>
 * The decision as to which timer implementation you use is entirely
 * subjective, as both perform the same operation.
 * <p>
 * You can customize the time between cleanup runs by setting a
 * context-param in web.xml
 * <p>
 * Example web.xml config:
 * <code>
 * <context-param>
 *     <param-name>millisBetweenCleanUps</param-name>
 *     <param-value>45000</param-value>
 * </context-param>
 * </code>
 * 
 * With this listener configured, a timer will be set to clean up any {@link Cleanable}s.
 * This timer will automatically shut down on webapp undeploy, preventing any classloader
 * leaks from occurring.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpListener implements ServletContextListener {
    protected static final int DEFAULT_MILLIS_BETWEEN_CLEANUPS = 60 * 1000;
	protected static final String MILLIS_BETWEEN_CLEANUPS_INIT_PARAM = "millisBetweenCleanUps";
	private final Timer timer;
	private final CleanUpRegistry cleanUpRegistry;
	
	public CleanUpListener() {
	    this.timer = new Timer(true);
	    this.cleanUpRegistry = CleanUpRegistryImpl.getInstance();
	}
	
	protected CleanUpListener(Timer timer, CleanUpRegistry cleanUpRegistry) {
	    this.timer = timer;
	    this.cleanUpRegistry = cleanUpRegistry;
	}
	
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	    final Cleaner cleaner = new Cleaner(this.cleanUpRegistry);
	    final long millisBetweenCleanUps = getMillisBetweenCleanups(servletContextEvent.getServletContext());
	    final long millisBeforeStart = millisBetweenCleanUps;
	    
        this.timer.schedule(cleaner, millisBeforeStart, millisBetweenCleanUps);
	}

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        this.timer.cancel();
	}
	
	protected long getMillisBetweenCleanups(ServletContext servletContext) {
	    final String millisBetweenCleanUps = servletContext.getInitParameter(MILLIS_BETWEEN_CLEANUPS_INIT_PARAM);
	    
	    if (millisBetweenCleanUps == null) {
	        return DEFAULT_MILLIS_BETWEEN_CLEANUPS;
	    }
	    
        try {
            return Long.parseLong(millisBetweenCleanUps);
        } catch (NumberFormatException exception) {
            throw new RuntimeException("The servlet context-param " + MILLIS_BETWEEN_CLEANUPS_INIT_PARAM + " must be a valid number (hint: this is usually set in web.xml)", exception);
        }
	}
	
	/**
	 * Does the actual clean up each time the timer goes off.
	 * 
	 * @author Brad Cupit (brad [at] lsu {dot} edu)
	 */
	private static final class Cleaner extends TimerTask {
	    private final CleanUpRegistry cleanUpRegistry;
        
	    private Cleaner(CleanUpRegistry cleanUpRegistry) {
            this.cleanUpRegistry = cleanUpRegistry;
	    }
	    
	    public void run() {
	        this.cleanUpRegistry.cleanAll();
	    }
	}
}
