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
package org.openhab.binding.automower.internal.rest.api.automowerconnect;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.automower.internal.rest.api.HusqvarnaApi;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCalendardRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerMessagesResult;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerResult;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerSettingsRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZoneRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaRequest;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.rest.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Allows access to the AutomowerConnectApi
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerConnectApi extends HusqvarnaApi {

    private final Logger logger = LoggerFactory.getLogger(AutomowerConnectApi.class);

    public AutomowerConnectApi(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getBaseUrl() {
        return "https://api.amc.husqvarna.dev/v1";
    }

    public MowerListResult getMowers(String appKey, String token) throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/mowers");
        request.method(HttpMethod.GET);

        ContentResponse response = executeRequest(appKey, token, request);
        logger.trace("getMowers: {}", response.getContentAsString());
        return parseResponse(response, MowerListResult.class);
    }

    public MowerResult getMower(String appKey, String token, String mowerId) throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/mowers/" + mowerId);
        request.method(HttpMethod.GET);

        ContentResponse response = executeRequest(appKey, token, request);
        logger.trace("getMower: {}", response.getContentAsString());
        return parseResponse(response, MowerResult.class);
    }

    public MowerMessagesResult getMowerMessages(String appKey, String token, String mowerId)
            throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/mowers/" + mowerId + "/messages");
        request.method(HttpMethod.GET);

        ContentResponse response = executeRequest(appKey, token, request);
        logger.trace("getMowerMessages: {}", response.getContentAsString());
        return parseResponse(response, MowerMessagesResult.class);
    }

    public void sendCommand(String appKey, String token, String id, MowerCommandRequest command)
            throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/mowers/" + id + "/actions");
        request.method(HttpMethod.POST);

        logger.trace("sendCommand: {}", gson.toJson(command));
        request.content(new StringContentProvider(gson.toJson(command)));

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendCalendar(String appKey, String token, String id, boolean hasWorkAreas, @Nullable Long workAreaId,
            MowerCalendardRequest calendar) throws AutomowerCommunicationException {
        String url;
        if (hasWorkAreas && (workAreaId != null)) {
            url = getBaseUrl() + "/mowers/" + id + "/workAreas/" + workAreaId + "/calendar";
        } else {
            url = getBaseUrl() + "/mowers/" + id + "/calendar";
        }
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.POST);

        logger.trace("sendCalendar: {}", gson.toJson(calendar));
        request.content(new StringContentProvider(gson.toJson(calendar)));

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendSettings(String appKey, String token, String id, MowerSettingsRequest settings)
            throws AutomowerCommunicationException {
        String url;
        url = getBaseUrl() + "/mowers/" + id + "/settings";
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.POST);

        logger.trace("sendSettings: {}", gson.toJson(settings));
        request.content(new StringContentProvider(gson.toJson(settings)));

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendConfirmError(String appKey, String token, String id) throws AutomowerCommunicationException {
        String url;
        url = getBaseUrl() + "/mowers/" + id + "/errors/confirm";
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.POST);

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendResetCuttingBladeUsageTime(String appKey, String token, String id)
            throws AutomowerCommunicationException {
        String url;
        url = getBaseUrl() + "/mowers/" + id + "/statistics/resetCuttingBladeUsageTime";
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.POST);

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendStayOutZone(String appKey, String token, String id, String zoneId,
            MowerStayOutZoneRequest zoneRequest) throws AutomowerCommunicationException {
        String url;
        url = getBaseUrl() + "/mowers/" + id + "/stayOutZones/" + zoneId;
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.PATCH);

        logger.trace("sendStayOutZone: {}", gson.toJson(zoneRequest));
        request.content(new StringContentProvider(gson.toJson(zoneRequest)));

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    public void sendWorkArea(String appKey, String token, String id, long workAreaId,
            MowerWorkAreaRequest workAreaRequest) throws AutomowerCommunicationException {
        String url;
        url = getBaseUrl() + "/mowers/" + id + "/workAreas/" + workAreaId;
        final Request request = getHttpClient().newRequest(url);
        request.method(HttpMethod.PATCH);

        logger.trace("sendWorkArea: {}", gson.toJson(workAreaRequest));
        request.content(new StringContentProvider(gson.toJson(workAreaRequest)));

        ContentResponse response = executeRequest(appKey, token, request);

        checkForError(response, response.getStatus());
    }

    private ContentResponse executeRequest(String appKey, String token, final Request request)
            throws AutomowerCommunicationException {
        request.timeout(10, TimeUnit.SECONDS);

        request.header("Authorization-Provider", "husqvarna");
        request.header("Authorization", "Bearer " + token);
        request.header("X-Api-Key", appKey);
        request.header("Content-Type", "application/vnd.api+json");

        ContentResponse response;
        try {
            response = request.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new AutomowerCommunicationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AutomowerCommunicationException(e);
        }
        return response;
    }

    private <T> T parseResponse(ContentResponse response, Class<T> type) throws AutomowerCommunicationException {
        int statusCode = response.getStatus();

        checkForError(response, statusCode);
        try {
            return gson.fromJson(response.getContentAsString(), type);
        } catch (JsonSyntaxException e) {
            throw new AutomowerCommunicationException(e);
        }
    }

    private void checkForError(ContentResponse response, int statusCode) throws AutomowerCommunicationException {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        switch (statusCode) {
            case HttpStatus.NOT_FOUND_404:
                throw new AutomowerCommunicationException(statusCode, "Target '" + response.getRequest().getURI()
                        + "' seems to be not available: " + response.getContentAsString());

            case HttpStatus.FORBIDDEN_403:
            case HttpStatus.UNAUTHORIZED_401:
                throw new UnauthorizedException(statusCode, response.getContentAsString());

            default:
                throw new AutomowerCommunicationException(statusCode, response.getContentAsString());
        }
    }
}
