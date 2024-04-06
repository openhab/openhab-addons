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
package org.openhab.binding.onecta.internal;

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;

/**
 * The {@link OnectaConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    private @Nullable static Thing bridgeThing = null;
    private @Nullable static HttpClientFactory httpClientFactory = null;

    private @Nullable static HttpClient httpClient = null;

    public static void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        OnectaConfiguration.httpClientFactory = httpClientFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    public static void setBridgeThing(Thing bridgeThing) {
        OnectaConfiguration.bridgeThing = bridgeThing;
    }

    public static String getHost() {
        return OnectaConfiguration.bridgeThing.getConfiguration().get(CHANNEL_OPENHAB_HOST).toString();
    };

    public static @Nullable HttpClient getHttpClient() {
        return httpClient;
    }

    public static @Nullable HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }
}
