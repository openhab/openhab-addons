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
package org.openhab.binding.nest.internal.sdm.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.nest.internal.sdm.dto.SDMGson.GSON;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMCommandRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMCommandResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMDevice;
import org.openhab.binding.nest.internal.sdm.dto.SDMError;
import org.openhab.binding.nest.internal.sdm.dto.SDMError.SDMErrorDetails;
import org.openhab.binding.nest.internal.sdm.dto.SDMListDevicesResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMListRoomsResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMListStructuresResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMRoom;
import org.openhab.binding.nest.internal.sdm.dto.SDMStructure;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAuthorizationCodeException;
import org.openhab.binding.nest.internal.sdm.listener.SDMAPIRequestListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMAPI} implements the SDM REST API which allows for querying Nest device, structure and room information
 * as well as executing device commands.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/reference/rest">
 *      https://developers.google.com/nest/device-access/reference/rest</a>
 */
@NonNullByDefault
public class SDMAPI {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String REDIRECT_URI = "https://www.google.com";

    private static final String SDM_HANDLE_FORMAT = "%s.sdm";
    private static final String SDM_SCOPE = "https://www.googleapis.com/auth/sdm.service";

    private static final String SDM_URL_PREFIX = "https://smartdevicemanagement.googleapis.com/v1/enterprises/";

    private static final String APPLICATION_JSON = "application/json";
    private static final String BEARER = "Bearer ";
    private static final String IMAGE_JPEG = "image/jpeg";

    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);

    private final Logger logger = LoggerFactory.getLogger(SDMAPI.class);

    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;
    private final OAuthClientService oAuthService;
    private final String oAuthServiceHandleId;
    private final String projectId;

    private final Set<SDMAPIRequestListener> requestListeners = ConcurrentHashMap.newKeySet();

    public SDMAPI(HttpClientFactory httpClientFactory, OAuthFactory oAuthFactory, String ownerId, String projectId,
            String clientId, String clientSecret) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.oAuthFactory = oAuthFactory;
        this.oAuthServiceHandleId = String.format(SDM_HANDLE_FORMAT, ownerId);
        this.oAuthService = oAuthFactory.createOAuthClientService(oAuthServiceHandleId, TOKEN_URL, AUTH_URL, clientId,
                clientSecret, SDM_SCOPE, false);
        this.projectId = projectId;
    }

    public void dispose() {
        requestListeners.clear();
        oAuthFactory.ungetOAuthService(oAuthServiceHandleId);
    }

    public void deleteOAuthServiceAndAccessToken() {
        oAuthFactory.deleteServiceAndAccessToken(oAuthServiceHandleId);
    }

    public void authorizeClient(String authorizationCode) throws InvalidSDMAuthorizationCodeException, IOException {
        try {
            oAuthService.getAccessTokenResponseByAuthorizationCode(authorizationCode, REDIRECT_URI);
        } catch (OAuthException | OAuthResponseException e) {
            throw new InvalidSDMAuthorizationCodeException(
                    "Failed to authorize SDM client. Check the authorization code or generate a new one.", e);
        }
    }

    public void checkAccessTokenValidity() throws InvalidSDMAccessTokenException, IOException {
        getAuthorizationHeader();
    }

    public void addRequestListener(SDMAPIRequestListener listener) {
        requestListeners.add(listener);
    }

    public void removeRequestListener(SDMAPIRequestListener listener) {
        requestListeners.remove(listener);
    }

    public <T extends SDMCommandResponse> @Nullable T executeDeviceCommand(String deviceId,
            SDMCommandRequest<T> request) throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Executing device command for: {}", deviceId);
        String requestContent = GSON.toJson(request);
        String responseContent = postJson(getDeviceUrl(deviceId) + ":executeCommand", requestContent);
        return GSON.fromJson(responseContent, request.getResponseClass());
    }

    private String getAuthorizationHeader() throws InvalidSDMAccessTokenException, IOException {
        try {
            AccessTokenResponse response = oAuthService.getAccessTokenResponse();
            if (response == null || response.getAccessToken() == null || response.getAccessToken().isEmpty()) {
                throw new InvalidSDMAccessTokenException("No SDM access token. Client may not have been authorized.");
            }
            if (response.getRefreshToken() == null || response.getRefreshToken().isEmpty()) {
                throw new InvalidSDMAccessTokenException(
                        "No SDM refresh token. Delete and readd credentials, then reauthorize.");
            }
            return BEARER + response.getAccessToken();
        } catch (OAuthException | OAuthResponseException e) {
            throw new InvalidSDMAccessTokenException(
                    "Error fetching SDM access token. Check the authorization code or generate a new one.", e);
        }
    }

    public byte[] getCameraImage(String url, String token, @Nullable BigDecimal imageWidth,
            @Nullable BigDecimal imageHeight) throws FailedSendingSDMDataException {
        try {
            logger.debug("Getting camera image from: {}", url);

            Request request = httpClient.newRequest(url) //
                    .method(GET) //
                    .header(ACCEPT, IMAGE_JPEG) //
                    .header(AUTHORIZATION, token) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS);

            if (imageWidth != null) {
                request = request.param("width", Long.toString(imageWidth.longValue()));
            } else if (imageHeight != null) {
                request = request.param("height", Long.toString(imageHeight.longValue()));
            }

            ContentResponse contentResponse = request.send();
            logResponseErrors(contentResponse);
            logger.debug("Retrieved camera image from: {}", url);
            requestListeners.forEach(SDMAPIRequestListener::onSuccess);
            return contentResponse.getContent();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.debug("Failed to get camera image", e);
            FailedSendingSDMDataException exception = new FailedSendingSDMDataException("Failed to get camera image",
                    e);
            requestListeners.forEach(listener -> listener.onError(exception));
            throw exception;
        }
    }

    public @Nullable SDMDevice getDevice(String deviceId)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Getting device: {}", deviceId);
        return GSON.fromJson(getJson(getDeviceUrl(deviceId)), SDMDevice.class);
    }

    public @Nullable SDMStructure getStructure(String structureId)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Getting structure: {}", structureId);
        return GSON.fromJson(getJson(getStructureUrl(structureId)), SDMStructure.class);
    }

    public @Nullable SDMRoom getRoom(String structureId, String roomId)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Getting structure {} room: {}", structureId, roomId);
        return GSON.fromJson(getJson(getRoomUrl(structureId, roomId)), SDMRoom.class);
    }

    private String getProjectUrl() {
        return SDM_URL_PREFIX + projectId;
    }

    private String getDevicesUrl() {
        return getProjectUrl() + "/devices";
    }

    private String getDevicesUrl(String pageToken) {
        return getDevicesUrl() + "?pageToken=" + pageToken;
    }

    private String getDeviceUrl(String deviceId) {
        return getDevicesUrl() + "/" + deviceId;
    }

    private String getStructuresUrl() {
        return getProjectUrl() + "/structures";
    }

    private String getStructuresUrl(String pageToken) {
        return getStructuresUrl() + "?pageToken=" + pageToken;
    }

    private String getStructureUrl(String structureId) {
        return getStructuresUrl() + "/" + structureId;
    }

    private String getRoomsUrl(String structureId) {
        return getStructureUrl(structureId) + "/rooms";
    }

    private String getRoomsUrl(String structureId, String pageToken) {
        return getRoomsUrl(structureId) + "?pageToken=" + pageToken;
    }

    private String getRoomUrl(String structureId, String roomId) {
        return getRoomsUrl(structureId) + "/" + roomId;
    }

    public List<SDMDevice> listDevices() throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Listing devices");
        SDMListDevicesResponse response = GSON.fromJson(getJson(getDevicesUrl()), SDMListDevicesResponse.class);
        List<SDMDevice> result = response == null ? List.of() : response.devices;
        while (response != null && !response.nextPageToken.isEmpty()) {
            response = GSON.fromJson(getJson(getDevicesUrl(response.nextPageToken)), SDMListDevicesResponse.class);
            if (response != null) {
                result.addAll(response.devices);
            }
        }
        return result;
    }

    public List<SDMStructure> listStructures() throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Listing structures");
        SDMListStructuresResponse response = GSON.fromJson(getJson(getStructuresUrl()),
                SDMListStructuresResponse.class);
        List<SDMStructure> result = response == null ? List.of() : response.structures;
        while (response != null && !response.nextPageToken.isEmpty()) {
            response = GSON.fromJson(getJson(getStructuresUrl(response.nextPageToken)),
                    SDMListStructuresResponse.class);
            if (response != null) {
                result.addAll(response.structures);
            }
        }
        return result;
    }

    public List<SDMRoom> listRooms(String structureId)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        logger.debug("Listing rooms for structure: {}", structureId);
        SDMListRoomsResponse response = GSON.fromJson(getJson(getRoomsUrl(structureId)), SDMListRoomsResponse.class);
        List<SDMRoom> result = response == null ? List.of() : response.rooms;
        while (response != null && !response.nextPageToken.isEmpty()) {
            response = GSON.fromJson(getJson(getRoomsUrl(structureId, response.nextPageToken)),
                    SDMListRoomsResponse.class);
            if (response != null) {
                result.addAll(response.rooms);
            }
        }
        return result;
    }

    private void logResponseErrors(ContentResponse contentResponse) {
        if (contentResponse.getStatus() >= 400) {
            logger.debug("SDM API error: {}", contentResponse.getContentAsString());

            SDMError error = GSON.fromJson(contentResponse.getContentAsString(), SDMError.class);
            SDMErrorDetails details = error == null ? null : error.error;

            if (details != null && !details.message.isBlank()) {
                logger.warn("SDM API error: {}", details.message);
            } else {
                logger.warn("SDM API error: {} (HTTP {})", contentResponse.getReason(), contentResponse.getStatus());
            }
        }
    }

    private String getJson(String url) throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        try {
            logger.debug("Getting JSON from: {}", url);
            ContentResponse contentResponse = httpClient.newRequest(url) //
                    .method(GET) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .header(AUTHORIZATION, getAuthorizationHeader()) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send();
            logResponseErrors(contentResponse);
            String response = contentResponse.getContentAsString();
            logger.debug("Response: {}", response);
            requestListeners.forEach(SDMAPIRequestListener::onSuccess);
            return response;
        } catch (ExecutionException | InterruptedException | IOException | TimeoutException e) {
            logger.debug("Failed to send JSON GET request", e);
            FailedSendingSDMDataException exception = new FailedSendingSDMDataException(
                    "Failed to send JSON GET request", e);
            requestListeners.forEach(listener -> listener.onError(exception));
            throw exception;
        }
    }

    private String postJson(String url, String requestContent)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        try {
            logger.debug("Posting JSON to: {}", url);
            ContentResponse contentResponse = httpClient.newRequest(url) //
                    .method(POST) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .header(AUTHORIZATION, getAuthorizationHeader()) //
                    .content(new StringContentProvider(requestContent), APPLICATION_JSON) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send();
            logResponseErrors(contentResponse);
            String response = contentResponse.getContentAsString();
            logger.debug("Response: {}", response);
            requestListeners.forEach(SDMAPIRequestListener::onSuccess);
            return response;
        } catch (ExecutionException | InterruptedException | IOException | TimeoutException e) {
            logger.debug("Failed to send JSON POST request", e);
            FailedSendingSDMDataException exception = new FailedSendingSDMDataException(
                    "Failed to send JSON POST request", e);
            requestListeners.forEach(listener -> listener.onError(exception));
            throw exception;
        }
    }
}
