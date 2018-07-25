# Java Apereo CAS Client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client)

<a name="intro"></a>
## Intro
This is the official home of the Java Apereo CAS client. The client consists of a collection of Servlet filters that are suitable for most Java-based web applications. It also serves as an API platform to interact with the CAS server programmatically to make authentication requests, validate tickets and consume principal attributes.

All client artifacts are published to Maven central. Depending on functionality, applications will need include one or more of the listed dependencies in their configuration.

<a name="build"></a>
## Build [![Build Status](https://travis-ci.org/apereo/java-cas-client.png?branch=master)](https://travis-ci.org/apereo/java-cas-client)

```bash
git clone git@github.com:apereo/java-cas-client.git
cd java-cas-client
mvn clean package
```

Please note that to be deployed in Maven Central, we mark a number of JARs as provided (related to JBoss and Memcache
Clients).  In order to build the clients, you must enable the commented out repositories in the appropriate `pom.xml`
files in the modules (`cas-client-integration-jboss` and `cas-client-support-distributed-memcached`) or follow the instructions on how to install the file manually.

<a name="components"></a>
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
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-support-saml</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Ehcache is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-support-distributed-ehcache</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Memcached is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-support-distributed-memcached</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Atlassian integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-atlassian</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- JBoss integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-jboss</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 6 integration is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-tomcat-v6</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 7 is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-tomcat-v7</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 8.0.x is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-tomcat-v8</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 8.5.x is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-tomcat-v85</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

<a name="configurtion"></a>
## Configuration

### Strategies
The client provides multiple strategies for the deployer to provide client settings. The following strategies are supported:

- JNDI (`JNDI`)
- Properties File (`PROPERTY_FILE`). The configuration is provided via an external properties file. The path may be specified in the web context as such:

```xml
<context-param>
    <param-name>configFileLocation</param-name>
    <param-value>/etc/cas/file.properties</param-value>
</context-param>
```
If no location is specified, by default `/etc/java-cas-client.properties` will be used.

- System Properties (`SYSTEM_PROPERTIES`)
- Web Context (`WEB_XML`)
- Default (`DEFAULT`)

In order to instruct the client to pick a strategy, strategy name must be specified in the web application's context:

```xml
<context-param>
    <param-name>configurationStrategy</param-name>
    <param-value>DEFAULT</param-value>
</context-param>
```

If no `configurationStrategy` is defined, `DEFAULT` is used which is a combination of `WEB_XML` and `JNDI`. 

<a name="client-configuration-using-webxml"></a>
### Client Configuration Using `web.xml`

The client can be configured in `web.xml` via a series of `context-param`s and filter `init-param`s. Each filter for the client has a required (and optional) set of properties. The filters are designed to look for these properties in the following way:

- Check the filter's local `init-param`s for a parameter matching the required property name.
- Check the `context-param`s for a parameter matching the required property name.
- If two properties are found with the same name in the `init-param`s and the `context-param`s, the `init-param` takes precedence. 

**Note**: If you're using the `serverName` property, you should note well that the fragment-URI (the stuff after the #) is not sent to the server by all browsers, thus the CAS client can't capture it as part of the URL.

An example application that is protected by the client is [available here](https://github.com/UniconLabs/cas-sample-java-webapp).

<a name="orgjasigcasclientauthenticationauthenticationfilter"></a>
#### org.jasig.cas.client.authentication.AuthenticationFilter
The `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.jasig.cas.client.authentication.AuthenticationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
</filter>
<filter-mapping>
    <filter-name>CAS Authentication Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes (unless `casServerLoginUrl` is set)
| `casServerLoginUrl` | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login`. This overrides `casServerUrlPrefix`, if set. | Yes (unless `casServerUrlPrefix` is set)
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes
| `service` | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html` | No
| `renew` | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `gateway ` | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all) | No
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `ticket`). | No
| `serviceParameterName ` | specifies the name of the request parameter on where to find the service (i.e. `service`) | No
| `encodeServiceUrl ` | Whether the client should auto encode the service url. Defaults to `true` | No
| `ignorePattern` | Defines the url pattern to ignore, when intercepting authentication requests. | No
| `ignoreUrlPatternType` | Defines the type of the pattern specified. Defaults to `REGEX`. Other types are `CONTAINS`, `EXACT`. | No
| `gatewayStorageClass` | The storage class used to record gateway requests | No
| `authenticationRedirectStrategyClass` | The class name of the component to decide how to handle authn redirects to CAS | No

<a name="orgjasigcasclientauthenticationsaml11authenticationfilter"></a>
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
<filter-mapping>
    <filter-name>CAS Authentication Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes (unless `casServerLoginUrl` is set)
| `casServerLoginUrl` | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login`. This overrides `casServerUrlPrefix`, if set. | Yes (unless `casServerUrlPrefix` is set)
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes
| `service` | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html` | No
| `renew` | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `gateway ` | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all) | No
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `SAMLart`). | No
| `serviceParameterName ` | specifies the name of the request parameter on where to find the service (i.e. `TARGET`) | No
| `encodeServiceUrl ` | Whether the client should auto encode the service url. Defaults to `true` | No

<a name="rgjasigcasclientvalidationcas10ticketvalidationfilter"></a>
#### org.jasig.cas.client.validation.Cas10TicketValidationFilter
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
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | Whether to throw an exception or not on ticket validation failure. Defaults to `true`. | No
| `sslConfigFile` | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.
| `encoding` | Specifies the encoding charset the client should use | No
| `hostnameVerifier` | Hostname verifier class name, used when making back-channel calls | No

<a name="orgjasigcasclientvalidationsaml11ticketvalidationfilter"></a>
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
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
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
| `sslConfigFile` | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.
| `encoding` | Specifies the encoding charset the client should use | No
| `hostnameVerifier` | Hostname verifier class name, used when making back-channel calls | No

<a name="orgjasigcasclientvalidationcas20proxyreceivingticketvalidationfilter"></a>
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
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
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
| `allowedProxyChains ` | Specifies the proxy chain. Each acceptable proxy chain should include a space-separated list of URLs (for exact match) or regular expressions of URLs (starting by the `^` character). Each acceptable proxy chain should appear on its own line. | No
| `proxyCallbackUrl` | The callback URL to provide the CAS server to accept Proxy Granting Tickets. | No
| `proxyGrantingTicketStorageClass ` | Specify an implementation of the ProxyGrantingTicketStorage class that has a no-arg constructor. | No
| `sslConfigFile` | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.
| `encoding` | Specifies the encoding charset the client should use | No
| `secretKey` | The secret key used by the `proxyGrantingTicketStorageClass` if it supports encryption. | No
| `cipherAlgorithm` | The algorithm used by the `proxyGrantingTicketStorageClass` if it supports encryption. Defaults to `DESede` | No
| `millisBetweenCleanUps` | Startup delay for the cleanup task to remove expired tickets from the storage. Defaults to `60000 msec` | No
| `ticketValidatorClass` | Ticket validator class to use/create | No
| `hostnameVerifier` | Hostname verifier class name, used when making back-channel calls | No

#### org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 3.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, 
a `Cas30ProxyTicketValidator` will be constructed. Otherwise a general `Cas30ServiceTicketValidator` will be constructed that does not 
accept proxy tickets. Supports all configurations that are available for `Cas20ProxyReceivingTicketValidationFilter`.

#### org.jasig.cas.client.validation.json.Cas30JsonProxyReceivingTicketValidationFilter
Indentical to `Cas30ProxyReceivingTicketValidationFilter`, yet the filter is able to accept validation responses from CAS
that are formatted as JSON per guidelines laid out by the CAS protocol. 
See the [protocol documentation](https://apereo.github.io/cas/5.1.x/protocol/CAS-Protocol-Specification.html)
for more info.

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

<a name="orgjasigcasclientutilhttpservletrequestwrapperfilter"></a>
#### org.jasig.cas.client.util.HttpServletRequestWrapperFilter
Wraps an `HttpServletRequest` so that the `getRemoteUser` and `getPrincipal` return the CAS related entries.

```xml
<filter>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.HttpServletRequestWrapperFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `roleAttribute` | Used to determine the principal role. | No
| `ignoreCase` | Whether role checking should ignore case. Defaults to `false` | No

<a name="orgjasigcasclientutilassertionthreadlocalfilter"></a>
#### org.jasig.cas.client.util.AssertionThreadLocalFilter
Places the `Assertion` in a `ThreadLocal` for portions of the application that need access to it. This is useful when the Web application that this filter "fronts" needs to get the Principal name, but it has no access to the `HttpServletRequest`, hence making `getRemoteUser()` call impossible.

```xml
<filter>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.AssertionThreadLocalFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

<a name="orgjasigcasclientutilerrorredirectfilter"></a>
#### org.jasig.cas.client.util.ErrorRedirectFilter
Filters that redirects to the supplied url based on an exception.  Exceptions and the urls are configured via init filter name/param values.

| Property | Description | Required
|----------|-------|-----------
| `defaultErrorRedirectPage` | Default url to redirect to, in case no erorr matches are found. | Yes
| `java.lang.Exception` | Fully qualified exception name. Its value must be redirection url | No


```xml
<filter>
  <filter-name>CAS Error Redirect Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.ErrorRedirectFilter</filter-class>
  <init-param>
    <param-name>java.lang.Exception</param-name>
    <param-value>/error.jsp</param-value>
  </init-param>
  <init-param>
    <param-name>defaultErrorRedirectPage</param-name>
    <param-value>/defaulterror.jsp</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>CAS Error Redirect Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```


<a name="client-configuration-using-spring"></a>
### Client Configuration Using Spring

Configuration via Spring IoC will depend heavily on `DelegatingFilterProxy` class. For each filter that will be configured for CAS via Spring, a corresponding `DelegatingFilterProxy` is needed in the web.xml.

As the `HttpServletRequestWrapperFilter` and `AssertionThreadLocalFilter` have no configuration options, we recommend you just configure them in the `web.xml`

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

<a name="bean-configuration"></a>
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


<a name="client-configuration-using-jndi"></a>
### Client Configuration Using JNDI

Configuring the CAS client via JNDI is essentially the same as configuring the client via the `web.xml`, except the properties will reside in JNDI and not in the `web.xml`.
All properties that are placed in JNDI should be placed under `java:comp/env/cas`

We use the following conventions:
1. JNDI will first look in `java:comp/env/cas/{SHORT FILTER NAME}/{PROPERTY NAME}` (i.e. `java:comp/env/cas/AuthenticationFilter/serverName`)
2. JNDI will as a last resort look in `java:comp/env/cas/{PROPERTY NAME}` (i.e. `java:comp/env/cas/serverName`)

<a name="example"></a>
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

<a name="configuring-single-sign-out"></a>
### Configuring Single Sign Out
The Single Sign Out support in CAS consists of configuring one `SingleSignOutFilter` and one `ContextListener`. Please note that if you have configured the CAS Client for Java as Web filters, this filter must come before the other filters as described.

The `SingleSignOutFilter` can affect character encoding. This becomes most obvious when used in conjunction with applications such as Atlassian Confluence. Its recommended you explicitly configure either the [VT Character Encoding Filter](http://code.google.com/p/vt-middleware/wiki/vtservletfilters#CharacterEncodingFilter) or the [Spring Character Encoding Filter](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/filter/CharacterEncodingFilter.html) with explicit encodings.

#### Configuration

| Property | Description | Required
|----------|-------|-----------
| `artifactParameterName` | The ticket artifact parameter name. Defaults to `ticket`| No
| `logoutParameterName` | Defaults to `logoutRequest` | No
| `frontLogoutParameterName` | Defaults to `SAMLRequest` | No
| `relayStateParameterName` | Defaults to `RelayState` | No
| `eagerlyCreateSessions` | Defaults to `true` | No
| `artifactParameterOverPost` | Defaults to  `false` | No
| `logoutCallbackPath` | The path which is expected to receive logout callback requests from the CAS server. This is necessary if your app needs access to the raw input stream when handling form posts. If not configured, the default behavior will check every form post for a logout parameter. | No
| `casServerUrlPrefix` | URL to root of CAS Web application context. | Yes

<a name="cas-protocol"></a>
#### CAS Protocol

```xml
<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
   <init-param>
      <param-name>casServerUrlPrefix</param-name>
      <param-value>https://cas.example.com/cas</param-value>
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

<a name="saml-protocol"></a>
#### SAML Protocol

```xml
<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
   <init-param>
      <param-name>artifactParameterName</param-name>
      <param-value>SAMLart</param-value>
   </init-param>
   <init-param>
      <param-name>casServerUrlPrefix</param-name>
      <param-value>https://cas.example.com/cas</param-value>
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



<a name="recommend-logout-procedure"></a>
#### Recommend Logout Procedure
The client has no code to help you handle log out. The client merely places objects in session. Therefore, we recommend you do a `session.invalidate()` call when you log a user out. However, that's entirely your application's responsibility. We recommend that text similar to the following appear when the application's session is ended.

```html
You have been logged out of [APPLICATION NAME GOES HERE].
To log out of all applications, click here. (provide link to CAS server's logout)
```

<a name="jaas"></a>
## JAAS
The client supports the Java Authentication and Authorization Service (JAAS) framework, which provides authnz facilities to CAS-enabled JEE applications.

A general JAAS authentication module, `CasLoginModule`, is available with the specific purpose of providing authentication and authorization services to CAS-enabled JEE applications. The design of the module is simple: given a service URL and a service ticket in a `NameCallback` and `PasswordCallback`, respectively, the module contacts the CAS server and attempts to validate the ticket. In keeping with CAS integration for Java applications, a JEE container-specific servlet filter is needed to protect JEE Web applications. The JAAS support should be extensible to any JEE container.

<a name="configure-casloginmodule"></a>
### Configure CasLoginModule
It is expected that for JEE applications both authentication and authorization services will be required for CAS integration. The following JAAS module configuration file excerpt demonstrates how to leverage SAML 1.1 attribute release in CAS to provide authorization data in addition to authentication:

```
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

### Programmatic JAAS login using the Servlet 3
A `org.jasig.cas.client.jaas.Servlet3AuthenticationFilter` servlet filter that performs a programmatic JAAS login using the Servlet 3.0 `HttpServletRequest#login()` facility. This component should be compatible with any servlet container that supports the Servlet 3.0/JEE6 specification.
 
The filter executes when it receives a CAS ticket and expects the
`CasLoginModule` JAAS module to perform the CAS ticket validation in order to produce an `AssertionPrincipal` from which the CAS assertion is obtained and inserted into the session to enable SSO.

If a `service` init-param is specified for this filter, it supersedes
the service defined for the `CasLoginModule`.
 
<a name="jboss-integration"></a>
## JBoss Integration

In keeping with CAS integration for Java applications, a JEE container-specific servlet filter is needed to protect JEE Web applications. The JBoss `WebAuthenticationFilter` component provided a convenient integration piece between a servlet filter and the JAAS framework, so a complete integration solution is available only for JBoss AS versions that provide the `WebAuthenticationFilter` class. The JAAS support should be extensible to any JEE container with additional development.

For JBoss it is vitally important to use the correct values for `principalGroupName` and `roleGroupName`. Additionally, the `cacheAssertions` and `cacheTimeout` are required since JBoss by default attempts to reauthenticate the JAAS principal with a fairly aggressive default timeout. Since CAS tickets are single-use authentication tokens by default, assertion caching is required to support periodic reauthentication.

<a name="configure-servlet-filters"></a>
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
<!-- one filter-mapping for each filter as seen in the examples above -->
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

<a name="tomcat-678-integration"></a>
## Tomcat 6/7/8 Integration
The client supports container-based CAS authentication and authorization support for the Tomcat servlet container. 

Suppose a single Tomcat container hosts multiple Web applications with similar authentication and authorization needs. Prior to Tomcat container support, each application would require a similar configuration of CAS servlet filters and authorization configuration in the `web.xml` servlet descriptor. Using the new container-based authentication/authorization feature, a single CAS configuration can be applied to the container and leveraged by all Web applications hosted by the container.

CAS authentication support for Tomcat is based on the Tomcat-specific Realm component. The Realm component has a fairly broad surface area and RealmBase is provided as a convenient superclass for custom implementations; the CAS realm implementations derive from `RealmBase`. Unfortunately RealmBase and related components have proven to change over both major and minor number releases, which requires version-specific CAS components for integration. We have provided 3 packages with similar components with the hope of supporting all 6.x, 7.x and 8.x versions. **No support for 5.x is provided.**

<a name="component-overview"></a>
### Component Overview
In the following discussion of components, only the Tomcat 8.x components are mentioned. Tomcat 8.0.x components are housed inside
`org.jasig.cas.client.tomcat.v8` while Tomcat 8.5.x components are inside `org.jasig.cas.client.tomcat.v85`. You should be able to use
the same exact configuration between the two modules provided package names are adjusted for each release. 

The Tomcat 7.0.x and 6.0.x components have exactly the same name, but **are in the tomcat.v7 and tomcat.v6 packages**, e.g. 
`org.jasig.cas.client.tomcat.v7.Cas20CasAuthenticator` or `org.jasig.cas.client.tomcat.v6.Cas20CasAuthenticator`.

<a name="authenticators"></a>
#### Authenticators
Authenticators are responsible for performing CAS authentication using a particular protocol. All protocols supported by the Jasig Java CAS client are supported: CAS 1.0, CAS 2.0, and SAML 1.1. The following components provide protocol-specific support:

```
org.jasig.cas.client.tomcat.v8.Cas10CasAuthenticator
org.jasig.cas.client.tomcat.v8.Cas20CasAuthenticator
org.jasig.cas.client.tomcat.v8.Cas20ProxyCasAuthenticator
org.jasig.cas.client.tomcat.v8.Saml11Authenticator
```

<a name="realms"></a>
#### Realms
In terms of CAS configuration, Tomcat realms serve as containers for users and role definitions. The roles defined in a Tomcat realm may be referenced in the web.xml servlet descriptor to define authorization constraints on Web applications hosted by the container. Two sources of user/role data are supported:

```
org.jasig.cas.client.tomcat.v8.PropertiesCasRealm
org.jasig.cas.client.tomcat.v8.AssertionCasRealm
```

`PropertiesCasRealm` uses a Java properties file as a source of static user/role information. This component is conceptually similar to the `MemoryRealm` component that ships with Tomcat and defines user/role data via XML configuration. The PropertiesCasRealm is different in that it explicitly lacks support for passwords, which have no use with CAS.

`AssertionCasRealm` is designed to be used in conjunction with the SAML 1.1. protocol to take advantage of CAS attribute release to provide for dynamic user/role data driven by the CAS server. With this component the deployer may define a role attribute, e.g. memberOf, which could be backed by LDAP group membership information. In that case the user would be added to all roles defined in the SAML attribute assertion for values of the the `memberOf` attribute.

<a name="valves"></a>
#### Valves
A number of Tomcat valves are provided to handle functionality outside Realms and Authenticators.

##### Logout Valves
Logout valves provide a way of destroying the CAS authentication state bound to the container for a particular user/session; the destruction of authenticated state is synonymous with logout for the container and its hosted applications. (Note this does not destroy the CAS SSO session.) The implementations provide various strategies to map a URI onto the state-destroying logout function.

```
org.jasig.cas.client.tomcat.v8.StaticUriLogoutValve
org.jasig.cas.client.tomcat.v8.RegexUriLogoutValve
```

##### SingleSignOutValve
The `org.jasig.cas.client.tomcat.v8.SingleSignOutValve` allows the container to participate in CAS single sign-out. In particular this valve handles the SAML LogoutRequest message sent from the CAS server that is delivered when the CAS SSO session ends.

##### ProxyCallbackValve
The `org.jasig.cas.client.tomcat.v8.ProxyCallbackValve` provides a handler for watching request URIs for requests that contain a proxy callback request in support of the CAS 2.0 protocol proxy feature.

<a name="container-setup"></a>
### Container Setup
The version-specific CAS libraries must be placed on the container classpath, `$CATALINA_HOME/lib`.

<a name="context-configuration"></a>
### Context Configuration
The Realm, Authenticator, and Valve components are wired together inside a Tomcat Context configuration element. The location and scope of the Context determines the scope of the applied configuration. To apply a CAS configuration to every Web application hosted in the container, configure the default Context at `$CATALINA_HOME/conf/context.xml`. Note that individual Web applications/servlets can override the default context; see the Context Container reference for more information. 

Alternatively, CAS configuration can be applied to individual Web applications through a Context configuration element located in a `$CONTEXT_NAME.xml` file placed in `$CATALINA_HOME/conf/$ENGINE/$HOST`, where `$ENGINE` is typically Catalina and `$HOST` is `localhost`, `$CATALINA_HOME/conf/Catalina/localhost`. For example, to configure the Tomcat manager servlet, a `manager.xml` file contains Context configuration elements.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context privileged="true">
  <!--
    The following configuration uses the CAS 2.0 protocol and a static
    properties file to define allowed users to the Tomcat manager application.
    The content of manager-users.properties contains entries like the following:
 
      admin=manager-gui,manager-script,manager-jmx,manager-status
      operator=manager-status
      deployer=manager-script
 
    Where admin, operator, and deployer are valid logins for the CAS server.
    The path to the properties file is relative to $CATALINA_HOME.
 
    This example also configures the container for CAS single sign-out.
  -->
  <Realm
    className="org.jasig.cas.client.tomcat.v8.PropertiesCasRealm"
    propertiesFilePath="conf/manager-user-roles.properties"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.Cas20CasAuthenticator"
    encoding="UTF-8"
    casServerLoginUrl="https://server.example.com/cas/login"
    casServerUrlPrefix="https://server.example.com/cas/"
    serverName="client.example.com"
    />
 
  <!-- Single sign-out support -->
  <Valve
    className="org.jasig.cas.client.tomcat.v8.SingleSignOutValve"
    artifactParameterName="SAMLart"
    />
 
  <!--
    Uncomment one of these valves to provide a logout URI for the
    manager servlet.
  -->
  <!--
  <Valve
    className="org.jasig.cas.client.tomcat.v8.RegexUriLogoutValve"
    logoutUriRegex="/manager/logout.*"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.StaticUriLogoutValve"
    logoutUri="/manager/logout.html"
    />
  -->
</Context>
```

The following example shows how to configure a Context for dynamic role data provided by the CAS attribute release feature.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context privileged="true">
  <!--
    The following configuration uses the SAML 1.1 protocol and role data
    provided by the assertion to enable dynamic server-driven role data.
    The attribute used for role data is "memberOf".
  -->
  <Realm
    className="org.jasig.cas.client.tomcat.v8.AssertionCasRealm"
    roleAttributeName="memberOf"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.Saml11Authenticator"
    encoding="UTF-8"
    casServerLoginUrl="https://server.example.com/cas/login"
    casServerUrlPrefix="https://server.example.com/cas/"
    serverName="client.example.com"
    />
 
  <!-- Single sign-out support -->
  <Valve
    className="org.jasig.cas.client.tomcat.v8.SingleSignOutValve"
    artifactParameterName="SAMLart"
    />
</Context>
```

<a name="jetty-integration"></a>
## Jetty Integration
Since version 3.4.2, the Java CAS Client supports Jetty container integration via the following module:

```xml
<dependency>
    <groupId>org.jasig.cas.client</groupId>
    <artifactId>cas-client-integration-jetty</artifactId>
    <version>${cas-client.version}</version>
</dependency>
```

Both programmatic (embedded) and context configuration are supported.

### Jetty Embedded Configuration
```
# CAS configuration parameters
String hostName = "app.example.com";
String casServerBaseUrl = "cas.example.com/cas";
String casRoleAttribute = "memberOf";
boolean casRenew = false;
int casTolerance = 5000;

# Jetty wiring
WebAppContext context = new WebAppContext("/path/to/context", "contextPath");
context.setTempDirectory("/tmp/jetty/work"));
context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
SessionCookieConfig config = context.getSessionHandler().getSessionManager().getSessionCookieConfig();
config.setHttpOnly(true);
config.setSecure(true);
Saml11TicketValidator validator = new Saml11TicketValidator(casServerBaseUrl);
validator.setRenew(casRenew);
validator.setTolerance(casTolerance);
CasAuthenticator authenticator = new CasAuthenticator();
authenticator.setRoleAttribute(casRoleAttribute);
authenticator.setServerNames(hostName);
authenticator.setTicketValidator(validator);
context.getSecurityHandler().setAuthenticator(authenticator);
```

### Jetty Context Configuration
```xml
<?xml version="1.0"  encoding="ISO-8859-1"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">

<Configure class="org.eclipse.jetty.webapp.WebAppContext">
    <Set name="contextPath">/</Set>
    <Set name="war"><SystemProperty name="jetty.base"/>/webapps/yourapp</Set>
    <Get name="securityHandler">
        <Set name="authenticator">
            <New class="org.jasig.cas.client.jetty.CasAuthenticator">
                <Set name="serverNames">app.example.com</Set>
                <Set name="ticketValidator">
                    <New class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator">
                        <Arg>https://cas.example.com/cas</Arg>
                        <!--<Set name="renew">true</Set>-->
                    </New>
                </Set>
            </New>
        </Set>
    </Get>
</Configure>
```

<a name="atlassian-integration"></a>
## Atlassian Integration
The clien includes Atlassian Confluence and JIRA support. Support is enabled by a custom CAS authenticator that extends the default authenticators.

<a name="configuration"></a>
### Configuration

<a name="jira_home-location"></a>
#### $JIRA_HOME Location
 
- WAR/EAR Installation: <extracted archive directory>/webapp
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z/webapp`

- Standalone: <extracted archive directory>/atlassian-jira
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z-standalone/atlassian-jira`

<a name="confluence_install-description"></a>
#### $CONFLUENCE_INSTALL Description

- <extracted archive directory>/confluence
`/opt/atlassian/confluence/confluence-x.y.z/confluence`

<a name="changes-to-webxml"></a>
#### Changes to web.xml
Add the CAS filters to the end of the filter list. See `web.xml` configuration of the client.


<a name="modify-the-seraph-configxml"></a>
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

<a name="cas-authenticator"></a>
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

<a name="confluence-cas-logout"></a>
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

<a name="copy-jars"></a>
#### Copy Jars
Copy cas-client-core-x.y.x.jar and cas-client-integration-atlassian-x.y.x.jar to `$JIRA_HOME/WEB-INF/lib`

<a name="spring-security-integration"></a>
## Spring Security Integration
This configuration tested against the sample application that is included with Spring Security. As of this writing, replacing the `applicationContext-security.xml` in the sample application with the one below would enable this alternative configuration. We can not guarantee this version will work without modification in future versions of Spring Security.

<a name="changes-to-webxml-1"></a>
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
   <init-param>
      <param-name>casServerUrlPrefix</param-name>
      <param-value>https://cas.example.com/cas</param-value>
   </init-param>
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

<a name="changes-to-applicationcontext-securityxml"></a>
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

1. You should replace the `userService` with something that checks your user storage.
2. Replace the `serverName` and `casServerLoginUrl` with your values (or better yet, externalize them).
3. Replace the URLs with the URL configuration for your application.
