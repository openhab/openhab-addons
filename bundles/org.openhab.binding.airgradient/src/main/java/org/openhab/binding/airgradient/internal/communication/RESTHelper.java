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
package org.openhab.binding.airgradient.internal.communication;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CALIBRATE_CO2_PATH;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CURRENT_MEASURES_PATH;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.LEDS_MODE_PATH;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.LOCAL_CONFIG_PATH;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.REQUEST_TIMEOUT;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;

/**
 * Helper for doing rest calls to the API.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class RESTHelper {

    public static @Nullable String generateMeasuresUrl(AirGradientAPIConfiguration apiConfig) {
        if (apiConfig.hasCloudUrl()) {
            return apiConfig.hostname + String.format(CURRENT_MEASURES_PATH, apiConfig.token);
        } else {
            return apiConfig.hostname;
        }
    }

    public static @Nullable String generateConfigUrl(AirGradientAPIConfiguration apiConfig) {
        URI uri = URI.create(apiConfig.hostname);
        URI configUri = uri.resolve(LOCAL_CONFIG_PATH);
        return configUri.toString();
    }

    public static @Nullable String generateCalibrationCo2Url(AirGradientAPIConfiguration apiConfig, String serialNo) {
        if (apiConfig.hasCloudUrl()) {
            return apiConfig.hostname + String.format(CALIBRATE_CO2_PATH, serialNo, apiConfig.token);
        } else {
            return generateConfigUrl(apiConfig);
        }
    }

    public static @Nullable String generateGetLedsModeUrl(AirGradientAPIConfiguration apiConfig, String serialNo) {
        if (apiConfig.hasCloudUrl()) {
            return apiConfig.hostname + String.format(LEDS_MODE_PATH, serialNo, apiConfig.token);
        } else {
            return generateConfigUrl(apiConfig);
        }
    }

    public static @Nullable Request generateRequest(HttpClient httpClient, @Nullable String url) {
        return generateRequest(httpClient, url, HttpMethod.GET);
    }

    public static @Nullable Request generateRequest(HttpClient httpClient, @Nullable String url, HttpMethod method) {
        if (url == null) {
            return null;
        }

        Request request = httpClient.newRequest(url);
        request.timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        request.method(method);
        return request;
    }
}
