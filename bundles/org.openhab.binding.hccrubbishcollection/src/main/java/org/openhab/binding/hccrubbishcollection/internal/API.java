/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hccrubbishcollection.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link API} holds the code to query the API for rubbish dates.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class API {
    private static final int REQUEST_TIMEOUT = 10;
    private static final String REQUEST_URL = "https://hccfightthelandfill.azure-api.net/get_Collection_Dates?address_string=";
    private static final int HTTP_OK = 200;

    private final Logger logger = LoggerFactory.getLogger(API.class);

    private final HttpClient httpClient;
    private final String address;

    private String errorDetailMessage = "";
    private ThingStatusDetail errorDetail = ThingStatusDetail.NONE;

    private @Nullable Integer collectionWeek = null;
    private @Nullable Integer day = null;
    private @Nullable ZonedDateTime recycling = null;
    private @Nullable ZonedDateTime general = null;

    public API(HttpClient httpClient, String address) {
        this.httpClient = httpClient;
        this.address = address;
    }

    public boolean update() {
        try {
            final String url = REQUEST_URL + URLEncoder.encode(address, StandardCharsets.UTF_8.toString());

            logger.debug("Fetching data from URL {} (address hidden)", REQUEST_URL);

            ContentResponse response = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS).send();

            if (response.getStatus() == HTTP_OK) {
                String content = response.getContentAsString();
                String cleanedContent = content.trim().substring(1, content.length() - 1);
                logger.trace("Got cleaned content: {}", cleanedContent);

                JsonObject jsonResponse = new JsonParser().parse(cleanedContent).getAsJsonObject();

                JsonElement dayElement = jsonResponse.get("CollectionDay");
                JsonElement collectionWeekElement = jsonResponse.get("CollectionWeek");
                JsonElement generalElement = jsonResponse.get("RedBin");
                JsonElement recyclingElement = jsonResponse.get("YellowBin");

                if (generalElement == null || recyclingElement == null) {
                    logger.debug("RedBin or YellowBin object is missing. Invalid premises or address");

                    errorDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                    errorDetailMessage = "Invalid address";
                    return false;
                }

                LocalDateTime localGeneralDate = LocalDateTime.parse(generalElement.getAsString());
                LocalDateTime localRecyclingDate = LocalDateTime.parse(recyclingElement.getAsString());

                ZoneId zone = ZonedDateTime.now().getZone();

                ZonedDateTime zonedGeneralDate = ZonedDateTime.of(localGeneralDate, zone);
                ZonedDateTime zonedRecyclingDate = ZonedDateTime.of(localRecyclingDate, zone);

                errorDetail = ThingStatusDetail.NONE;

                recycling = zonedRecyclingDate;
                general = zonedGeneralDate;

                day = dayElement.getAsInt();
                collectionWeek = collectionWeekElement.getAsInt();

                return true;
            } else {
                logger.error("Data fetch failed, got HTTP Code {}", response.getStatus());
                errorDetail = ThingStatusDetail.COMMUNICATION_ERROR;
                errorDetailMessage = "HTTP Code " + response.getStatus();
                return false;
            }
        } catch (UnsupportedEncodingException ue) {
            errorDetail = ThingStatusDetail.COMMUNICATION_ERROR;
            errorDetailMessage = "Encoding not supported!";
            return false;
        } catch (TimeoutException to) {
            errorDetail = ThingStatusDetail.COMMUNICATION_ERROR;
            errorDetailMessage = "Response Timeout (will try again soon)";
            return false;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    public ThingStatusDetail getErrorDetail() {
        return errorDetail;
    }

    public String getErrorDetailMessage() {
        return errorDetailMessage;
    }

    public @Nullable Integer getCollectionWeek() {
        return collectionWeek;
    }

    public @Nullable Integer getDay() {
        return day;
    }

    public @Nullable ZonedDateTime getRecyclingDate() {
        return recycling;
    }

    public @Nullable ZonedDateTime getGeneralDate() {
        return general;
    }
}
