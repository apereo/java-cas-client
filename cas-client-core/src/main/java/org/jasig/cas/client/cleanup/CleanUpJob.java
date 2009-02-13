package org.jasig.cas.client.cleanup;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * One of two timer implementations that regularly calls
 * {@link CleanUpRegistry#cleanAll()}. This implementation
 * is configured in via Spring and does not require any changes
 * to web.xml
 * <p>
 * {@link CleanUpListener}, the other implementation, does require
 * changes to web.xml, but does not require any Spring configuration.
 * <p>
 * The decision as to which timer implementation you use is entirely
 * subjective, as both perform the same operation.
 * <p>
 * Example Spring config:
 * <code>
 * <bean id="cleanUpJob" class="org.springframework.scheduling.quartz.JobDetailBean">
 *     <property name="jobClass" value="org.jasig.cas.client.cleanup.CleanUpJob"/>
 * </bean>
 *
 * <bean id="cleanUpTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerBean">
 *     <property name="jobDetail" ref="cleanUpJob"/>
 *     <property name="startDelay" value="60000"/>
 *     <property name="repeatInterval" value="60000"/>
 * </bean>
 *
 * <bean class="org.springframework.scheduling.quartz.SchedulerFactoryBean" destroy-method="destroy">
 *     <property name="triggers">
 *         <list>
 *             <ref bean="cleanUpTrigger"/>
 *         </list>
 *     </property>
 * </bean>
 * </code>
 * 
 * Note the destroy-method attribute of SchedulerFactoryBean. Without this attribute,
 * a classloader leak may occur.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpJob extends QuartzJobBean {
    private final CleanUpRegistry cleanUpRegistry;

    public CleanUpJob() {
        this.cleanUpRegistry = CleanUpRegistryImpl.getInstance();
    }
    
    public CleanUpJob(CleanUpRegistry cleanUpRegistry) {
        this.cleanUpRegistry = cleanUpRegistry;
    }
    
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        cleanUpRegistry.cleanAll();
    }
}
