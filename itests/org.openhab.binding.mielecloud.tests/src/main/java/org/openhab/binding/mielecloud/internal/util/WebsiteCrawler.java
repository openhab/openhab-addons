/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mielecloud.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Allows for requesting website content from URLs.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class WebsiteCrawler {
    private HttpClient httpClient;

    public WebsiteCrawler(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    /**
     * Gets a website relative to the address of the openHAB installation running in test mode during integration tests.
     *
     * @param relativeUrl The relative URL.
     * @return The website.
     * @throws Exception if anything goes wrong.
     */
    public Website doGetRelative(String relativeUrl) throws Exception {
        ContentResponse response = httpClient.GET("http://127.0.0.1:" + getServerPort() + relativeUrl);
        assertEquals(200, response.getStatus());
        return new Website(response.getContentAsString());
    }

    /**
     * Gets the port the webserver for this integration test instance is running on. The port is reserved in the pom.xml
     * by the build-helper-maven-plugin.
     */
    public static int getServerPort() {
        return Integer.getInteger("org.osgi.service.http.port", 8080);
    }
}
