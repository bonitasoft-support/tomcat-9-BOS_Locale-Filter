# tomcat-9-BOS_Locale-Filter
A filter to force a value of BOS_Locale
# Build
```
mvn clean package
```
# Lib installation
```
cp target/BonitaLocaleFilter-1.0-SNAPSHOT.jar path/to/BonitaSubscription/server/lib/bonita
```
# Configuration
Edit the path/to/BonitaSubscription/server/webapps/bonita/WEB-INF/web.xml
- Insert before the first `<filter>` xml eleemnt this block of lines
```
    <filter>
      <filter-name>BonitaLocaleFilter</filter-name>
      <filter-class>org.not.supported.bonitasoft.BonitaLocaleFilter</filter-class>
      <init-param>
        <param-name>language</param-name>
        <param-value>es</param-value>
      </init-param>
      <!--
      <init-param>
        <param-name>dryRun</param-name>
        <param-value>true</param-value>
      </init-param>
      <init-param>
        <param-name>bonitaLocaleCookieName</param-name>
        <param-value>BOS_Locale</param-value>
      </init-param>
      -->
    </filter>
```
- Insert before the first `<filter-mapping>` xml eleemnt this block of lines
```
    <filter-mapping>
      <filter-name>BonitaLocaleFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>
```
Do the necessary changes in the path/to/BonitaSubscription/server/webapps/bonita.war
