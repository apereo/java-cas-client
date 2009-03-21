package org.jasig.cas.client.proxy;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * One of two choices for cleaning up {@link ProxyGrantingTicketStorage}.
 * You must either configure this listener in web.xml or configure
 * {@link CleanUpTimerTask} in Spring xml. Both choices perform the same
 * operation, the only difference is one is configured via the Spring
 * Framework and the other via web.xml.
 * <p>
 * See below for example web.xml configuration.
 * See {@link CleanUpTimerTask} for an example Spring xml configuration.
 * <p>
 * With this listener configured, a timer will clean up the
 * {@link ProxyGrantingTicketStorage storage}. This timer automatically
 * shuts down on webapp undeploy, so there will be no classloader leaks
 * due to an orphan thread.
 * <p>
 * Example web.xml configuration:
 * <code>
 *     <listener>
 *         <listener-class>org.jasig.cas.client.proxy.CleanUpListener</listener-class>
 *     </listener>
 * </code>
 * <p>
 * The default time between cleanups is 60 seconds, but you can optionally
 * customize it by setting a context-param in web.xml. Example config:
 * <code>
 *     <context-param>
 *         <param-name>millisBetweenCleanUps</param-name>
 *         <!-- 45 seconds between cleanup runs -->
 *         <param-value>45000</param-value>
 *     </context-param>
 * </code>
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public final class CleanUpListener implements ServletContextListener {
    protected static final int DEFAULT_MILLIS_BETWEEN_CLEANUPS = 60 * 1000;
	protected static final String MILLIS_BETWEEN_CLEANUPS_INIT_PARAM = "millisBetweenCleanUps";
	private final Timer timer;
	private final TimerTask timerTask;
	
	public CleanUpListener() {
	    this.timer = new Timer(true);
	    this.timerTask = new CleanUpTimerTask();
	}
	
	/**
	 * for unit test use only
	 */
	protected CleanUpListener(final Timer timer, TimerTask timerTask ) {
	    this.timer = timer;
	    this.timerTask = timerTask;
	}
	
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	    final long millisBetweenCleanUps = getMillisBetweenCleanups(servletContextEvent.getServletContext());
	    final long millisBeforeStart = millisBetweenCleanUps;
	    
        this.timer.schedule(timerTask, millisBeforeStart, millisBetweenCleanUps);
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
}
