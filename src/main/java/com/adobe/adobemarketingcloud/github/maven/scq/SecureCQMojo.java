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
package com.adobe.adobemarketingcloud.github.maven.scq;

import static java.util.Arrays.asList;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.Key.get;
import static com.google.inject.name.Names.named;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.adobe.adobemarketingcloud.github.maven.scq.di.SecureCQComponentsModule;
import com.cognifide.securecq.AbstractTest;
import com.cognifide.securecq.Configuration;
import com.cognifide.securecq.TestResult;
import com.cognifide.securecq.markers.AuthorTest;
import com.cognifide.securecq.markers.DispatcherTest;
import com.cognifide.securecq.markers.PublishTest;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        // setup tests
        Injector injector = createInjector(new SecureCQComponentsModule(authorUrl, publishUrl, dispatcherUrl));

        // discover all available predefined tests
        if (enabledTests == null || enabledTests.length == 0) {
            List<String> boundTests = new LinkedList<String>();

            TypeLiteral<AbstractTest> abstractTestTypeLiteral = new TypeLiteral<AbstractTest>(){};

            for (Entry<Key<?>, Binding<?>> binding : injector.getAllBindings().entrySet()) {
                Key<?> bindingKey = binding.getKey();

                if (abstractTestTypeLiteral.equals(bindingKey.getTypeLiteral())
                        && bindingKey.getAnnotation() != null
                        && Named.class.isAssignableFrom(bindingKey.getAnnotationType())) {
                    boundTests.add(((Named) bindingKey.getAnnotation()).value());
                }
            }

            enabledTests = boundTests.toArray(new String[boundTests.size()]);
        }

        getLog().debug("Performing tests: " + asList(enabledTests));

        // perform tests

        boolean success = true;

        for (String enabledTest : enabledTests) {
            getLog().info("Discovering test '" + enabledTest + "'...");

            try {
                AbstractTest test = injector.getInstance(get(AbstractTest.class, named(enabledTest)));

                getLog().info("Performing security check '" + enabledTest + "'...");

                // configuration exists at that point
                Configuration configuration = injector.getInstance(get(Configuration.class, named(enabledTest)));

                // putting the `success` flag BEFORE, if false, doesn't evaluate the test!
                success = performTest(enabledTest, test, configuration) && success;
            } catch (ConfigurationException e) {
                getLog().warn("Test '" + enabledTest + "' does not exist in this context, ignored it.");
            }
        }

        if (success) {
            getLog().info("Congratulations, all SequreCQ tests passed!");
        } else {
            throw new MojoFailureException("SequreCQ detected secutity vulnerabilities in your instances, see the log for details.");
        }
    }

    private boolean performTest(String componentName, AbstractTest test, Configuration configuration) throws MojoExecutionException {
        try {
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
