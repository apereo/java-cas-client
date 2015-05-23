# Java Apereo CAS Client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server)

## Intro
This is the official home of the Java Apereo CAS client. The client consists of a collection of Servlet filters that are suitable for most Java-based web applications. It also serves as an API platform to interact with the CAS server programmatically to make authentication requests, validate tickets and consume principal attributes.

All client artifacts are published to Maven central. At a minimum, a given application will need to configure the following dependency:

```xml
<dependency>
	<groupId>org.jasig.cas.client</groupId>
	<artifactId>cas-client-core</artifactId>
	<version>${java.cas.client.version}</version>
</dependency>
```
## Configurtion

### Client Configuration Using `web.xml`

The client can be configured via `web.xml` via a series of `context-param`s and filter `init-param`s. Each filter for the client has a required (and optional) set of properties. The filters are designed to look for these properties in the following way:

- Check the filter's local `init-param`s for a parameter matching the required property name.
- Check the `context-param`s for a parameter matching the required property name.
- If two properties are found with the same name in the `init-param`s and the `context-param`s, the `init-param` takes precedence. 

**Note**: If you're using the `serverName` property, you should note well that the fragment-URI (the stuff after the #) is not sent to the server by all browsers, thus the CAS client can't capture it as part of the URL.

An example application that is protected by the client is [available here](https://github.com/UniconLabs/cas-sample-java-webapp).

#### org.jasig.cas.client.authentication.AuthenticationFilter
The `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.jasig.cas.client.authentication.AuthenticationFilter</filter-class>
  <init-param>
    <param-name>casServerLoginUrl</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas/login</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
</filter>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerLoginUrl` | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes
| `service` | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html` | No
| `renew` | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `gateway ` | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all) | No
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `ticket`). | No
| `serviceParameterName ` | specifies the name of the request parameter on where to find the service (i.e. `service`) | No


#### org.jasig.cas.client.authentication.Saml11AuthenticationFilter
The SAML 1.1 `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.jasig.cas.client.authentication.Saml11AuthenticationFilter</filter-class>
  <init-param>
    <param-name>casServerLoginUrl</param-name>
    <param-value>https://somewhere.cas.edu:8443/cas/login</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.the-client.com</param-value>
  </init-param>
 </filter>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerLoginUrl` | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes
| `service` | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html` | No
| `renew` | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `gateway ` | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all) | No
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `SAMLart`). | No
| `serviceParameterName ` | specifies the name of the request parameter on where to find the service (i.e. `TARGET`) | No


####org.jasig.cas.client.validation.Cas10TicketValidationFilter
Validates tickets using the CAS 1.0 Protocol.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.jasig.cas.client.validation.Cas10TicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://somewhere.cas.edu:8443/cas</param-value>
  </init-param>
</filter>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | Whether to throw an exception or not on ticket validation failure. Defaults to `true`. | No


#### org.jasig.cas.client.validation.Saml11TicketValidationFilter
Validates tickets using the SAML 1.1 protocol.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.jasig.cas.client.validation.Saml11TicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
 </filter>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | whether to throw an exception or not on ticket validation failure. Defaults to `true` | No
| `tolerance ` | The tolerance for drifting clocks when validating SAML tickets. Note that 10 seconds should be more than enough for most environments that have NTP time synchronization. Defaults to `1000 msec` | No


#### org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 2.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, a `Cas20ProxyTicketValidator` will be constructed. Otherwise a general `Cas20ServiceTicketValidator` will be constructed that does not accept proxy tickets. 

**Note**: If you are using proxy validation, you should place the `filter-mapping` of the validation filter before the authentication filter.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
</filter>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | whether to throw an exception or not on ticket validation failure. Defaults to `true` | No
| `proxyReceptorUrl ` | The URL to watch for `PGTIOU/PGT` responses from the CAS server. Should be defined from the root of the context. For example, if your application is deployed in `/cas-client-app` and you want the proxy receptor URL to be `/cas-client-app/my/receptor` you need to configure proxyReceptorUrl to be `/my/receptor`. | No
| `acceptAnyProxy ` | Specifies whether any proxy is OK. Defaults to `false`. | No
| `allowedProxyChains ` | Specifies the proxy chain. Each acceptable proxy chain should include a space-separated list of URLs. Each acceptable proxy chain should appear on its own line. | No
| `proxyCallbackUrl` | The callback URL to provide the CAS server to accept Proxy Granting Tickets. | No
| `proxyGrantingTicketStorageClass ` | Specify an implementation of the ProxyGrantingTicketStorage class that has a no-arg constructor. | No


##### Proxy Authentication vs. Distributed Caching
The client has support for clustering and distributing the TGT state among application nodes that are behind a load balancer. In order to do so, the parameter needs to be defined as such for the filter.

###### Ehcache

Include the following dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-support-distributed-ehcache</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

Configure the client:

```xml
<init-param>
  <param-name>proxyGrantingTicketStorageClass</param-name>
  <param-value>org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl</param-value>
</init-param>
```
The setting provides an implementation for proxy storage using EhCache to take advantage of its replication features so that the PGT is successfully replicated and shared among nodes, regardless which node is selected as the result of the load balancer rerouting. 

Configuration of this parameter is not enough. The EhCache configuration needs to enable the replication mechanism through once of its suggested ways. A sample of that configuration based on RMI replication can be found here. Please note that while the sample is done for a distributed ticket registry implementation, the basic idea and configuration should easily be transferable. 

When loading from the `web.xml`, the Jasig CAS Client relies on a series of default values, one of which being that the cache must be configured in the default location (i.e. `classpath:ehcache.xml`). 

```xml
<cacheManagerPeerProviderFactory class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
   properties="peerDiscovery=automatic,
   multicastGroupAddress=230.0.0.1, multicastGroupPort=4446"/>
 
<cacheManagerPeerListenerFactory class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory"/>
 
<cache
   name="org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl.cache"
   maxElementsInMemory="100"
   eternal="false"
   timeToIdleSeconds="100"
   timeToLiveSeconds="100"
   overflowToDisk="false">
   <cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory"/>
</cache>
```

###### Memcached
A similar implementation based on Memcached is also available:

Include the following dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-support-distributed-memcached</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

Configure the client:

```xml
<init-param>
  <param-name>proxyGrantingTicketStorageClass</param-name>
  <param-value>org.jasig.cas.client.proxy. MemcachedBackedProxyGrantingTicketStorageImpl</param-value>
</init-param>
```

When loading from the `web.xml`, the Client relies on a series of default values, one of which being that the list of memcached servers must be defined in `/cas/casclient_memcached_hosts.txt` on the classpath). The file is a simple list of `<hostname>:<ports>` on separate lines. **BE SURE NOT TO HAVE EXTRA LINE BREAKS**.

#### org.jasig.cas.client.util.HttpServletRequestWrapperFilter
Wraps an `HttpServletRequest` so that the `getRemoteUser` and `getPrincipal` return the CAS related entries.

```xml
<filter>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.HttpServletRequestWrapperFilter</filter-class>
</filter>
```

#### org.jasig.cas.client.util.AssertionThreadLocalFilter
Places the `Assertion` in a `ThreadLocal` for portions of the application that need access to it. This is useful when the Web application that this filter "fronts" needs to get the Principal name, but it has no access to the `HttpServletRequest`, hence making `getRemoteUser()` call impossible.

```xml
<filter>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.AssertionThreadLocalFilter</filter-class>
</filter>
```

### Client Configuration Using Spring

Configuration via Spring IoC will depend heavily on `DelegatingFilterProxy` class. For each filter that will be configured for CAS via Spring, a corresponding `DelegatingFilterProxy` is needed in the web.xml.

As the `SingleSignOutFilter`, `HttpServletRequestWrapperFilter` and `AssertionThreadLocalFilter` have no configuration options, we recommend you just configure them in the `web.xml`

```xml
<filter>
    <filter-name>CAS Authentication Filter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    <init-param>
        <param-name>targetBeanName</param-name>
        <param-value>authenticationFilter</param-value>
    </init-param>
  </filter>
<filter-mapping>
    <filter-name>CAS Authentication Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

#### Bean Configuration

##### AuthenticationFilter
```xml
<bean
    name="authenticationFilter"
    class="org.jasig.cas.client.authentication.AuthenticationFilter"
    p:casServerLoginUrl="https://localhost:8443/cas/login"
    p:renew="false"
    p:gateway="false"
    p:service="https://my.local.service.com/cas-client" />
```

##### Cas10TicketValidationFilter
```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Cas10TicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.jasig.cas.client.validation.Cas10TicketValidator">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

##### Saml11TicketValidationFilter
```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Saml11TicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.jasig.cas.client.validation.Saml11TicketValidator">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

##### Cas20ProxyReceivingTicketValidationFilter
Configuration to validate tickets:
```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

Configuration to accept a Proxy Granting Ticket:
```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean
            class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator"
            p:proxyCallbackUrl="/proxy/receptor">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

Configuration to accept any Proxy Ticket (and Proxy Granting Tickets):

```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean class="org.jasig.cas.client.validation.Cas20ProxyTicketValidator"
            p:acceptAnyProxy="true"
            p:proxyCallbackUrl="/proxy/receptor">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

Configuration to accept Proxy Ticket from a chain (and Proxy Granting Tickets):

```xml
<bean
    name="ticketValidationFilter"
    class="org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean class="org.jasig.cas.client.validation.Cas20ProxyTicketValidator"
            p:proxyCallbackUrl="/proxy/receptor">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
            <property name="allowedProxyChains">
                <list>
                    <value>http://proxy1 http://proxy2</value>
                </list>
            </property>
        </bean>
    </property>
</bean>
```

The specific filters can be configured in the following ways. Please see the JavaDocs included in the distribution for specific required and optional properties:


### Client Configuration Using JNDI

Configuring the CAS client via JNDI is essentially the same as configuring the client via the `web.xml`, except the properties will reside in JNDI and not in the `web.xml`.
All properties that are placed in JNDI should be placed under `java:comp/env/cas`

We use the following conventions:
1. JNDI will first look in `java:comp/env/cas/{SHORT FILTER NAME}/{PROPERTY NAME}` (i.e. `java:comp/env/cas/AuthenticationFilter/serverName`)
2. JNDI will as a last resort look in `java:comp/env/cas/{PROPERTY NAME}` (i.e. `java:comp/env/cas/serverName`)

#### Example
This is an update to the `META-INF/context.xml` that is included in Tomcat's Manager application:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context antiResourceLocking="false" privileged="true">
 
<Environment description="Server Name" name="cas/serverName" override="false"
type="java.lang.String" value="http://localhost:8080"/>
 
<Environment description="CAS Login Url" name="cas/AuthenticationFilter/casServerLoginUrl" override="false"
type="java.lang.String" value="https://www.apereo.org/cas/login"/>
 
<Environment description="CAS Url Prefix" name="cas/Cas20ProxyReceivingTicketValidationFilter/casServerUrlPrefix" override="false"
type="java.lang.String" value="https://www.apereo.org/cas"/>
</Context>
```

### Configuring Single Sign Out
The Single Sign Out support in CAS consists of configuring one `SingleSignOutFilter` and one `ContextListener`. Please note that if you have configured the CAS Client for Java as Web filters, this filter must come before the other filters as described.

The `SingleSignOutFilter` can affect character encoding. This becomes most obvious when used in conjunction with Confluence. Its recommended you explicitly configure either the [VT Character Encoding Filter](http://code.google.com/p/vt-middleware/wiki/vtservletfilters#CharacterEncodingFilter) or the [Spring Character Encoding Filter](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/filter/CharacterEncodingFilter.html) with explicit encodings.

#### CAS Protocol

```xml
<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
</filter>
...
<filter-mapping>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
...
<listener>
    <listener-class>org.jasig.cas.client.session.SingleSignOutHttpSessionListener</listener-class>
</listener>
```

#### SAML Protocol

```xml
filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
   <init-param>
      <param-name>artifactParameterName</param-name>
      <param-value>SAMLart</param-value>
   </init-param>
</filter>
...
<filter-mapping>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
...
<listener>
    <listener-class>org.jasig.cas.client.session.SingleSignOutHttpSessionListener</listener-class>
</listener>
```

#### Recommend Logout Procedure
The client has no code to help you handle log out. The client merely places objects in session. Therefore, we recommend you do a `session.invalidate()` call when you log a user out. However, that's entirely your application's responsibility. We recommend that text similar to the following appear when the application's session is ended.

```html
You have been logged out of [APPLICATION NAME GOES HERE].
To log out of all applications, click here. (provide link to CAS server's logout)
```

## JBoss Integration

## Tomcat 6 Integration

## Tomcat 7 Integration

## Confluence Integration

## Build

```bash
git clone git@github.com:Jasig/java-cas-client.git
cd java-cas-client
mvn clean package
```

Please note that to be deployed in Maven Central, we mark a number of JARs as provided (related to JBoss and Memcache
Clients).  In order to build the clients, you must enable the commented out repositories in the appropriate `pom.xml`
files in the modules (cas-client-integration-jboss and cas-client-support-distributed-memcached) or follow the instructions on how to install the file manually.
