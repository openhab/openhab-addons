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
package org.openhab.binding.tedee.internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * Access for the Tedee Cloud Api.
 *
 * @author Alex Goll - Initial contribution
 */

@NonNullByDefault
public class TedeeCloudApi implements TedeeClient {

    private static final String BASE = "https://api.tedee.com/api/v37";
    private static final String LOCKS_PATH = "/my/lock/";
    private static final String LOCK_OPERATION_PATH = "/operation/";
    private static final String LOCK_OPERATION = "lock";
    private static final String UNLOCK_OPERATION = "unlock";
    private static final String PULL_OPERATION = "pull";
    private static final String ITEMS_PER_PAGE_PATH = BASE + "/my/lock/?ItemsPerPage=";
    private static final String PAGE_PARAMETER = "&Page=";
    private static final String MODE_PARAMETER = "?mode=";
    private static final String UNLOCK_WITHOUT_PULL_MODE = "3";
    private static final String UNLOCK_OR_PULL_MODE = "4";
    private static final String PERSONAL_KEY_AUTHORIZATION_SCHEME = "PersonalKey";
    private static final String JSON_CONTENT_TYPE = MimeTypes.Type.APPLICATION_JSON.asString();
    private static final String JSON_PATCH_CONTENT_TYPE = "application/json-patch+json";
    private static final String EMPTY_CONTENT_LENGTH = "0";
    private static final int GET_LOCKS_TIMEOUT_SECONDS = 10;

    private final HttpClient client;

    private final Gson gson = new Gson();

    private final String personalAccessKey;

    public TedeeCloudApi(HttpClient client, String personalAccessKey) {
        this.client = client;
        this.personalAccessKey = personalAccessKey;
    }

    @Override
    public TedeeLock getLock(int id) throws TedeeApiException {
        ContentResponse response = execute(client.newRequest(BASE + LOCKS_PATH + id).method(HttpMethod.GET));
        if (response.getStatus() != HttpStatus.OK_200) {
            throw error(response);
        }
        JsonObject root = parseObject(response.getContentAsString());
        JsonElement result = root.get("result");
        if (result == null || !result.isJsonObject()) {
            throw new TedeeApiException("Invalid Tedee Cloud lock response", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return toTedeeLock(result.getAsJsonObject());
    }

    @Override
    public List<TedeeLock> getLocks() throws TedeeApiException {
        List<TedeeLock> locks = new ArrayList<>();
        int page = 1;
        final int itemsPerPage = 100;
        while (true) {
            String url = ITEMS_PER_PAGE_PATH + itemsPerPage + PAGE_PARAMETER + page;
            Request request = client.newRequest(url).timeout(GET_LOCKS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .method(HttpMethod.GET);
            ContentResponse response = execute(request);
            if (response.getStatus() != HttpStatus.OK_200) {
                throw error(response);
            }
            JsonObject root = parseObject(response.getContentAsString());
            JsonElement result = root.get("result");
            if (result == null || !result.isJsonArray()) {
                throw new TedeeApiException("Invalid Tedee Cloud lock list response",
                        HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            JsonArray array = result.getAsJsonArray();
            if (array.isEmpty()) {
                break;
            }
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    locks.add(toTedeeLock(element.getAsJsonObject()));
                }
            }
            if (array.size() < itemsPerPage) {
                break;
            }
            page++;
        }
        return locks;
    }

    @Override
    public void lock(int id) throws TedeeApiException {
        operation(LOCKS_PATH + id + LOCK_OPERATION_PATH + LOCK_OPERATION);
    }

    @Override
    public void unlock(int id) throws TedeeApiException {
        operation(LOCKS_PATH + id + LOCK_OPERATION_PATH + UNLOCK_OPERATION);
    }

    @Override
    public void unlockWithoutPull(int id) throws TedeeApiException {
        operation(LOCKS_PATH + id + LOCK_OPERATION_PATH + UNLOCK_OPERATION + MODE_PARAMETER + UNLOCK_WITHOUT_PULL_MODE);
    }

    @Override
    public void pull(int id) throws TedeeApiException {
        operation(LOCKS_PATH + id + LOCK_OPERATION_PATH + PULL_OPERATION);
    }

    @Override
    public void unlockOrPull(int id) throws TedeeApiException {
        operation(LOCKS_PATH + id + LOCK_OPERATION_PATH + UNLOCK_OPERATION + MODE_PARAMETER + UNLOCK_OR_PULL_MODE);
    }

    private void operation(String path) throws TedeeApiException {
        Request request = client.newRequest(BASE + path).method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, JSON_PATCH_CONTENT_TYPE);
        request.header(HttpHeader.CONTENT_LENGTH, EMPTY_CONTENT_LENGTH);
        ContentResponse response = execute(request);
        if (response.getStatus() < HttpStatus.OK_200 || response.getStatus() >= HttpStatus.MULTIPLE_CHOICES_300) {
            throw error(response);
        }
    }

    private ContentResponse execute(Request request) throws TedeeApiException {
        request.header(HttpHeader.AUTHORIZATION, PERSONAL_KEY_AUTHORIZATION_SCHEME + " " + personalAccessKey);
        request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
        try {
            return request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TedeeApiException("Interrupted", HttpStatus.REQUEST_TIMEOUT_408, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TedeeApiException("HTTP failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR_500, e);
        }
    }

    private JsonObject parseObject(String content) throws TedeeApiException {
        try {
            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonObject()) {
                throw new TedeeApiException("Invalid Tedee Cloud JSON response", HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            return root.getAsJsonObject();
        } catch (TedeeApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new TedeeApiException("Invalid Tedee Cloud JSON response", HttpStatus.INTERNAL_SERVER_ERROR_500, e);
        }
    }

    private TedeeLock toTedeeLock(JsonObject source) throws TedeeApiException {
        JsonObject local = new JsonObject();
        local.addProperty("type", intValue(source, "type", 2));
        local.addProperty("id", intValue(source, "id", 0));
        local.addProperty("name", stringValue(source, "name", ""));
        local.addProperty("serialNumber", stringValue(source, "serialNumber", ""));
        local.addProperty("isConnected", booleanAsInt(source, "isConnected"));
        local.addProperty("rssi", 0);
        local.addProperty("deviceRevision", intValue(source, "deviceRevision", 0));
        local.addProperty("version", softwareVersion(source));
        JsonElement deviceStateElement = source.get("deviceState");
        if (deviceStateElement != null && deviceStateElement.isJsonObject()) {
            JsonObject deviceState = deviceStateElement.getAsJsonObject();
            local.addProperty("state", intValue(deviceState, "state", 9));
            local.addProperty("doorState", cloudDoorState(deviceState));
            local.addProperty("batteryLevel", intValue(deviceState, "batteryLevel", 0));
            local.addProperty("isCharging", booleanAsInt(deviceState, "isCharging"));
        } else {
            local.addProperty("state", 9);
            local.addProperty("doorState", 0);
            local.addProperty("batteryLevel", 0);
            local.addProperty("isCharging", 0);
        }
        local.addProperty("jammed", 0);
        local.add("deviceSettings", deviceSettings(source));
        TedeeLock lock = gson.fromJson(local, TedeeLock.class);
        if (lock == null) {
            throw new TedeeApiException("Invalid Tedee Cloud lock model", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return lock;
    }

    private JsonObject deviceSettings(JsonObject source) {
        JsonElement settingsElement = source.get("deviceSettings");
        JsonObject settings = settingsElement != null && settingsElement.isJsonObject()
                ? settingsElement.getAsJsonObject()
                : new JsonObject();
        JsonObject result = new JsonObject();
        result.addProperty("autoLockEnabled", booleanAsInt(settings, "autoLockEnabled"));
        result.addProperty("autoLockDelay", intValue(settings, "autoLockDelay", 0));
        result.addProperty("autoLockImplicitEnabled", booleanAsInt(settings, "autoLockImplicitEnabled"));
        result.addProperty("autoLockImplicitDelay", intValue(settings, "autoLockImplicitDelay", 0));
        result.addProperty("pullSpringEnabled", booleanAsInt(settings, "pullSpringEnabled"));
        result.addProperty("pullSpringDuration", intValue(settings, "pullSpringDuration", 0));
        result.addProperty("autoPullSpringEnabled", booleanAsInt(settings, "autoPullSpringEnabled"));
        result.addProperty("postponedLockEnabled", booleanAsInt(settings, "postponedLockEnabled"));
        result.addProperty("postponedLockDelay", intValue(settings, "postponedLockDelay", 0));
        result.addProperty("buttonLockEnabled", booleanAsInt(settings, "buttonLockEnabled"));
        result.addProperty("buttonUnlockEnabled", booleanAsInt(settings, "buttonUnlockEnabled"));
        return result;
    }

    private static int cloudDoorState(JsonObject deviceState) {
        int value = intValue(deviceState, "doorState", 0);
        return switch (value) {
            case 3 -> 1;
            case 2 -> 2;
            default -> value;
        };
    }

    private static int booleanAsInt(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) {
            return 0;
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean() ? 1 : 0;
        }
        return value.getAsInt() != 0 ? 1 : 0;
    }

    private static int intValue(JsonObject object, String name, int defaultValue) {
        JsonElement value = object.get(name);
        return value != null && !value.isJsonNull() ? value.getAsInt() : defaultValue;
    }

    private static String stringValue(JsonObject object, String name, String defaultValue) {
        JsonElement value = object.get(name);
        return value != null && !value.isJsonNull() ? value.getAsString() : defaultValue;
    }

    private static String softwareVersion(JsonObject source) {
        JsonElement versions = source.get("softwareVersions");
        if (versions != null && versions.isJsonArray() && !versions.getAsJsonArray().isEmpty()) {
            JsonElement first = versions.getAsJsonArray().get(0);
            if (first.isJsonObject()) {
                JsonElement version = first.getAsJsonObject().get("version");
                if (version != null && !version.isJsonNull()) {
                    return version.getAsString();
                }
            }
        }
        return "";
    }

    private TedeeApiException error(ContentResponse response) {
        return new TedeeApiException("Tedee Cloud HTTP " + response.getStatus() + " " + response.getReason(),
                response.getStatus());
    }
}
