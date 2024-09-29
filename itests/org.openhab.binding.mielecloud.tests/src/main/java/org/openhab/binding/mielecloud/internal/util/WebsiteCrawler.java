/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Allows for requesting website content from URLs.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class WebsiteCrawler implements AutoCloseable {
    private HttpClient httpClient;

    public WebsiteCrawler(HttpClientFactory httpClientFactory) throws Exception {
        this.httpClient = httpClientFactory.createHttpClient("mielecloud-int-tests");
        this.httpClient.start();
    }

    /**
     * Gets a website during integration tests.
     * The resulting website will be the one obtained after following all redirections.
     *
     * @param url The URL.
     * @return The website.
     * @throws Exception if anything goes wrong.
     */
    public Website doGet(String url) throws Exception {
        httpClient.setFollowRedirects(true);
        ContentResponse response = httpClient.GET(url);
        assertEquals(200, response.getStatus());
        return new Website(response.getContentAsString());
    }

    /**
     * Gets a website relative to the address of the openHAB installation running in test mode during integration tests.
     * The resulting website will be the one obtained after following all redirections.
     *
     * @param relativeUrl The relative URL.
     * @return The website.
     * @throws Exception if anything goes wrong.
     */
    public Website doGetRelative(String relativeUrl) throws Exception {
        return doGet("http://127.0.0.1:" + getServerPort() + relativeUrl);
    }

    /**
     * Gets a redirection URL from an URL relative to the address of the openHAB installation running in test mode
     * during integration tests expecting to receive a 302 Found response.
     *
     * @param relativeUrl The relative URL.
     * @return The website that the client was redirected to.
     * @throws Exception if anything goes wrong.
     */
    public String doGetRedirectUrlRelative(String relativeUrl) throws Exception {
        httpClient.setFollowRedirects(false);
        ContentResponse response = httpClient.GET("http://127.0.0.1:" + getServerPort() + relativeUrl);
        assertEquals(302, response.getStatus());
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    /**
     * Gets the port the webserver for this integration test instance is running on. The port is reserved in the pom.xml
     * by the build-helper-maven-plugin.
     */
    public static int getServerPort() {
        return Integer.getInteger("org.osgi.service.http.port", 8080);
    }

    @Override
    public void close() throws Exception {
        httpClient.stop();
    }
}
