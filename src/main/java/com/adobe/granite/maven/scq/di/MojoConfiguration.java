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
package com.adobe.granite.maven.scq.di;

import org.apache.commons.lang.StringUtils;

import com.cognifide.securecq.Configuration;
import com.cognifide.securecq.cli.XmlConfigurationReader;

/**
 *
 */
final class MojoConfiguration implements Configuration {

    private final String authorUrl;

    private final String publishUrl;

    private final String dispatcherUrl;

    private final XmlConfigurationReader xmlConfigReader;

    public MojoConfiguration(String authorUrl, String publishUrl, String dispatcherUrl, XmlConfigurationReader xmlConfigReader) {
        this.authorUrl = authorUrl;
        this.publishUrl = publishUrl;
        this.dispatcherUrl = dispatcherUrl;
        this.xmlConfigReader = xmlConfigReader;
    }

    public String getDispatcherUrl() {
        return dispatcherUrl;
    }

    public String getAuthor() {
        return authorUrl;
    }

    public String getPublish() {
        return publishUrl;
    }

    public String getStringValue(String name, String defaultValue) {
        // a small hack to enable a test via Mojo without altering the configs
        if ("enabled".equals(name)) {
            return "true";
        }

        return StringUtils.defaultIfEmpty(xmlConfigReader.getValue(name), defaultValue);
    }

    public String[] getStringList(String name) {
        return xmlConfigReader.getValueList(name);
    }

    public static String makeUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        String result = StringUtils.removeEnd(url, "/");
        if (!result.startsWith("http://") && !result.startsWith("https://")) {
            result = StringUtils.removeStart(result, "/");
            result = "http://" + result;
        }
        return result;
    }

}
