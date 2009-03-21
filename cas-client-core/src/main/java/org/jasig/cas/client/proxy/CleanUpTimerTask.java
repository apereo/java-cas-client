package org.jasig.cas.client.proxy;

import java.util.TimerTask;

import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;

/**
 * A {@link TimerTask} implementation which performs the
 * actual 'cleaning' by calling {@link ProxyGrantingTicketStorage#cleanUp()}.
 * <p>
 * You must configure either this TimerTask directly in Spring,
 * or the {@link CleanUpListener} (which is configured in
 * web.xml). Both choices perform the same operation, the only difference is
 * one is configured via the Spring Framework and the other via web.xml.
 * <p>
 * For an example web.xml configuration, see {@link CleanUpListener}.
 * For an example Spring xml configuration, see below:
 * <code>
 *     <bean id="cleanUpTimerTask" class="org.jasig.cas.client.proxy.CleanUpTimerTask"/>
 *     
 *     <bean id="scheduledTimerTask" class="org.springframework.scheduling.timer.ScheduledTimerTask">
 *         <!-- first run is 15 seconds after startup -->
 *         <property name="delay" value="15000"/>
 *         <!-- subsequent runs every 5 seconds -->
 *         <property name="period" value="5000"/>
 *         <property name="timerTask" ref="cleanUpTimerTask"/>
 *     </bean>
 *     
 *     <bean class="org.springframework.scheduling.timer.TimerFactoryBean">
 *         <property name="scheduledTimerTasks">
 *             <list>
 *                 <ref bean="scheduledTimerTask"/>
 *             </list>
 *         </property>
 *     </bean>
 * </code>
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public final class CleanUpTimerTask extends TimerTask {
    public void run() {
        Cas20ProxyReceivingTicketValidationFilter.getProxyGrantingTicketStorage().cleanUp();
    }
}
