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
package org.openhab.binding.draytonwiser.internal.api;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubConfiguration;
import org.openhab.binding.draytonwiser.internal.model.DomainDTO;
import org.openhab.binding.draytonwiser.internal.model.StationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Class with api specific call code.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Moved Api specific code to it's own class
 */
@NonNullByDefault
public class DraytonWiserApi {

    public static final Gson GSON = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    private final Logger logger = LoggerFactory.getLogger(DraytonWiserApi.class);
    private final HttpClient httpClient;

    private HeatHubConfiguration configuration = new HeatHubConfiguration();
    private int failCount;

    public DraytonWiserApi(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setConfiguration(final HeatHubConfiguration configuration) {
        this.configuration = configuration;
    }

    public @Nullable StationDTO getStation() throws DraytonWiserApiException {
        final ContentResponse response = sendMessageToHeatHub(STATION_ENDPOINT, HttpMethod.GET);

        return response == null ? null : GSON.fromJson(response.getContentAsString(), StationDTO.class);
    }

    public @Nullable DomainDTO getDomain() throws DraytonWiserApiException {
        final ContentResponse response = sendMessageToHeatHub(DOMAIN_ENDPOINT, HttpMethod.GET);

        if (response == null) {
            return null;
        }

        try {
            return GSON.fromJson(response.getContentAsString(), DomainDTO.class);
        } catch (final JsonSyntaxException e) {
            logger.debug("Could not parse Json content: {}", e.getMessage(), e);
            return null;
        }
    }

    public void setRoomSetPoint(final int roomId, final int setPoint) throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"Manual\", \"SetPoint\":" + setPoint + "}}";

        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId, "PATCH", payload);
    }

    public void setRoomManualMode(final int roomId, final boolean manualMode) throws DraytonWiserApiException {
        String payload = "{\"Mode\":\"" + (manualMode ? "Manual" : "Auto") + "\"}";
        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId, "PATCH", payload);
        payload = "{\"RequestOverride\":{\"Type\":\"None\",\"Originator\" :\"App\",\"DurationMinutes\":0,\"SetPoint\":0}}";
        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId, "PATCH", payload);
    }

    public void setRoomWindowStateDetection(final int roomId, final boolean windowStateDetection)
            throws DraytonWiserApiException {
        final String payload = windowStateDetection ? "true" : "false";
        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId + "/WindowDetectionActive", "PATCH", payload);
    }

    public void setRoomBoostActive(final int roomId, final int setPoint, final int duration)
            throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"Manual\",\"Originator\" :\"App\",\"DurationMinutes\":"
                + duration + ",\"SetPoint\":" + setPoint + "}}";
        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId, "PATCH", payload);
    }

    public void setRoomBoostInactive(final int roomId) throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"None\",\"Originator\" :\"App\",\"DurationMinutes\":0,\"SetPoint\":0}}";
        sendMessageToHeatHub(ROOMS_ENDPOINT + roomId, "PATCH", payload);
    }

    public void setHotWaterManualMode(final boolean manualMode) throws DraytonWiserApiException {
        String payload = "{\"Mode\":\"" + (manualMode ? "Manual" : "Auto") + "\"}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2", "PATCH", payload);
        payload = "{\"RequestOverride\":{\"Type\":\"None\",\"Originator\" :\"App\",\"DurationMinutes\":0,\"SetPoint\":0}}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2", "PATCH", payload);
    }

    public void setHotWaterSetPoint(final int setPoint) throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"Manual\", \"SetPoint\":" + setPoint + "}}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2", "PATCH", payload);
    }

    public void setHotWaterBoostActive(final int duration) throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"Manual\",\"Originator\" :\"App\",\"DurationMinutes\":"
                + duration + ",\"SetPoint\":1100}}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2", "PATCH", payload);
    }

    public void setHotWaterBoostInactive() throws DraytonWiserApiException {
        final String payload = "{\"RequestOverride\":{\"Type\":\"None\",\"Originator\" :\"App\",\"DurationMinutes\":0,\"SetPoint\":0}}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2", "PATCH", payload);
    }

    public void setAwayMode(final boolean awayMode) throws DraytonWiserApiException {
        final int setPoint = configuration.awaySetPoint * 10;

        String payload = "{\"Type\":" + (awayMode ? "2" : "0") + ", \"setPoint\":" + (awayMode ? setPoint : "0") + "}";
        sendMessageToHeatHub(SYSTEM_ENDPOINT + "RequestOverride", "PATCH", payload);
        payload = "{\"Type\":" + (awayMode ? "2" : "0") + ", \"setPoint\":" + (awayMode ? "-200" : "0") + "}";
        sendMessageToHeatHub(HOTWATER_ENDPOINT + "2/RequestOverride", "PATCH", payload);
    }

    public void setDeviceLocked(final int deviceId, final boolean locked) throws DraytonWiserApiException {
        final String payload = locked ? "true" : "false";
        sendMessageToHeatHub(DEVICE_ENDPOINT + deviceId + "/DeviceLockEnabled", "PATCH", payload);
    }

    public void setEcoMode(final boolean ecoMode) throws DraytonWiserApiException {
        final String payload = "{\"EcoModeEnabled\":" + ecoMode + "}";
        sendMessageToHeatHub(SYSTEM_ENDPOINT, "PATCH", payload);
    }

    public void setSmartPlugManualMode(final int id, final boolean manualMode) throws DraytonWiserApiException {
        final String payload = "{\"Mode\":\"" + (manualMode ? "Manual" : "Auto") + "\"}";
        sendMessageToHeatHub(SMARTPLUG_ENDPOINT + id, "PATCH", payload);
    }

    public void setSmartPlugOutputState(final int id, final boolean outputState) throws DraytonWiserApiException {
        final String payload = "{\"RequestOutput\":\"" + (outputState ? "On" : "Off") + "\"}";
        sendMessageToHeatHub(SMARTPLUG_ENDPOINT + id, "PATCH", payload);
    }

    public void setSmartPlugAwayAction(final int id, final boolean awayAction) throws DraytonWiserApiException {
        final String payload = "{\"AwayAction\":\"" + (awayAction ? "Off" : "NoChange") + "\"}";
        sendMessageToHeatHub(SMARTPLUG_ENDPOINT + id, "PATCH", payload);
    }

    public void setComfortMode(final boolean comfortMode) throws DraytonWiserApiException {
        final String payload = "{\"ComfortModeEnabled\":" + comfortMode + "}";
        sendMessageToHeatHub(SYSTEM_ENDPOINT, "PATCH", payload);
    }

    private synchronized @Nullable ContentResponse sendMessageToHeatHub(final String path, final HttpMethod method)
            throws DraytonWiserApiException {
        return sendMessageToHeatHub(path, method.asString(), "");
    }

    private synchronized @Nullable ContentResponse sendMessageToHeatHub(final String path, final String method,
            final String content) throws DraytonWiserApiException {
        // we need to keep track of the number of times that the heat hub has "failed" to respond.
        // we only actually report a failure if we hit an error state 3 or more times
        try {
            logger.debug("Sending message to heathub: {}", path);
            final StringContentProvider contentProvider = new StringContentProvider(content);
            final ContentResponse response = httpClient
                    .newRequest("http://" + configuration.networkAddress + "/" + path).method(method)
                    .header("SECRET", configuration.secret).content(contentProvider).timeout(10, TimeUnit.SECONDS)
                    .send();

            if (logger.isTraceEnabled()) {
                logger.trace("Reponse (Status:{}): {}", response.getStatus(), response.getContentAsString());
            }
            if (response.getStatus() == HttpStatus.OK_200) {
                failCount = 0;
                return response;
            } else if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                failCount++;
                if (failCount > 2) {
                    throw new DraytonWiserApiException("Invalid authorization token");
                }
            } else {
                failCount++;
                if (failCount > 2) {
                    throw new DraytonWiserApiException("Heathub didn't repond after " + failCount + " retries");
                }
            }
        } catch (final TimeoutException e) {
            failCount++;
            if (failCount > 2) {
                logger.debug("Heathub didn't repond in time: {}", e.getMessage());
                throw new DraytonWiserApiException("Heathub didn't repond in time", e);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            logger.debug("Execution Exception: {}", e.getMessage(), e);
            throw new DraytonWiserApiException(e.getMessage(), e);
        } catch (final RuntimeException e) {
            logger.debug("Unexpected error: {}", e.getMessage(), e);
            throw new DraytonWiserApiException(e.getMessage(), e);
        }
        return null;
    }
}
