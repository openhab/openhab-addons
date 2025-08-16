/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.client.HttpClient;

/**
 * @author Christian Kemper - Initial contribution
 */
public class SungrowClientFactory {

    private SungrowClientFactory() {
    }

    public static SungrowClient createSungrowClient(HttpClient commonHttpClient, URI baseUrl, String appKey,
            String secretKey) {
        return new SungrowClient(commonHttpClient, baseUrl, appKey, secretKey);
    }

    public enum Region {
        CHINA("https://gateway.isolarcloud.com/"),
        INTERNATIONAL("https://gateway.isolarcloud.com.hk/"),
        EUROPE("https://gateway.isolarcloud.eu/"),
        AUSTRALIA("https://augateway.isolarcloud.com/");

        private final URI baseUrl;

        Region(String baseUrl) {
            try {
                this.baseUrl = new URI(baseUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public URI getBaseUrl() {
            return baseUrl;
        }
    }
}
