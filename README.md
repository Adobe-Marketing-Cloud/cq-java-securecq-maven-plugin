cq-java-securecq-maven-plugin
=======================

This is a simple Maven Plugin integration of the [Cognifide's SecureCQ](https://github.com/Cognifide/SecureCQ), a tool to find the most popular security problems in your CQ instance.

# Plugin quick reference

<table class="bodyTable" border="0">
          <tbody><tr class="a">
            <th>Name</th>
            <th>Type</th>
            <th>Since</th>
            <th>Description</th>
          </tr>
          <tr class="b">
            <td><b>authorUrl</b></td>
            <td><tt>String</tt></td>
            <td><tt>-</tt></td>
            <td>The <tt>author</tt> CQ instance URL.<br><b>Default value is</b>: <tt>http://localhost:4502</tt>.<br><b>User property is</b>: <tt>scq.url.author</tt>.</td>
          </tr>
          <tr class="a">
            <td><b>dispatcherUrl</b></td>
            <td><tt>String</tt></td>
            <td><tt>-</tt></td>
            <td>The <tt>dispatcher</tt> CQ instance URL.<br><b>User property is</b>: <tt>scq.url.dispatcher</tt>.</td>
          </tr>
          <tr class="b">
            <td><b>enabledTests</b></td>
            <td><tt>String[]</tt></td>
            <td><tt>-</tt></td>
            <td>The list of tests have to be performed,
<tt>config-validation</tt>, <tt>default-passwords</tt>,
<tt>dispatcher-access</tt>, <tt>shindig-proxy</tt>,
<tt>etc-tools</tt>, <tt>content-grabbing</tt>,
<tt>feed-selector</tt>, <tt>wcm-debug</tt>,
<tt>webdav</tt>, <tt>webdav</tt>, <tt>geometrixx</tt>
and <tt>redundant-selectors</tt> by default.<br></td>
          </tr>
          <tr class="a">
            <td><b>publishUrl</b></td>
            <td><tt>String</tt></td>
            <td><tt>-</tt></td>
            <td>The <tt>publish</tt> CQ instance URL.<br><b>User property is</b>: <tt>scq.url.publish</tt>.</td>
          </tr>
        </tbody></table>

# Usage

 * Run a CQ instance:
 
    <pre>java -Djava.net.preferIPv4Stack=true -jar cq5-5.6.0.20130129-author.jar</pre>

 * Perform the tests
 
    <pre>mvn com.adobe.granite.maven:securecq-maven-plugin:0.0.1-SNAPSHOT:securecq [-Dscq.url.author=http://${host}:${port} -Dscq.url.publish=http://${host}:${port} -Dscq.url.dispatcher=http://${host}:${port}]</pre>

   It will produce an output like the following one:

<pre>
[INFO] ------------------------------------------------------------------------
[INFO] Building Cognifide's SecureCQ Maven plugin 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- securecq-maven-plugin:0.0.1-SNAPSHOT:securecq (default-cli) @ securecq-maven-plugin ---
[INFO] Performing security check 'config-validation'...
[INFO] 'config-validation' result: OK
[INFO] 'config-validation' passed tests:
[INFO]  - URL [http://localhost:4502] for instance author looks OK
[INFO]  - URL [http://localhost:4502] for instance publish looks OK
[INFO]  - URL [http://localhost:4502] for instance dispatcher looks OK
[INFO] Performing security check 'default-passwords'...
[INFO] 'default-passwords' result: FAIL
[WARNING] 'default-passwords' detected some failures:
[WARNING]  - User admin:admin exists on author
[WARNING]  - User author:author exists on author
[WARNING]  - User jdoe@geometrixx.info:jdoe exists on author
[WARNING]  - User aparker@geometrixx.info:aparker exists on author
[WARNING]  - User admin:admin exists on publish
[WARNING]  - User author:author exists on publish
[WARNING]  - User jdoe@geometrixx.info:jdoe exists on publish
[WARNING]  - User aparker@geometrixx.info:aparker exists on publish
[INFO] 'default-passwords' passed tests:
[INFO]  - User replication-receiver:replication-receiver doesn't exists on author
[INFO]  - User replication-receiver:replication-receiver doesn't exists on publish
[INFO] Performing security check 'dispatcher-access'...
[INFO] 'dispatcher-access' result: OK
[INFO] 'dispatcher-access' passed tests:
[INFO]  - [http://localhost:4502/.json] is restricted
[INFO]  - [http://localhost:4502/.1.json] is restricted
[INFO]  - [http://localhost:4502/.2.json] is restricted
[INFO]  - [http://localhost:4502/apps.json] is restricted
[INFO]  - [http://localhost:4502/bin.1.json] is restricted
[INFO]  - [http://localhost:4502/bin/querybuilder.json] is restricted
[INFO]  - [http://localhost:4502/bin/receive] is restricted
[INFO]  - [http://localhost:4502/bin/workflow] is restricted
[INFO]  - [http://localhost:4502/libs.json] is restricted
[INFO]  - [http://localhost:4502/tmp.json] is restricted
[INFO]  - [http://localhost:4502/var.json] is restricted
[INFO]  - [http://localhost:4502/libs/cq/search/content/querydebug.html] is restricted
[INFO]  - [http://localhost:4502/home/groups/e/everyone.json] is restricted
[INFO] Performing security check 'shindig-proxy'...
[INFO] 'shindig-proxy' result: OK
[INFO] 'shindig-proxy' passed tests:
[INFO]  - [http://localhost:4502/libs/shindig/proxy] is restricted
[INFO] Performing security check 'etc-tools'...
[INFO] 'etc-tools' result: FAIL
[WARNING] 'etc-tools' detected some failures:
[WARNING]  - [http://localhost:4502/crx/de/index.jsp] is not restricted
[INFO] Performing security check 'content-grabbing'...
[INFO] 'content-grabbing' result: FAIL
[WARNING] 'content-grabbing' detected some failures:
[WARNING]  - [http://localhost:4502/.infinity.json] is not restricted
[WARNING]  - [http://localhost:4502/.tidy.json] is not restricted
[WARNING]  - [http://localhost:4502/.sysview.xml] is not restricted
[WARNING]  - [http://localhost:4502/.docview.json] is not restricted
[WARNING]  - [http://localhost:4502/.docview.xml] is not restricted
[WARNING]  - [http://localhost:4502/.2.json] is not restricted
[WARNING]  - [http://localhost:4502/.query.json] is not restricted
[INFO] Performing security check 'feed-selector'...
[INFO] 'feed-selector' result: FAIL
[WARNING] 'feed-selector' detected some failures:
[WARNING]  - [http://localhost:4502/.feed.xml] is not restricted
[WARNING]  - [http://localhost:4502/.feed.html] is not restricted
[INFO] Performing security check 'wcm-debug'...
[INFO] 'wcm-debug' result: OK
[INFO] 'wcm-debug' passed tests:
[INFO]  - WCM debug filter is disabled at [http://localhost:4502/?debug=layout]
[INFO] Performing security check 'webdav'...
[INFO] 'webdav' result: FAIL
[WARNING] 'webdav' detected some failures:
[WARNING]  - WebDAV is enabled at publish
[INFO] Performing security check 'geometrixx'...
[INFO] 'geometrixx' result: OK
[INFO] 'geometrixx' passed tests:
[INFO]  - [http://localhost:4502/content/geometrixx/en.html] is restricted
[INFO] Performing security check 'redundant-selectors'...
[INFO] 'redundant-selectors' result: FAIL
[WARNING] 'redundant-selectors' detected some failures:
[WARNING]  - [http://localhost:4502/.thisIsAdditionalSelector.html] is not restricted
[WARNING]  - [http://localhost:4502/.this.is.additional.selector.html] is not restricted
[WARNING]  - [http://localhost:4502/.html/thisIsAdditionalSuffix] is not restricted
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.653s
[INFO] Finished at: Mon Jun 24 15:47:51 CEST 2013
[INFO] Final Memory: 9M/2031M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal com.adobe.granite.maven:securecq-maven-plugin:0.0.1-SNAPSHOT:securecq (default-cli) on project securecq-maven-plugin: SequreCQ detected secutity vulnerabilities in your instances, see the log for details.
</pre>