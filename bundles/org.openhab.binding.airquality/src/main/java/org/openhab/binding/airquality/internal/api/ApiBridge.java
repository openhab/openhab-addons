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
package org.openhab.binding.airquality.internal.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airquality.internal.AirQualityException;
import org.openhab.binding.airquality.internal.api.dto.AirQualityData;
import org.openhab.binding.airquality.internal.api.dto.AirQualityResponse;
import org.openhab.binding.airquality.internal.api.dto.AirQualityResponse.ResponseStatus;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiBridge} is the interface between handlers
 * and the actual web service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiBridge {
    private static final Gson GSON = new Gson();
    private static final String URL = "http://api.waqi.info/feed/%query%/?token=%apiKey%";
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);
    private final String apiKey;

    public ApiBridge(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the aqicn.org service
     * @throws AirQualityException
     */
    private String buildRequestURL(String key, int stationId, String location) {
        String geoStr = stationId != 0 ? String.format("@%d", stationId)
                : String.format("geo:%s",
                        location.replace(" ", "").replace(",", ";").replace("\"", "").replace("'", "").trim());

        return URL.replace("%apiKey%", key).replace("%query%", geoStr);
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @return an air quality data object mapping the JSON response
     * @throws AirQualityException
     */
    public AirQualityData getData(int stationId, String location, int retryCounter) throws AirQualityException {
        String urlStr = buildRequestURL(apiKey, stationId, location);
        logger.debug("URL = {}", urlStr);

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, null, null, null, REQUEST_TIMEOUT_MS);
            logger.debug("aqiResponse = {}", response);
            AirQualityResponse result = GSON.fromJson(response, AirQualityResponse.class);
            if (result != null && result.getStatus() == ResponseStatus.OK) {
                return result.getData();
            } else if (retryCounter == 0) {
                logger.debug("Error in aqicn.org, retrying once");
                return getData(stationId, location, retryCounter + 1);
            }
            throw new AirQualityException("Error in aqicn.org response: Missing data sub-object");
        } catch (IOException | JsonSyntaxException e) {
            throw new AirQualityException("Communication error", e);
        }
    }
}
