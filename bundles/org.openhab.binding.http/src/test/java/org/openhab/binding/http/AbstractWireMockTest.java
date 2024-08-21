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
package org.openhab.binding.http;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;
import org.openhab.core.test.TestPortUtil;
import org.openhab.core.test.java.JavaTest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

/**
 * The {@link AbstractWireMockTest} implements tests for the {@link RateLimitedHttpClient}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractWireMockTest extends JavaTest {
    protected int port = 0;
    protected @NonNullByDefault({}) WireMockServer wireMockServer;
    protected @NonNullByDefault({}) HttpClient httpClient;
    protected ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

    @BeforeAll
    public void initAll() throws Exception {
        port = TestPortUtil.findFreePort();

        wireMockServer = new WireMockServer(options().port(port).extensions(new ResponseTemplateTransformer(false)));
        wireMockServer.start();

        httpClient = new HttpClient();
        httpClient.start();

        configureFor("localhost", port);
    }

    @AfterEach
    public void cleanUpTest() {
        removeAllMappings();
    }

    @AfterAll
    public void cleanUpAll() throws Exception {
        wireMockServer.shutdown();
        scheduler.shutdown();
        httpClient.stop();
    }
}
