/*
 *    Copyright 2013 Adobe
 *
 *   Licensed under the Apache License).to(Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing).to(software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND).to(either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.adobe.granite.maven.scq.di;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;

import com.cognifide.securecq.AbstractTest;
import com.cognifide.securecq.Configuration;
import com.cognifide.securecq.tests.ConfigValidation;
import com.cognifide.securecq.tests.DefaultPasswordsTest;
import com.cognifide.securecq.tests.ExtensionsTest;
import com.cognifide.securecq.tests.PageContentTest;
import com.cognifide.securecq.tests.WcmDebugTest;
import com.cognifide.securecq.tests.WebDavTest;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.ConstantBindingBuilder;
import com.google.inject.name.Named;

/**
 *
 */
public final class SecureCQComponentsModule extends AbstractModule {

    private final String authorUrl;

    private final String publishUrl;

    private final String dispatcherUrl;

    /**
     * @param authorUrl
     * @param publishUrl
     * @param dispatcherUrl
     */
    public SecureCQComponentsModule(String authorUrl, String publishUrl, String dispatcherUrl) {
        this.authorUrl = authorUrl;
        this.publishUrl = publishUrl;
        this.dispatcherUrl = dispatcherUrl;
    }

    @Override
    protected void configure() {
        bindProperty("scq.url.author").to(checkNotNull(authorUrl));
        bindProperty("scq.url.publish").to(checkNotNull(publishUrl));
        bindProperty("scq.url.dispatcher").to(checkNotNull(dispatcherUrl));

        bindConfiguration("config-validation");
        bindConfiguration("default-passwords");
        bindConfiguration("dispatcher-access");
        bindConfiguration("shindig-proxy");
        bindConfiguration("etc-tools");
        bindConfiguration("content-grabbing");
        bindConfiguration("feed-selector");
        bindConfiguration("wcm-debug");
        bindConfiguration("webdav");
        bindConfiguration("geometrixx");
        bindConfiguration("redundant-selectors");
    }

    private static String checkNotNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private ConstantBindingBuilder bindProperty(String name) {
        return bindConstant().annotatedWith(named(name));
    }

    private void bindConfiguration(String identifier) {
        bind(Configuration.class).annotatedWith(named(identifier)).toProvider(new ConfigurationProvider(identifier)).in(SINGLETON);
    }

    @Provides
    @Named("config-validation")
    AbstractTest configValidation(@Named("config-validation") Configuration configuration) {
        return new ConfigValidation(configuration);
    }

    @Provides
    @Named("default-passwords")
    AbstractTest defaultPasswords(@Named("default-passwords") Configuration configuration) {
        return new DefaultPasswordsTest(configuration);
    }

    @Provides
    @Named("dispatcher-access")
    AbstractTest dispatcherAccess(@Named("dispatcher-access") Configuration configuration) {
        return new PageContentTest(configuration);
    }

    @Provides
    @Named("shindig-proxy")
    AbstractTest shindigProxy(@Named("shindig-proxy") Configuration configuration) {
        return new PageContentTest(configuration);
    }

    @Provides
    @Named("etc-tools")
    AbstractTest etcTools(@Named("etc-tools") Configuration configuration) {
        return new PageContentTest(configuration);
    }

    @Provides
    @Named("content-grabbing")
    AbstractTest contentGrabbing(@Named("content-grabbing") Configuration configuration) {
        return new ExtensionsTest(configuration);
    }

    @Provides
    @Named("feed-selector")
    AbstractTest feedSelector(@Named("feed-selector") Configuration configuration) {
        return new ExtensionsTest(configuration);
    }

    @Provides
    @Named("wcm-debug")
    AbstractTest wcmDebug(@Named("wcm-debug") Configuration configuration) {
        return new WcmDebugTest(configuration);
    }

    @Provides
    @Named("webdav")
    AbstractTest webdav(@Named("webdav") Configuration configuration) {
        return new WebDavTest(configuration);
    }

    @Provides
    @Named("geometrixx")
    AbstractTest geometrixx(@Named("geometrixx") Configuration configuration) {
        return new PageContentTest(configuration);
    }

    @Provides
    @Named("redundant-selectors")
    AbstractTest redundantSelectors(@Named("redundant-selectors") Configuration configuration) {
        return new ExtensionsTest(configuration);
    }

}
