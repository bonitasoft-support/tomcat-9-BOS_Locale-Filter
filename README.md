# tomcat-9-BOS_Locale-Filter
A filter to force a value of BOS_Locale cookie. 
# Parameters
The `language` is configurable: default en. 
The `bonitaLocaleCookieName` is configurable: default BOS_Locale. 
When `dryRun` is true, nothing is done. 

> **HINT:** With DryRun set to true, log level set to `FINER` the filter will trace in the log file all the calls
>           to addCookie(...) method with cookie name and value. 
# Download the project
Open a terminal: 
```
git clone git@github.com:bonitasoft-support/tomcat-9-BOS_Locale-Filter.git
```
# Build
```
cd tomcat-9-BOS_Locale-Filter
mvn clean package
```
# Lib installation
```
cp target/BonitaLocaleFilter-1.0-SNAPSHOT.jar path/to/BonitaSubscription/server/lib/bonita
```
# Configuration
Edit the path/to/BonitaSubscription/server/webapps/bonita/WEB-INF/web.xml 
- Insert before the first `<filter>` xml element this block of lines, and adapt the parameters' values as needed
```
    <filter>
      <filter-name>BonitaLocaleFilter</filter-name>
      <filter-class>org.not.supported.bonitasoft.BonitaLocaleFilter</filter-class>
      <!-- default values language en, dryRun false, bonitaLocaleCookieName BOS_Locale
      <init-param>
        <param-name>language</param-name>
        <param-value>en</param-value>
      </init-param>
      <init-param>
        <param-name>dryRun</param-name>
        <param-value>false</param-value>
      </init-param>
      <init-param>
        <param-name>bonitaLocaleCookieName</param-name>
        <param-value>BOS_Locale</param-value>
      </init-param>
      -->
    </filter>
```
- Insert before the first `<filter-mapping>` xml element this block of lines 
```
    <filter-mapping>
      <filter-name>BonitaLocaleFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>
```
Do the necessary changes in the path/to/BonitaSubscription/server/webapps/bonita.war 
# Add logging configuration
Edit the `/opt/BonitaSubscription-2021.2-u0/server/conf/logging.properties` file and add these lines after the line 61: 
```
# BOS_Locale level INFO no messages ; FINE addCookie(BOS_Locale) are logged ; FINER incoming HTTP request info and addCoookie(...) calls are logged
org.not.supported.bonitasoft.BonitaLocaleFilter.level = FINEST
```

> **INFORMATION:** log.debug(...) and log.trace(...) methods are used in the lines of code. 
# Log file abstract
```
2022-12-06 12:02:21.983 +0100 FINER (http-nio-8080-exec-2) org.not.supported.bonitasoft.BonitaLocaleFilter First call addCookie - incoming HTTP request
127.0.0.1 POST http://localhost:8080/bonita/loginservice?redirect=true&redirectUrl=%2Fbonita%2Fapps%2FappDirectoryBonita
Parameters: redirect:[true], redirectUrl:[/bonita/apps/appDirectoryBonita], username:[***], password:[***], _l:[en]
Accept-Language: en-US,en;q=0.7,fr-FR;q=0.3
Cookie: JSESSIONID=934F53D23679ADF391A33EE5806803C8
2022-12-06 12:02:21.983 +0100 FINER (http-nio-8080-exec-2) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie bonita.tenant=1
2022-12-06 12:02:21.989 +0100 FINER (http-nio-8080-exec-2) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=f0281f14-fd39-4691-9939-2f43cb03fca3
2022-12-06 12:02:22.069 +0100 FINER (http-nio-8080-exec-5) org.not.supported.bonitasoft.BonitaLocaleFilter First call addCookie - incoming HTTP request
127.0.0.1 GET http://localhost:8080/bonita/apps/appDirectoryBonita/home/?_l=en
Parameters: _l:[en]
Accept-Language: en-US,en;q=0.7,fr-FR;q=0.3
Cookie: JSESSIONID=61397BE3CC17C2B6D58198EB0E751444; bonita.tenant=1; X-Bonita-API-Token=f0281f14-fd39-4691-9939-2f43cb03fca3
2022-12-06 12:02:22.069 +0100 FINE (http-nio-8080-exec-5) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie BOS_Locale=en ####   DRY RUN  #  NO CHANGE   #### 
2022-12-06 12:02:22.153 +0100 INFO (http-nio-8080-exec-5) org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/bonita] RestletServlet: [Restlet] ServerServlet: component class is null
2022-12-06 12:02:22.281 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter First call addCookie - incoming HTTP request
127.0.0.1 GET http://localhost:8080/bonita/portal/resource/app/appDirectoryBonita/home/API/system/session/unusedId
Accept-Language: en-US,en;q=0.7,fr-FR;q=0.3
Cookie: JSESSIONID=61397BE3CC17C2B6D58198EB0E751444; bonita.tenant=1; X-Bonita-API-Token=f0281f14-fd39-4691-9939-2f43cb03fca3; BOS_Locale=en
2022-12-06 12:02:22.282 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=
2022-12-06 12:02:22.282 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=
2022-12-06 12:02:22.282 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=f0281f14-fd39-4691-9939-2f43cb03fca3
2022-12-06 12:02:22.284 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=
2022-12-06 12:02:22.284 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=
2022-12-06 12:02:22.284 +0100 FINER (http-nio-8080-exec-6) org.not.supported.bonitasoft.BonitaLocaleFilter Handling addCookie X-Bonita-API-Token=f0281f14-fd39-4691-9939-2f43cb03fca3
```
