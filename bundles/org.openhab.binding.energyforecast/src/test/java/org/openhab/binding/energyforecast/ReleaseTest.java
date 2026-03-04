/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.energyforecast;//

import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ReleaseTest} performs real queries against the ENTSOE web service. Shall be performed before releasing a new
 * version
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ReleaseTest {
    private final Logger logger = LoggerFactory.getLogger(ReleaseTest.class);

    private static HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
    private String token = "YOUR_TOKEN";

    public static void main(String[] args) {
        new ReleaseTest();
    }

    public ReleaseTest() {
        try {
            httpClient.start();
            // ContentResponse response = httpClient.GET("https://api.energy-charts.info/price?bzn=DE-LU");
            ContentResponse response = httpClient
                    .GET("https://www.energyforecast.de/api/v1/predictions/next_96_hours?token=" + token);
            logger.warn("Response status: {}", response.getStatus());
            logger.warn("{}", response.getContentAsString());
            httpClient.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
