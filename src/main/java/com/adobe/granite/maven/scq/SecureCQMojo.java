/*
 *    Copyright 2013 Adobe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.adobe.granite.maven.scq;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.xml.sax.SAXException;

import com.cognifide.securecq.AbstractTest;
import com.cognifide.securecq.Configuration;
import com.cognifide.securecq.TestResult;
import com.cognifide.securecq.cli.XmlConfigurationReader;
import com.cognifide.securecq.markers.AuthorTest;
import com.cognifide.securecq.markers.DispatcherTest;
import com.cognifide.securecq.markers.PublishTest;
import com.cognifide.securecq.tests.ConfigValidation;
import com.cognifide.securecq.tests.DefaultPasswordsTest;
import com.cognifide.securecq.tests.ExtensionsTest;
import com.cognifide.securecq.tests.PageContentTest;
import com.cognifide.securecq.tests.WcmDebugTest;
import com.cognifide.securecq.tests.WebDavTest;

/**
 * Performs the SecureCQ test analysis.
 */
@Mojo(
    name = "securecq",
    defaultPhase = LifecyclePhase.TEST,
    threadSafe = true,
    requiresProject = false
)
public final class SecureCQMojo extends AbstractMojo {

    /**
     * The {@code author} CQ instance URL.
     */
    @Parameter(property = "scq.url.author", defaultValue = "http://localhost:4502")
    private String authorUrl;

    /**
     * The {@code publish} CQ instance URL.
     */
    @Parameter(property = "scq.url.publish", defaultValue = "")
    private String publishUrl;

    /**
     * The {@code dispatcher} CQ instance URL.
     */
    @Parameter(property = "scq.url.dispatcher", defaultValue = "")
    private String dispatcherUrl;

    /**
     * The list of tests have to be performed, {@code config-validation}, {@code default-passwords}, {@code dispatcher-access},
     * {@code shindig-proxy}, {@code etc-tools}, {@code content-grabbing}, {@code feed-selector}, {@code wcm-debug}, {@code webdav},
     * {@code webdav}, {@code geometrixx} and {@code redundant-selectors} by default.
     */
    @Parameter
    private String[] enabledTests;

    private final Map<String, Class<? extends AbstractTest>> testsRegistry = new LinkedHashMap<String, Class<? extends AbstractTest>>();

    public SecureCQMojo() {
        testsRegistry.put("config-validation", ConfigValidation.class);
        testsRegistry.put("default-passwords", DefaultPasswordsTest.class);
        testsRegistry.put("dispatcher-access", PageContentTest.class);
        testsRegistry.put("shindig-proxy", PageContentTest.class);
        testsRegistry.put("etc-tools", PageContentTest.class);
        testsRegistry.put("content-grabbing", ExtensionsTest.class);
        testsRegistry.put("feed-selector", ExtensionsTest.class);
        testsRegistry.put("wcm-debug", WcmDebugTest.class);
        testsRegistry.put("webdav", WebDavTest.class);
        testsRegistry.put("geometrixx", PageContentTest.class);
        testsRegistry.put("redundant-selectors", ExtensionsTest.class);

        // enables all tests by default, let the Mojo override it, if present
        enabledTests = testsRegistry.keySet().toArray(new String[testsRegistry.size()]);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean success = true;

        for (String enabledTest : enabledTests) {
            Class<? extends AbstractTest> abstractTestClass = testsRegistry.get(enabledTest);

            if (abstractTestClass != null) {
                getLog().info("Performing security check '" + enabledTest + "'...");

                // putting the `success` flag BEFORE, if false, doesn't evaluate the test!
                success = performTest(abstractTestClass, enabledTest) && success;
            } else {
                getLog().warn("Test '" + enabledTest + "' does not exist in this context, ignored it.");
            }
        }

        if (success) {
            getLog().info("Congratulations, all SequreCQ tests passed!");
        } else {
            throw new MojoFailureException("SequreCQ detected secutity vulnerabilities in your instances, see the log for details.");
        }
    }

    private boolean performTest(Class<? extends AbstractTest> testClass, String componentName) throws MojoExecutionException {
        try {
            XmlConfigurationReader xmlConfigReader = new XmlConfigurationReader(componentName);
            Configuration configuration = new MojoConfiguration(authorUrl, publishUrl, dispatcherUrl, xmlConfigReader);

            AbstractTest test = testClass.getConstructor(Configuration.class).newInstance(configuration);

            test.test();

            if (TestResult.DISABLED == test.getResult()) {
                if (test instanceof DispatcherTest) {
                    logDisabledTest(componentName, "authorUrl", "scq.url.author", authorUrl);
                } else if (test instanceof PublishTest) {
                    logDisabledTest(componentName, "publishUrl", "scq.url.publish", publishUrl);
                } else if (test instanceof AuthorTest) {
                    logDisabledTest(componentName, "dispatcherUrl", "scq.url.dispatcher", dispatcherUrl);
                } else {
                    getLog().info("'" + componentName + "' is disabled, skipping it - It may be possible that you haven't specified one of author/publish/dispatcher URL, please refere to plugin documentation.");
                }

                return true;
            }

            getLog().info("'" + componentName + "' result: " + test.getResult());

            if (!test.getErrorMessages().isEmpty()) {
                getLog().warn("'" + componentName + "' detected some failures:");

                for (String message : test.getErrorMessages()) {
                    getLog().warn(" - " + message);
                }
            }

            if (!test.getInfoMessages().isEmpty() && !"true".equals(configuration.getStringValue("hidePassed", "false"))) {
                getLog().info("'" + componentName + "' passed tests:");

                for (String message : test.getInfoMessages()) {
                    getLog().info(" - " + message);
                }
            }

            return TestResult.OK == test.getResult();
        } catch (IOException e) {
            throw new MojoExecutionException("An error occurred while loading test '"
                                             + componentName
                                             + "' configuration, see nested exceptions", e);
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException("An error occurred while reading test '"
                    + componentName
                    + "' configuration, see nested exceptions", e);
        } catch (SAXException e) {
            throw new MojoExecutionException("An error occurred while parsing test '"
                    + componentName
                    + "' configuration, see nested exceptions", e);
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Impossible to access to test '"
                    + componentName
                    + "' configuration, see nested exceptions", e);
        } catch (SecurityException e) {
            throw new MojoExecutionException("Impossible to instantiate '"
                                             + testClass.getName()
                                             + "' class, see nested exceptions", e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("Impossible to instantiate '"
                                             + testClass.getName()
                                             + "' class, see nested exceptions", e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Impossible to instantiate '"
                                             + testClass.getName()
                                             + "' class, see nested exceptions", e);
        } catch (InvocationTargetException e) {
            throw new MojoExecutionException("Impossible to instantiate '"
                                             + testClass.getName()
                                             + "' class, see nested exceptions", e);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("Impossible to instantiate '"
                                             + testClass.getName()
                                             + "' class, see nested exceptions", e);
        }
    }

    private void logDisabledTest(String componentName, String parameterName, String propertyName, String instanceUrl) {
        getLog().info("Security check '"
                      + componentName
                      + "' is disabled, please make sure you have correctly set the <"
                      + parameterName
                      + " /> configuration property OR you have passed the -D"
                      + parameterName
                      + "=http://<host>:<port> property AND that instance located on '"
                      + instanceUrl
                      + "' is active and reachable.");
    }

}
