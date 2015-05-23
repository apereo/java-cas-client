# Java Apereo CAS Client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server)

## Intro
This is the official home of the Java Apereo CAS client. The client consists of a collection of Servlet filters that are suitable for most Java-based web applications. It also serves as an API platform to interact with the CAS server programmatically to make authentication requests, validate tickets and consume principal attributes.

All client artifacts are published to Maven central. Depending on functionality, applications will need include one or more of the listed dependencies in their configuration.

## Components

- Core functionality, which includes CAS authentication/validation filters.

```xml
<dependency>
	<groupId>org.jasig.cas.client</groupId>
	<artifactId>cas-client-core</artifactId>
	<version>${java.cas.client.version}</version>
</dependency>
```

- Support for SAML functionality is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-support-saml</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Ehcache is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-support-distributed-ehcache</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Memcached is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-support-distributed-memcached</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Atlassian integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-integration-atlassian</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- JBoss integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-integration-jboss</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 6 integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-integration-tomcat-v6</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 7 is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-client-integration-tomcat-v7</artifactId>
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
A similar implementation based on Memcached is also available.

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

The `SingleSignOutFilter` can affect character encoding. This becomes most obvious when used in conjunction with applications such as Atlassian Confluence. Its recommended you explicitly configure either the [VT Character Encoding Filter](http://code.google.com/p/vt-middleware/wiki/vtservletfilters#CharacterEncodingFilter) or the [Spring Character Encoding Filter](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/filter/CharacterEncodingFilter.html) with explicit encodings.

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

## JAAS
The client supports the Java Authentication and Authorization Service (JAAS) framework, which provides authnz facilities to CAS-enabled JEE applications.

A general JAAS authentication module, `CasLoginModule`, is available with the specific purpose of providing authentication and authorization services to CAS-enabled JEE applications. The design of the module is simple: given a service URL and a service ticket in a `NameCallback` and `PasswordCallback`, respectively, the module contacts the CAS server and attempts to validate the ticket. In keeping with CAS integration for Java applications, a JEE container-specific servlet filter is needed to protect JEE Web applications. The JAAS support should be extensible to any JEE container.

### Configure CasLoginModule
It is expected that for JEE applications both authentication and authorization services will be required for CAS integration. The following JAAS module configuration file excerpt demonstrates how to leverage SAML 1.1 attribute release in CAS to provide authorization data in addition to authentication:

```json
cas {
  org.jasig.cas.client.jaas.CasLoginModule required
    ticketValidatorClass="org.jasig.cas.client.validation.Saml11TicketValidator"
    casServerUrlPrefix="https://cas.example.com/cas"
    tolerance="20000"
    service="https://webapp.example.com/webapp"
    defaultRoles="admin,operator"
    roleAttributeNames="memberOf,eduPersonAffiliation"
    principalGroupName="CallerPrincipal"
    roleGroupName="Roles"
    cacheAssertions="true"
    cacheTimeout="480";
}
```


| Property | Description | Required
|----------|-------|-----------|
| `ticketValidatorClass ` | Fully-qualified class name of CAS ticket validator class. | Yes
| `casServerUrlPrefix` | URL to root of CAS Web application context. | Yes
| `service` | CAS service parameter that may be overridden by callback handler. **Note**: service must be specified by at least one component such that it is available at service ticket validation time. | No
| `defaultRoles` | Comma-delimited list of static roles applied to all authenticated principals. | No
| `roleAttributeNames` | Comma-delimited list of attribute names that describe role data delivered to CAS in the service-ticket validation response that should be applied to the current authenticated principal. | No
| `principalGroupName` | The name of a group principal containing the primary principal name of the current JAAS subject. The default value is `CallerPrincipal`. | No
| `roleGroupName` | The name of a group principal containing all role data. The default value is `Roles`. | No
| `cacheAssertions` | Flag to enable assertion caching. This may be required for JAAS providers that attempt to periodically reauthenticate to renew principal. Since CAS tickets are one-time-use, a cached assertion must be provided on reauthentication. | No
| `cacheTimeout` | Assertion cache timeout in minutes. | No
| `tolerance` | The tolerance for drifting clocks when validating SAML tickets. | No


## JBoss Integration

In keeping with CAS integration for Java applications, a JEE container-specific servlet filter is needed to protect JEE Web applications. The JBoss `WebAuthenticationFilter` component provided a convenient integration piece between a servlet filter and the JAAS framework, so a complete integration solution is available only for JBoss AS versions that provide the `WebAuthenticationFilter` class. The JAAS support should be extensible to any JEE container with additional development.

For JBoss it is vitally important to use the correct values for `principalGroupName` and `roleGroupName`. Additionally, the `cacheAssertions` and `cacheTimeout` are required since JBoss by default attempts to reauthenticate the JAAS principal with a fairly aggressive default timeout. Since CAS tickets are single-use authentication tokens by default, assertion caching is required to support periodic reauthentication.

### Configure Servlet Filters

Integration with the servlet pipeline is required for a number of purposes:

1. Examine servlet request for an authenticated session
2. Redirect to CAS server for unauthenticated sessions
3. Provide service URL and CAS ticket to JAAS pipeline for validation

The `WebAuthenticationFilter` performs these operations for the JBoss AS container. It is important to note that this filter simply collects the service URL and CAS ticket from the request and passes it to the JAAS pipeline. It is assumed that the `CasLoginModule` will be present in the JAAS pipeline to consume the data and perform ticket validation. The following web.xml excerpts demonstrate how to integrate WebAuthenticationFilter into a JEE Web application.


```xml
...
<filter>
	<filter-name>CASWebAuthenticationFilter</filter-name>
	<filter-class>org.jasig.cas.client.jboss.authentication.WebAuthenticationFilter</filter-class>
</filter>

<filter>
	<filter-name>CASAuthenticationFilter</filter-name>
	<filter-class>org.jasig.cas.client.authentication.AuthenticationFilter</filter-class>
	<init-param>
	  <param-name>casServerLoginUrl</param-name>
	  <param-value>https://cas.example.com/cas/login</param-value>
	</init-param>
</filter>
...
```

The JAAS LoginModule configuration in `conf/login-config.xml` may require the following changes in a JBoss environment:

```xml
<application-policy name="cas">
   <authentication>
      <login-module code="org.jasig.cas.client.jaas.CasLoginModule" flag="required">
         <module-option name="ticketValidatorClass">org.jasig.cas.client.validation.Saml11TicketValidator</module-option>
         <module-option name="casServerUrlPrefix">http://yourcasserver/cas</module-option>
         <module-option name="tolerance">20000</module-option>
         <module-option name="defaultRoles">admin,user</module-option>
         <module-option name="roleAttributeNames">memberOf,eduPersonAffiliation,authorities</module-option>
         <module-option name="principalGroupName">CallerPrincipal</module-option>
         <module-option name="roleGroupName">Roles</module-option>
         <module-option name="cacheAssertions">true</module-option>
         <module-option name="cacheTimeout">480</module-option>
      </login-module>
   </authentication>
</application-policy>
```
It may be necessary to modify the JBoss `server.xml` and uncomment:

```xml
<Valve className="org.apache.catalina.authenticator.SingleSignOn" />
```

Remember not to add `<security-constraint>` and `<login-config>` elements in your `web.xml`.

If you have any trouble, you can enable the log of cas in `jboss-logging.xml` by adding:

```xml
<logger category="org.jasig">
   <level name="DEBUG" />
</logger>
``` 

## Tomcat 6 Integration

## Tomcat 7 Integration

## Atlassian Integration
The clien includes Atlassian Confluence and JIRA support. Support is enabled by a custom CAS authenticator that extends the default authenticators.

### Configuration

#### $JIRA_HOME Location
 
- WAR/EAR Installation: <extracted archive directory>/webapp
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z/webapp`

- Standalone: <extracted archive directory>/atlassian-jira
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z-standalone/atlassian-jira`

#### $CONFLUENCE_INSTALL Description

- <extracted archive directory>/confluence
`/opt/atlassian/confluence/confluence-x.y.z/confluence`

#### Changes to web.xml
Add the CAS filters to the end of the filter list. See `web.xml` configuration of the client.


#### Modify the seraph-config.xml
To rely on the Single Sign Out functionality to sign off of Jira, comment out the normal logout URL and replace it with the CAS logout URL. Also, change the login links to point to the CAS login service.

```xml
<init-param>
    <!--
      The login URL to redirect to when the user tries to access a protected resource (rather than clicking on
      an explicit login link). Most of the time, this will be the same value as 'link.login.url'.
    - if the URL is absolute (contains '://'), then redirect that URL (for SSO applications)
    - else the context path will be prepended to this URL
 
    If '${originalurl}' is present in the URL, it will be replaced with the URL that the user requested.
    This gives SSO login pages the chance to redirect to the original page
    -->
    <param-name>login.url</param-name>
    <!--<param-value>/login.jsp?os_destination=${originalurl}</param-value>-->
    <param-value>http://cas.institution.edu/cas/login?service=${originalurl}</param-value>
</init-param>
<init-param>
    <!--
      the URL to redirect to when the user explicitly clicks on a login link (rather than being redirected after
      trying to access a protected resource). Most of the time, this will be the same value as 'login.url'.
    - same properties as login.url above
    -->
    <param-name>link.login.url</param-name>
    <!--<param-value>/login.jsp?os_destination=${originalurl}</param-value>-->
    <!--<param-value>/secure/Dashboard.jspa?os_destination=${originalurl}</param-value>-->
    <param-value>http://cas.institution.edu/cas/login?service=${originalurl}</param-value>
</init-param>
<init-param>
    <!-- URL for logging out.
    - If relative, Seraph just redirects to this URL, which is responsible for calling Authenticator.logout().
    - If absolute (eg. SSO applications), Seraph calls Authenticator.logout() and redirects to the URL
    -->
    <param-name>logout.url</param-name>
    <!--<param-value>/secure/Logout!default.jspa</param-value>-->
    <param-value>https://cas.institution.edu/cas/logout</param-value>
</init-param>
```

#### CAS Authenticator
Comment out the `DefaultAuthenticator` like so in `[$JIRA_HOME|$CONFLUENCE_INSTALL]/WEB-INF/classes/seraph-config.xml`:

```xml
<!-- CROWD:START - The authenticator below here will need to be commented out for Crowd SSO integration -->
<!--
<authenticator class="com.atlassian.seraph.auth.DefaultAuthenticator"/>
-->
<!-- CROWD:END -->
```

For JIRA, add in the Client Jira Authenticator:

```xml
<!-- CAS:START - Java Client Jira Authenticator -->
<authenticator class="org.jasig.cas.client.integration.atlassian.JiraCasAuthenticator"/>
<!-- CAS:END -->
```

For Confluence, add in the Client Confluence Authenticator:

```xml
<!-- CAS:START - Java Client Confluence Authenticator -->
<authenticator class="org.jasig.cas.client.integration.atlassian.ConfluenceCasAuthenticator"/>
<!-- CAS:END -->
```

#### Confluence CAS Logout

As of this writing, Atlassian doesn't support a config option yet (like Jira). To rely on the Single Sign Out functionality to sign off of Confluence we need to modify the logout link.


- Copy `$CONFLUENCE_INSTALL/WEB-INF/lib/confluence-x.x.x.jar` to a temporary directory
- `mkdir /tmp/confluence-jar && cp WEB-INF/lib/confluence-x.y.z.jar /tmp/confluence-jar`
- Unpack the jar
- `cd /tmp/confluence-jar && jar xvf confluence-x.y.z.jar`
- `cp xwork.xml $CONFLUENCE_INSTALL/WEB-INF/classes`
- `cp xwork.xml $CONFLUENCE_INSTALL/WEB-INF/classes/ && cd $CONFLUENCE_INSTALL/WEB-INF/classes/`
- Edit `$CONFLUENCE_INSTALL/WEB-INF/classes/xwork.xml`, find the logout action and comment out the success result and replace it with this one:

```xml
<!-- <result name="success" type="velocity">/logout.vm</result> -->
<!-- CAS:START - CAS Logout Redirect -->
<result name="success" type="redirect">https://cas.institution.edu/cas/logout</result>
<!-- CAS:END -->
```

#### Copy Jars
Copy cas-client-core-x.y.x.jar and cas-client-integration-atlassian-x.y.x.jar to `$JIRA_HOME/WEB-INF/lib`

## Spring Security Integration
This configuration tested against the sample application that is included with Spring Security. As of this writing, replacing the `applicationContext-security.xml` in the sample application with the one below would enable this alternative configuration. We can not guarantee this version will work without modification in future versions of Spring Security.

### Changes to web.xml

```xml
...
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
        /WEB-INF/applicationContext-security.xml
    </param-value>
</context-param>

<context-param>
    <param-name>log4jConfigLocation</param-name>
    <param-value>/WEB-INF/classes/log4j.properties</param-value>
</context-param>

<context-param>
    <param-name>webAppRootKey</param-name>
    <param-value>cas.root</param-value>
</context-param>

<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
</filter>

<filter>
    <filter-name>springSecurityFilterChain</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>

<filter-mapping>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>

<filter-mapping>
  <filter-name>springSecurityFilterChain</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>

<listener>
    <listener-class>org.jasig.cas.client.session.SingleSignOutHttpSessionListener</listener-class>
</listener>

<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<listener>
    <listener-class>org.springframework.web.util.Log4jConfigListener</listener-class>
</listener>

<error-page>
    <error-code>403</error-code>
    <location>/casfailed.jsp</location>
</error-page>
...
```

The important additions to the `web.xml` include the addition of the 403 error page. 403 is what the CAS Validation Filter will throw if it has a problem with the ticket. Also, if you want Single Log Out, you should enable the `SingleSignOutHttpSessionListener`.

### Changes to applicationContext-security.xml

```xml
...
<bean id="springSecurityFilterChain" class="org.springframework.security.web.FilterChainProxy">
    <sec:filter-chain-map path-type="ant">
        <sec:filter-chain pattern="/" filters="casValidationFilter, wrappingFilter" />
        <sec:filter-chain pattern="/secure/receptor" filters="casValidationFilter" />
        <sec:filter-chain pattern="/j_spring_security_logout" filters="logoutFilter,etf,fsi" />
        <sec:filter-chain pattern="/**" filters="casAuthenticationFilter, casValidationFilter, wrappingFilter, sif,j2eePreAuthFilter,logoutFilter,etf,fsi"/>
    </sec:filter-chain-map>
</bean>

<bean id="sif" class="org.springframework.security.web.context.SecurityContextPersistenceFilter"/>

<sec:authentication-manager alias="authenticationManager">
    <sec:authentication-provider ref="preAuthAuthProvider"/>
</sec:authentication-manager>

 <bean id="preAuthAuthProvider" class="org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider">
    <property name="preAuthenticatedUserDetailsService">
        <bean id="userDetailsServiceWrapper" class="org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper">
            <property name="userDetailsService" ref="userService"/>
        </bean>
    </property>
</bean>

<bean id="preAuthEntryPoint" class="org.springframework.security.web.authentication.Http403ForbiddenEntryPoint" />

<bean id="j2eePreAuthFilter" class="org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter">
    <property name="authenticationManager" ref="authenticationManager"/>
    <property name="authenticationDetailsSource">
        <bean class="org.springframework.security.web.authentication.WebAuthenticationDetailsSource" />
    </property>
</bean>

<bean id="logoutFilter" class="org.springframework.security.web.authentication.logout.LogoutFilter">
    <constructor-arg value="/"/>
    <constructor-arg>
        <list>
            <bean class="org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler"/>
        </list>
    </constructor-arg>
</bean>

<bean id="servletContext" class="org.springframework.web.context.support.ServletContextFactoryBean"/>

<bean id="etf" class="org.springframework.security.web.access.ExceptionTranslationFilter">
    <property name="authenticationEntryPoint" ref="preAuthEntryPoint"/>
</bean>

<bean id="httpRequestAccessDecisionManager" class="org.springframework.security.access.vote.AffirmativeBased">
    <property name="allowIfAllAbstainDecisions" value="false"/>
    <property name="decisionVoters">
        <list>
            <ref bean="roleVoter"/>
        </list>
    </property>
</bean>

<bean id="fsi" class="org.springframework.security.web.access.intercept.FilterSecurityInterceptor">
    <property name="authenticationManager" ref="authenticationManager"/>
    <property name="accessDecisionManager" ref="httpRequestAccessDecisionManager"/>
    <property name="securityMetadataSource">
        <sec:filter-invocation-definition-source>
            <sec:intercept-url pattern="/secure/extreme/**" access="ROLE_SUPERVISOR"/>
            <sec:intercept-url pattern="/secure/**" access="ROLE_USER"/>
            <sec:intercept-url pattern="/**" access="ROLE_USER"/>
        </sec:filter-invocation-definition-source>
    </property>
</bean>

<bean id="roleVoter" class="org.springframework.security.access.vote.RoleVoter"/>

<bean id="securityContextHolderAwareRequestFilter" class="org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter"/>
 
<bean class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator" id="ticketValidator">
    <constructor-arg index="0" value="https://localhost:9443/cas" />
    <property name="proxyGrantingTicketStorage" ref="proxyGrantingTicketStorage" />
    <property name="proxyCallbackUrl" value="https://localhost:8443/cas-sample/secure/receptor" />
</bean>

<bean id="proxyGrantingTicketStorage" class="org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl" />

<sec:user-service id="userService">
    <sec:user name="rod" password="rod" authorities="ROLE_SUPERVISOR,ROLE_USER" />
    <sec:user name="dianne" password="dianne" authorities="ROLE_USER" />
    <sec:user name="scott" password="scott" authorities="ROLE_USER" />
</sec:user-service>

<bean id="casAuthenticationFilter" class="org.jasig.cas.client.authentication.AuthenticationFilter">
    <property name="casServerLoginUrl" value="https://localhost:9443/cas/login" />
    <property name="serverName" value="https://localhost:8443" />
</bean>

<bean id="casValidationFilter" class="org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter">
    <property name="serverName" value="https://localhost:8443" />
    <property name="exceptionOnValidationFailure" value="true" />
    <property name="proxyGrantingTicketStorage" ref="proxyGrantingTicketStorage" />
    <property name="redirectAfterValidation" value="true" />
    <property name="ticketValidator" ref="ticketValidator" />
    <property name="proxyReceptorUrl" value="/secure/receptor" />
</bean>

<bean id="wrappingFilter" class="org.jasig.cas.client.util.HttpServletRequestWrapperFilter" />
...
```

1. You should replace the userService with something that checks your user storage.
2. Replace the `serverName` and `casServerLoginUrl` with your values (or better yet, externalize them).
3. Replace the URLs with the URL configuration for your application.

## Build

```bash
git clone git@github.com:Jasig/java-cas-client.git
cd java-cas-client
mvn clean package
```

Please note that to be deployed in Maven Central, we mark a number of JARs as provided (related to JBoss and Memcache
Clients).  In order to build the clients, you must enable the commented out repositories in the appropriate `pom.xml`
files in the modules (cas-client-integration-jboss and cas-client-support-distributed-memcached) or follow the instructions on how to install the file manually.
