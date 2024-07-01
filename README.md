# Java Apereo CAS Client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas.client/cas-client-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereocas.client/cas-client)

<a name="intro"></a>
## Intro
This is the official home of the Java Apereo CAS client. The client consists of a collection of Servlet filters that are suitable for most Java-based web applications. It also serves as an API platform to interact with the CAS server programmatically to make authentication requests, validate tickets and consume principal attributes.

All client artifacts are published to Maven central. Depending on functionality, applications will need include one or more of the listed dependencies in their configuration.

<a name="build"></a>
## Build

```bash
git clone git@github.com:apereo/java-cas-client.git
cd java-cas-client
mvn clean package
```


<a name="components"></a>
## Components

- Core functionality, which includes CAS authentication/validation filters.

```xml
<dependency>
    <groupId>org.apereo.cas.client</groupId>
    <artifactId>cas-client-core</artifactId>
    <version>${java.cas.client.version}</version>
</dependency>
```

- Support for SAML functionality is provided by this dependency:

```xml
<dependency>
   <groupId>org.apereo.cas.client</groupId>
   <artifactId>cas-client-support-saml</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Ehcache is provided by this dependency:

```xml
<dependency>
   <groupId>org.apereo.cas.client</groupId>
   <artifactId>cas-client-support-distributed-ehcache</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Distributed proxy ticket caching with Memcached is provided by this dependency:

```xml
<dependency>
   <groupId>org.apereo.cas.client</groupId>
   <artifactId>cas-client-support-distributed-memcached</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Spring Boot AutoConfiguration is provided by this dependency:

```xml
<dependency>
   <groupId>org.apereo.cas.client</groupId>
   <artifactId>cas-client-support-springboot</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

<a name="configuration"></a>
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

<a name="orgapereocasclientauthenticationauthenticationfilter"></a>
#### org.apereo.cas.client.authentication.AuthenticationFilter
The `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.apereo.cas.client.authentication.AuthenticationFilter</filter-class>
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

| Property                              | Description                                                                                                                                                                                                              | Required                                 |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| `casServerUrlPrefix`                  | The start of the CAS server URL, i.e. `https://localhost:8443/cas`                                                                                                                                                       | Yes (unless `casServerLoginUrl` is set)  |
| `casServerLoginUrl`                   | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login`. This overrides `casServerUrlPrefix`, if set.                                                                                  | Yes (unless `casServerUrlPrefix` is set) |
| `serverName`                          | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes                                      |
| `service`                             | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html`                                                                                                                           | No                                       |
| `renew`                               | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting.                         | No                                       |
| `gateway `                            | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all)                                                                                             | No                                       |
| `artifactParameterName `              | specifies the name of the request parameter on where to find the artifact (i.e. `ticket`).                                                                                                                               | No                                       |
| `serviceParameterName `               | specifies the name of the request parameter on where to find the service (i.e. `service`)                                                                                                                                | No                                       |
| `encodeServiceUrl `                   | Whether the client should auto encode the service url. Defaults to `true`                                                                                                                                                | No                                       |
| `ignorePattern`                       | Defines the url pattern to ignore, when intercepting authentication requests.                                                                                                                                            | No                                       |
| `ignoreUrlPatternType`                | Defines the type of the pattern specified. Defaults to `REGEX`. Other types are `CONTAINS`, `EXACT`, `FULL_REGEX`. Can also accept a fully-qualified class name that implements `UrlPatternMatcherStrategy`.             | No                                       |
| `gatewayStorageClass`                 | The storage class used to record gateway requests                                                                                                                                                                        | No                                       |
| `authenticationRedirectStrategyClass` | The class name of the component to decide how to handle authn redirects to CAS                                                                                                                                           | No                                       |
| `method`                              | The method used by the CAS server to send the user back to the application. Defaults to `null`                                                                                                                           | No                                       |

##### Ignore Patterns

The following types are supported:

| Type         | Description                                                                                                                                                                                                                                                                                                                                                 |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `REGEX`      | Matches the URL the `ignorePattern` using `Matcher#find()`. It matches the next occurrence within the substring that matches the regex.                                                                                                                                                                                                                     |
| `CONTAINS`   | Uses the `String#contains()` operation to determine if the url contains the specified pattern. Behavior is case-sensitive.                                                                                                                                                                                                                                  |
| `EXACT`      | Uses the `String#equals()` operation to determine if the url exactly equals the specified pattern. Behavior is case-sensitive.                                                                                                                                                                                                                              |
| `FULL_REGEX` | Matches the URL the `ignorePattern` using `Matcher#matches()`. It matches the expression against the entire string as it implicitly add a `^` at the start and `$` at the end of the pattern, so it will not match substring or part of the string. `^` and `$` are meta characters that represents start of the string and end of the string respectively. |

<a name="orgapereocasclientauthenticationsaml11authenticationfilter"></a>
#### org.apereo.cas.client.authentication.Saml11AuthenticationFilter
The SAML 1.1 `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.apereo.cas.client.authentication.Saml11AuthenticationFilter</filter-class>
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

| Property                 | Description                                                                                                                                                                                                              | Required                                 |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| `casServerUrlPrefix`     | The start of the CAS server URL, i.e. `https://localhost:8443/cas`                                                                                                                                                       | Yes (unless `casServerLoginUrl` is set)  |
| `casServerLoginUrl`      | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login`. This overrides `casServerUrlPrefix`, if set.                                                                                  | Yes (unless `casServerUrlPrefix` is set) |
| `serverName`             | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes                                      |
| `service`                | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html`                                                                                                                           | No                                       |
| `renew`                  | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting.                         | No                                       |
| `gateway `               | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all)                                                                                             | No                                       |
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `SAMLart`).                                                                                                                              | No                                       |
| `serviceParameterName `  | specifies the name of the request parameter on where to find the service (i.e. `TARGET`)                                                                                                                                 | No                                       |
| `encodeServiceUrl `      | Whether the client should auto encode the service url. Defaults to `true`                                                                                                                                                | No                                       |
| `method`                 | The method used by the CAS server to send the user back to the application. Defaults to `null`                                                                                                                           | No                                       |

<a name="rgapereocasclientvalidationcas10ticketvalidationfilter"></a>
#### org.apereo.cas.client.validation.Cas10TicketValidationFilter
Validates tickets using the CAS 1.0 Protocol.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.apereo.cas.client.validation.Cas10TicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://somewhere.cas.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.the-client.com</param-value>
  </init-param>    
</filter>
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property                        | Description                                                                                                                                                                                                                                                                                                             | Required |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `casServerUrlPrefix `           | The start of the CAS server URL, i.e. `https://localhost:8443/cas`                                                                                                                                                                                                                                                      | Yes      |
| `serverName`                    | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port).                                                                                              | Yes      |
| `renew`                         | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting.                                                                                                                        | No       |
| `redirectAfterValidation `      | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`.                                                                                                                                                                                               | No       |
| `useSession `                   | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`.                                                                                                                                                                              | No       |
| `exceptionOnValidationFailure ` | Whether to throw an exception or not on ticket validation failure. Defaults to `true`.                                                                                                                                                                                                                                  | No       |
| `sslConfigFile`                 | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.      |
| `encoding`                      | Specifies the encoding charset the client should use                                                                                                                                                                                                                                                                    | No       |
| `hostnameVerifier`              | Hostname verifier class name, used when making back-channel calls                                                                                                                                                                                                                                                       | No       |

<a name="orgapereocasclientvalidationsaml11ticketvalidationfilter"></a>
#### org.apereo.cas.client.validation.Saml11TicketValidationFilter
Validates tickets using the SAML 1.1 protocol.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.apereo.cas.client.validation.Saml11TicketValidationFilter</filter-class>
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

| Property                        | Description                                                                                                                                                                                                                                                                                                             | Required |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `casServerUrlPrefix `           | The start of the CAS server URL, i.e. `https://localhost:8443/cas`                                                                                                                                                                                                                                                      | Yes      |
| `serverName`                    | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port).                                                                                              | Yes      |
| `renew`                         | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting.                                                                                                                        | No       |
| `redirectAfterValidation `      | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`.                                                                                                                                                                                               | No       |
| `useSession `                   | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`.                                                                                                                                                                              | No       |
| `exceptionOnValidationFailure ` | whether to throw an exception or not on ticket validation failure. Defaults to `true`                                                                                                                                                                                                                                   | No       |
| `tolerance `                    | The tolerance for drifting clocks when validating SAML tickets. Note that 10 seconds should be more than enough for most environments that have NTP time synchronization. Defaults to `1000 msec`                                                                                                                       | No       |
| `sslConfigFile`                 | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.      |
| `encoding`                      | Specifies the encoding charset the client should use                                                                                                                                                                                                                                                                    | No       |
| `hostnameVerifier`              | Hostname verifier class name, used when making back-channel calls                                                                                                                                                                                                                                                       | No       |

<a name="orgapereocasclientvalidationcas20proxyreceivingticketvalidationfilter"></a>
#### org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 2.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, a `Cas20ProxyTicketValidator` will be constructed. Otherwise a general `Cas20ServiceTicketValidator` will be constructed that does not accept proxy tickets. 

**Note**: If you are using proxy validation, you should place the `filter-mapping` of the validation filter before the authentication filter.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter</filter-class>
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

| Property                           | Description                                                                                                                                                                                                                                                                                                             | Required |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `casServerUrlPrefix `              | The start of the CAS server URL, i.e. `https://localhost:8443/cas`                                                                                                                                                                                                                                                      | Yes      |
| `serverName`                       | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port).                                                                                              | Yes      |
| `renew`                            | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting.                                                                                                                        | No       |
| `redirectAfterValidation `         | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`.                                                                                                                                                                                               | No       |
| `useSession `                      | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`.                                                                                                                                                                              | No       |
| `exceptionOnValidationFailure `    | whether to throw an exception or not on ticket validation failure. Defaults to `true`                                                                                                                                                                                                                                   | No       |
| `proxyReceptorUrl `                | The URL to watch for `PGTIOU/PGT` responses from the CAS server. Should be defined from the root of the context. For example, if your application is deployed in `/cas-client-app` and you want the proxy receptor URL to be `/cas-client-app/my/receptor` you need to configure proxyReceptorUrl to be `/my/receptor`. | No       |
| `acceptAnyProxy `                  | Specifies whether any proxy is OK. Defaults to `false`.                                                                                                                                                                                                                                                                 | No       |
| `allowedProxyChains `              | Specifies the proxy chain. Each acceptable proxy chain should include a space-separated list of URLs (for exact match) or regular expressions of URLs (starting by the `^` character). Each acceptable proxy chain should appear on its own line.                                                                       | No       |
| `proxyCallbackUrl`                 | The callback URL to provide the CAS server to accept Proxy Granting Tickets.                                                                                                                                                                                                                                            | No       |
| `proxyGrantingTicketStorageClass ` | Specify an implementation of the ProxyGrantingTicketStorage class that has a no-arg constructor.                                                                                                                                                                                                                        | No       |
| `sslConfigFile`                    | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.      |
| `encoding`                         | Specifies the encoding charset the client should use                                                                                                                                                                                                                                                                    | No       |
| `secretKey`                        | The secret key used by the `proxyGrantingTicketStorageClass` if it supports encryption.                                                                                                                                                                                                                                 | No       |
| `cipherAlgorithm`                  | The algorithm used by the `proxyGrantingTicketStorageClass` if it supports encryption. Defaults to `DESede`                                                                                                                                                                                                             | No       |
| `millisBetweenCleanUps`            | Startup delay for the cleanup task to remove expired tickets from the storage. Defaults to `60000 msec`                                                                                                                                                                                                                 | No       |
| `ticketValidatorClass`             | Ticket validator class to use/create                                                                                                                                                                                                                                                                                    | No       |
| `hostnameVerifier`                 | Hostname verifier class name, used when making back-channel calls                                                                                                                                                                                                                                                       | No       |
| `privateKeyPath`                   | The path to a private key to decrypt PGTs directly sent encrypted as an attribute                                                                                                                                                                                                                                       | No       |
| `privateKeyAlgorithm`              | The algorithm of the private key. Defaults to `RSA`                                                                                                                                                                                                                                                                     | No       |

#### org.apereo.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 3.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, 
a `Cas30ProxyTicketValidator` will be constructed. Otherwise a general `Cas30ServiceTicketValidator` will be constructed that does not 
accept proxy tickets. Supports all configurations that are available for `Cas20ProxyReceivingTicketValidationFilter`.

#### org.apereo.cas.client.validation.Cas30JsonProxyReceivingTicketValidationFilter
Identical to `Cas30ProxyReceivingTicketValidationFilter`, yet the filter is able to accept validation responses from CAS
that are formatted as JSON per guidelines laid out by the CAS protocol. 
See the [protocol documentation](https://apereo.github.io/cas/5.1.x/protocol/CAS-Protocol-Specification.html)
for more info.

<a name="orgapereocasclientvalidationcasjwtticketvalidationfilter"></a>
#### org.apereo.cas.client.validation.CasJWTTicketValidationFilter
Validates service tickets that issued by the CAS server as JWTs.
  
Supported JWTs are:

- The JWT must be signed and encrypted, in that order, or...
- The JWT must be encrypted and signed, in that order, or...
- The JWT must be encrypted.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.apereo.cas.client.validation.CasJWTTicketValidationFilter</filter-class>
  <init-param>
    <param-name>signingKey</param-name>
    <param-value>...</param-value>
  </init-param>
  <init-param>
    <param-name>encryptionKey</param-name>
    <param-value>...</param-value>
  </init-param>
</filter>
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property                  | Description                                                                              | Required |
|---------------------------|------------------------------------------------------------------------------------------|----------|
| `signingKey `             | The signing key. Only `AES` secret keys are supported.                                   | Yes      |
| `encryptionKey `          | The encryption key. Only `AES` secret keys are supported.                                | Yes      |
| `expectedIssuer `         | `iss` claim value that is required to match what is in the JWT.                          | Yes      |
| `expectedAudience `       | `aud` claim value that is required to match what is in the JWT.                          | Yes      |
| `encryptionKeyAlgorithm ` | Default is `AES`.                                                                        | No       |
| `encryptionKeyAlgorithm ` | Default is `AES`.                                                                        | No       |
| `requiredClaims `         | Default is `sub,aud,iat,jti,exp,iss`.                                                    | No       |
| `base64EncryptionKey `    | If encryption key should be base64-decoded first. Default is `true`.                     | No       |
| `base64SigningKey `       | If encryption key should be base64-decoded first. Default is `false`.                    | No       |
| `maxClockSkew `           | Maximum acceptable clock skew when validating expiration dates. Default is `60` seconds. | No       |

##### Proxy Authentication vs. Distributed Caching
The client has support for clustering and distributing the TGT state among application nodes that are behind a load balancer. In order to do so, 
the parameter needs to be defined as such for the filter.

###### Ehcache

Configure the client:

```xml
<init-param>
  <param-name>proxyGrantingTicketStorageClass</param-name>
  <param-value>org.apereo.cas.client.EhcacheBackedProxyGrantingTicketStorageImpl</param-value>
</init-param>
```
The setting provides an implementation for proxy storage using EhCache to take advantage of its replication features so that the PGT is successfully replicated and shared among nodes, regardless which node is selected as the result of the load balancer rerouting. 

Configuration of this parameter is not enough. The EhCache configuration needs to enable the replication mechanism through once of its suggested ways. A sample of that configuration based on RMI replication can be found here. Please note that while the sample is done for a distributed ticket registry implementation, the basic idea and configuration should easily be transferable. 

When loading from the `web.xml`, the Apereo CAS Client relies on a series of default values, one of which being that the cache must be configured in the default location (i.e. `classpath:ehcache.xml`). 

```xml
<cacheManagerPeerProviderFactory class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
   properties="peerDiscovery=automatic,
   multicastGroupAddress=230.0.0.1, multicastGroupPort=4446"/>
 
<cacheManagerPeerListenerFactory class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory"/>
 
<cache
   name="org.apereo.cas.client.EhcacheBackedProxyGrantingTicketStorageImpl.cache"
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
  <param-value>org.apereo.cas.client.proxy.MemcachedBackedProxyGrantingTicketStorageImpl</param-value>
</init-param>
```

When loading from the `web.xml`, the Client relies on a series of default values, one of which being that the list of memcached servers must be defined in `/cas/casclient_memcached_hosts.txt` on the classpath. The file is a simple list of `<hostname>:<ports>` on separate lines. **BE SURE NOT TO HAVE EXTRA LINE BREAKS**.

<a name="orgapereocasclientutilhttpservletrequestwrapperfilter"></a>
#### org.apereo.cas.client.HttpServletRequestWrapperFilter

Wraps an `HttpServletRequest` so that the `getRemoteUser` and `getPrincipal` return the CAS related entries.

```xml
<filter>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <filter-class>org.apereo.cas.client.HttpServletRequestWrapperFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property        | Description                                                   | Required |
|-----------------|---------------------------------------------------------------|----------|
| `roleAttribute` | Used to determine the principal role.                         | No       |
| `ignoreCase`    | Whether role checking should ignore case. Defaults to `false` | No       |

<a name="orgapereocasclientutilassertionthreadlocalfilter"></a>

#### org.apereo.cas.client.AssertionThreadLocalFilter
Places the `Assertion` in a `ThreadLocal` for portions of the application that need access to it. This is useful when the Web application that this filter "fronts" needs to get the Principal name, but it has no access to the `HttpServletRequest`, hence making `getRemoteUser()` call impossible.

```xml
<filter>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <filter-class>org.apereo.cas.client.AssertionThreadLocalFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

<a name="orgapereocasclientutilerrorredirectfilter"></a>

#### org.apereo.cas.client.ErrorRedirectFilter
Filters that redirects to the supplied url based on an exception.  Exceptions and the urls are configured via init filter name/param values.

| Property                   | Description                                                       | Required |
|----------------------------|-------------------------------------------------------------------|----------|
| `defaultErrorRedirectPage` | Default url to redirect to, in case no error matches are found.   | Yes      |
| `java.lang.Exception`      | Fully qualified exception name. Its value must be redirection url | No       |

```xml
<filter>
  <filter-name>CAS Error Redirect Filter</filter-name>
  <filter-class>org.apereo.cas.client.ErrorRedirectFilter</filter-class>
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
    class="org.apereo.cas.client.authentication.AuthenticationFilter"
    p:casServerLoginUrl="https://localhost:8443/cas/login"
    p:renew="false"
    p:gateway="false"
    p:service="https://my.local.service.com/cas-client" />
```

##### Cas10TicketValidationFilter
```xml
<bean
    name="ticketValidationFilter"
    class="org.apereo.cas.client.validation.Cas10TicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.apereo.cas.client.validation.Cas10TicketValidator">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

##### Saml11TicketValidationFilter
```xml
<bean
    name="ticketValidationFilter"
    class="org.apereo.cas.client.validation.Saml11TicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.apereo.cas.client.validation.Saml11TicketValidator">
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
    class="org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client">
    <property name="ticketValidator">
        <bean class="org.apereo.cas.client.validation.Cas20ServiceTicketValidator">
            <constructor-arg index="0" value="https://localhost:8443/cas" />
        </bean>
    </property>
</bean>
```

Configuration to accept a Proxy Granting Ticket:
```xml
<bean
    name="ticketValidationFilter"
    class="org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean
            class="org.apereo.cas.client.validation.Cas20ServiceTicketValidator"
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
    class="org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean class="org.apereo.cas.client.validation.Cas20ProxyTicketValidator"
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
    class="org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"
    p:service="https://my.local.service.com/cas-client"
    p:proxyReceptorUrl="/proxy/receptor">
    <property name="ticketValidator">
        <bean class="org.apereo.cas.client.validation.Cas20ProxyTicketValidator"
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

<a name="springboot-autoconfiguration"></a>
## Spring Boot AutoConfiguration

### Usage 

* Define a dependency:

> Maven:

```xml
<dependency>
   <groupId>org.apereo.cas.client</groupId>
   <artifactId>cas-client-support-springboot</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

> Gradle:

```groovy
dependencies {
    ...
    implementation 'org.apereo.cas.client:cas-client-support-springboot:${java.cas.client.version}'
    ...
}
```

* Add the following required properties in Spring Boot's `application.properties` or `application.yml`:

```properties
cas.server-url-prefix=https://cashost.com/cas
cas.server-login-url=https://cashost.com/cas/login
cas.client-host-url=https://casclient.com
```

* Annotate Spring Boot application (or any @Configuration class) with `@EnableCasClient` annotation

```java
@SpringBootApplication
@Controller
@EnableCasClient
public class MyApplication { .. }
```

> For CAS3 protocol (authentication and validation filters) - which is default if nothing is specified

```properties
cas.validation-type=CAS3
```

> For CAS2 protocol (authentication and validation filters)

```properties
cas.validation-type=CAS
```

> For SAML protocol (authentication and validation filters)

```properties
cas.validation-type=SAML
```

### Available optional properties

* `cas.single-logout.enabled`
* `cas.authentication-url-patterns`
* `cas.validation-url-patterns`
* `cas.request-wrapper-url-patterns`
* `cas.assertion-thread-local-url-patterns`
* `cas.gateway`
* `cas.use-session`
* `cas.attribute-authorities`
* `cas.redirect-after-validation`
* `cas.allowed-proxy-chains`
* `cas.proxy-callback-url`
* `cas.proxy-receptor-url`
* `cas.accept-any-proxy`
* `server.context-parameters.renew`

### Spring Security Integration

An application that is handling security concerns via Spring Security can take advantage
of this module to automatically populate the Spring Security authentication context
with roles and authorities that are fetched as attributes from the CAS assertion. 

To do so, the attributes names (i.e. `membership`) from the CAS assertion that should be translated to Spring Security 
authorities must be specified in the configuration:

```properties
cas.attribute-authorities=membership
```

The application may then enforce role-based security via:

```java         
@SpringBootApplication
@EnableCasClient
public class MyConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/protected-endpoint").hasAuthority("ADMIN")
            .anyRequest().authenticated();
    }
}
```

The translation between CAS attributes and Spring Security authorities and/or roles can be customized using 
the following bean definition:

```java
@Bean
public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> springSecurityCasUserDetailsService() {
    return null;
}
```    

### Advanced configuration

This module does not expose ALL the CAS client configuration options via standard Spring property sources, but only most commonly used ones.
If there is a need however, to set any number of not exposed, 'exotic' properties, you can implement the `CasClientConfigurer`
class in your `@EnableCasClient` annotated class and override appropriate configuration method(s) for CAS client filter(s) in question.
For example:

```java
@SpringBootApplication
@EnableCasClient
class CasProtectedApplication implements CasClientConfigurer {    
    @Override
    void configureValidationFilter(FilterRegistrationBean validationFilter) {           
        validationFilter.getInitParameters().put("millisBetweenCleanUps", "120000");
    }        
    @Override
    void configureAuthenticationFilter(FilterRegistrationBean authenticationFilter) {
        authenticationFilter.getInitParameters().put("artifactParameterName", "casTicket");
        authenticationFilter.getInitParameters().put("serviceParameterName", "targetService");
    }                                
}
```

<a name="configuring-single-sign-out"></a>
### Configuring Single Sign Out
The Single Sign Out support in CAS consists of configuring one `SingleSignOutFilter` and one `ContextListener`. Please note that if you have configured the CAS Client for Java as Web filters, this filter must come before the other filters as described.

The `SingleSignOutFilter` can affect character encoding. This becomes most obvious when used in conjunction with applications such as Atlassian Confluence. It's recommended you explicitly configure either the [VT Character Encoding Filter](http://code.google.com/p/vt-middleware/wiki/vtservletfilters#CharacterEncodingFilter) or the [Spring Character Encoding Filter](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/filter/CharacterEncodingFilter.html) with explicit encodings.

#### Configuration

| Property                    | Description                                                                                                                                                                                                                                                                 | Required |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `artifactParameterName`     | The ticket artifact parameter name. Defaults to `ticket`                                                                                                                                                                                                                    | No       |
| `logoutParameterName`       | Defaults to `logoutRequest`                                                                                                                                                                                                                                                 | No       |
| `relayStateParameterName`   | Defaults to `RelayState`                                                                                                                                                                                                                                                    | No       |
| `eagerlyCreateSessions`     | Defaults to `true`                                                                                                                                                                                                                                                          | No       |
| `artifactParameterOverPost` | Defaults to  `false`                                                                                                                                                                                                                                                        | No       |
| `logoutCallbackPath`        | The path which is expected to receive logout callback requests from the CAS server. This is necessary if your app needs access to the raw input stream when handling form posts. If not configured, the default behavior will check every form post for a logout parameter. | No       |

<a name="cas-protocol"></a>
#### CAS Protocol

```xml
<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.apereo.cas.client.session.SingleSignOutFilter</filter-class>
</filter>
...
<filter-mapping>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
...
<listener>
    <listener-class>org.apereo.cas.client.session.SingleSignOutHttpSessionListener</listener-class>
</listener>
```

<a name="saml-protocol"></a>
#### SAML Protocol

```xml
<filter>
   <filter-name>CAS Single Sign Out Filter</filter-name>
   <filter-class>org.apereo.cas.client.session.SingleSignOutFilter</filter-class>
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
    <listener-class>org.apereo.cas.client.session.SingleSignOutHttpSessionListener</listener-class>
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
The client supports the Java Authentication and Authorization Service (JAAS) framework, which provides authn facilities to CAS-enabled JEE applications.

A general JAAS authentication module, `CasLoginModule`, is available with the specific purpose of providing authentication and authorization services to CAS-enabled JEE applications. The design of the module is simple: given a service URL and a service ticket in a `NameCallback` and `PasswordCallback`, respectively, the module contacts the CAS server and attempts to validate the ticket. In keeping with CAS integration for Java applications, a JEE container-specific servlet filter is needed to protect JEE Web applications. The JAAS support should be extensible to any JEE container.

<a name="configure-casloginmodule"></a>
### Configure CasLoginModule
It is expected that for JEE applications both authentication and authorization services will be required for CAS integration. The following JAAS module configuration file excerpt demonstrates how to leverage SAML 1.1 attribute release in CAS to provide authorization data in addition to authentication:

```
cas {
  jaas.org.apereo.cas.client.CasLoginModule required
    ticketValidatorClass="org.apereo.cas.client.validation.Saml11TicketValidator"
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


| Property                | Description                                                                                                                                                                                                                           | Required |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `ticketValidatorClass ` | Fully-qualified class name of CAS ticket validator class.                                                                                                                                                                             | Yes      |
| `casServerUrlPrefix`    | URL to root of CAS Web application context.                                                                                                                                                                                           | Yes      |
| `service`               | CAS service parameter that may be overridden by callback handler. **Note**: service must be specified by at least one component such that it is available at service ticket validation time.                                          | No       |
| `defaultRoles`          | Comma-delimited list of static roles applied to all authenticated principals.                                                                                                                                                         | No       |
| `roleAttributeNames`    | Comma-delimited list of attribute names that describe role data delivered to CAS in the service-ticket validation response that should be applied to the current authenticated principal.                                             | No       |
| `principalGroupName`    | The name of a group principal containing the primary principal name of the current JAAS subject. The default value is `CallerPrincipal`.                                                                                              | No       |
| `roleGroupName`         | The name of a group principal containing all role data. The default value is `Roles`.                                                                                                                                                 | No       |
| `cacheAssertions`       | Flag to enable assertion caching. This may be required for JAAS providers that attempt to periodically reauthenticate to renew principal. Since CAS tickets are one-time-use, a cached assertion must be provided on reauthentication.| No       |
| `cacheTimeout`          | Assertion cache timeout in minutes.                                                                                                                                                                                                   | No       |
| `tolerance`             | The tolerance for drifting clocks when validating SAML tickets.                                                                                                                                                                       | No       |

### Programmatic JAAS login using the Servlet 3
A `jaas.org.apereo.cas.client.Servlet3AuthenticationFilter` servlet filter that performs a programmatic JAAS login using the Servlet 3.0 `HttpServletRequest#login()` facility. This component should be compatible with any servlet container that supports the Servlet 3.0/JEE6 specification.
 
The filter executes when it receives a CAS ticket and expects the
`CasLoginModule` JAAS module to perform the CAS ticket validation in order to produce an `AssertionPrincipal` from which the CAS assertion is obtained and inserted into the session to enable SSO.

If a `service` init-param is specified for this filter, it supersedes
the service defined for the `CasLoginModule`.
