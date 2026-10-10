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
package org.openhab.binding.veluxactive.internal;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.api.dto.ApiResponse;
import org.openhab.binding.veluxactive.internal.api.dto.Home;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.binding.veluxactive.internal.api.dto.ModuleError;
import org.openhab.binding.veluxactive.internal.api.dto.Room;
import org.openhab.binding.veluxactive.internal.api.dto.Topology;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.binding.veluxactive.internal.exception.ApiUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Authenticated VELUX topology, home status and departure commands.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveApi {
    private static final String BASE_URL = "https://app.velux-active.com";
    private static final String HOMES_DATA_PATH = "/api/homesdata";
    private static final String HOME_STATUS_PATH = "/api/homestatus";
    private static final String SET_STATE_PATH = "/syncapi/v1/setstate";
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(VeluxActiveApi.class);
    private final HttpClient httpClient;
    private volatile @Nullable ServerTime serverTime;

    private record ServerTime(long seconds, long receivedNanos) {
    }

    public long signingTime() throws ApiException {
        ServerTime sample = serverTime;
        if (sample == null || sample.seconds() <= 0
                || System.nanoTime() - sample.receivedNanos() > TimeUnit.MINUTES.toNanos(5)) {
            throw new ApiException("Server time is unavailable for signing.");
        }
        return sample.seconds() + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - sample.receivedNanos());
    }

    public VeluxActiveApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<ModuleInfo> modules(String token) throws ApiException, InterruptedException {
        JsonObject body = requireBody(request(token, HOMES_DATA_PATH, "", false));
        try {
            Topology topology = GSON.fromJson(body, Topology.class);
            List<@Nullable Home> homes = topology == null ? null : topology.homes;
            if (homes == null) {
                throw new ApiException("Home topology is missing.");
            }
            List<ModuleInfo> modules = new ArrayList<>();
            for (Home home : homes) {
                String homeId = home == null ? null : home.id;
                List<@Nullable Module> homeModules = home == null ? null : home.modules;
                if (home == null || homeId == null || homeId.isBlank() || homeModules == null) {
                    continue;
                }
                for (Module module : homeModules) {
                    String moduleId = module == null ? null : module.id;
                    ModuleType type = module == null ? null : module.type;
                    if (module != null && type != null && moduleId != null && !moduleId.isBlank()) {
                        String name = module.name;
                        Room room = findRoom(home, module);
                        String bridgeId = findGateway(home, module);
                        String roomId = module.roomId;
                        if ((roomId == null || roomId.isBlank()) && room != null) {
                            roomId = room.id;
                        }
                        modules.add(new ModuleInfo(homeId, moduleId,
                                name == null || name.isBlank() ? type.defaultName() : name, type, roomId,
                                room == null ? null : room.name, bridgeId));
                    }
                }
            }
            return List.copyOf(modules);
        } catch (JsonParseException | IllegalStateException e) {
            throw new ApiException("Invalid home topology.");
        }
    }

    public HomeStatus homeStatus(String token, String homeId) throws ApiException, InterruptedException {
        String form = "home_id=" + URLEncoder.encode(homeId, StandardCharsets.UTF_8);
        JsonObject body = requireBody(request(token, HOME_STATUS_PATH, form, false));
        try {
            HomeStatus status = GSON.fromJson(body, HomeStatus.class);
            Home home = status == null ? null : status.home;
            List<@Nullable ModuleError> errors = status == null ? null : status.errors;
            if (status == null || home != null && !homeId.equals(home.id)
                    || home == null && (errors == null || errors.isEmpty())) {
                throw new ApiException("Home status is missing or belongs to another home.");
            }
            return status;
        } catch (JsonParseException | IllegalStateException e) {
            throw new ApiException("Invalid home status.", e);
        }
    }

    public void lock(String token, String homeId, String moduleId, String timezone)
            throws ApiException, InterruptedException {
        setState(token, homeId, timezone, Map.of("id", moduleId, "scenario", "away"));
    }

    public void setPosition(String token, String homeId, String moduleId, String gatewayId, int position,
            String timezone) throws ApiException, InterruptedException {
        if (position < 0 || position > 100) {
            throw new IllegalArgumentException("Position must be between 0 and 100.");
        }
        setState(token, homeId, timezone, Map.of("id", moduleId, "bridge", gatewayId, "target_position", position));
    }

    public void retrieveKey(String token, String homeId, String gatewayId, String timezone)
            throws ApiException, InterruptedException {
        setState(token, homeId, timezone, Map.of("id", gatewayId, "retrieve_key", true));
    }

    public void unlock(String token, String homeId, String gatewayId, String timezone, Map<String, Object> signature)
            throws ApiException, InterruptedException {
        Map<String, Object> module = new HashMap<>(signature);
        module.put("id", gatewayId);
        module.put("scenario", "home");
        setState(token, homeId, timezone, module);
    }

    public void setSignedPosition(String token, String homeId, String moduleId, String gatewayId, int position,
            String timezone, Map<String, Object> signature) throws ApiException, InterruptedException {
        if (position < 0 || position > 100) {
            throw new IllegalArgumentException("Position must be between 0 and 100.");
        }
        Map<String, Object> module = new HashMap<>(signature);
        module.put("id", moduleId);
        module.put("bridge", gatewayId);
        module.put("target_position", position);
        setState(token, homeId, timezone, module);
    }

    public void stopMovements(String token, String homeId, String gatewayId, String timezone)
            throws ApiException, InterruptedException {
        setState(token, homeId, timezone, Map.of("id", gatewayId, "stop_movements", "all"));
    }

    private void setState(String token, String homeId, String timezone, Map<String, Object> module)
            throws ApiException, InterruptedException {
        String payload = GSON.toJson(Map.of("app_type", "app_velux", "app_version", APP_VERSION, "home",
                Map.of("id", homeId, "timezone", timezone, "modules", List.of(module))));
        JsonElement body = request(token, SET_STATE_PATH, payload, true).body;
        JsonElement errors = body == null ? null : body.getAsJsonObject().get("errors");
        if (errors != null && !errors.isJsonNull() && (!errors.isJsonArray() || !errors.getAsJsonArray().isEmpty())) {
            throw new ApiException("The gateway rejected the command.");
        }
    }

    private ApiResponse request(String token, String path, String payload, boolean json)
            throws ApiException, InterruptedException {
        Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.POST)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token).followRedirects(false)
                .timeout(10, TimeUnit.SECONDS)
                .content(new StringContentProvider(json ? "application/json" : "application/x-www-form-urlencoded",
                        payload, StandardCharsets.UTF_8));
        ContentResponse response;
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("POST request for {}: {}", request.getURI(), payload);
            }
            response = request.send();
        } catch (ExecutionException | TimeoutException e) {
            throw new ApiException("Could not communicate with VELUX ACTIVE.");
        }
        String content = response.getContentAsString();
        if (logger.isTraceEnabled()) {
            logger.trace("POST response for {}: HTTP {}: {}", request.getURI(), response.getStatus(), content);
        }
        ApiResponse result = parseResponse(response.getStatus(), content);
        Long seconds = result.timeServer;
        if (seconds != null && seconds > 0) {
            serverTime = new ServerTime(seconds, System.nanoTime());
        }
        return result;
    }

    static ApiResponse parseResponse(int httpStatus, String content) throws ApiException {
        if (httpStatus != HttpStatus.OK_200) {
            if (httpStatus == HttpStatus.UNAUTHORIZED_401) {
                throw new ApiUnauthorizedException("VELUX ACTIVE rejected the authentication token.");
            }
            throw new ApiException("VELUX ACTIVE returned HTTP " + httpStatus + ".");
        }
        try {
            ApiResponse result = GSON.fromJson(content, ApiResponse.class);
            if (result == null) {
                throw new ApiException("VELUX ACTIVE returned an empty response.");
            }
            JsonElement error = result.error;
            if (error != null && !error.isJsonNull()) {
                JsonElement code = error.isJsonObject() ? error.getAsJsonObject().get("code") : null;
                if (code != null && code.isJsonPrimitive() && "2".equals(code.getAsString())) {
                    throw new ApiUnauthorizedException("VELUX ACTIVE rejected the authentication token.");
                }
                throw new ApiException("VELUX ACTIVE returned an API error.");
            }
            if (!"ok".equals(result.status)) {
                throw new ApiException("VELUX ACTIVE did not confirm the request.");
            }
            JsonElement body = result.body;
            if (body != null) {
                if (body.isJsonNull()) {
                    result.body = null;
                } else if (!body.isJsonObject()) {
                    throw new ApiException("VELUX ACTIVE returned an invalid response body.");
                }
            }
            return result;
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException e) {
            throw new ApiException("VELUX ACTIVE returned invalid JSON.");
        }
    }

    private static JsonObject requireBody(ApiResponse response) throws ApiException {
        JsonElement body = response.body;
        if (body == null) {
            throw new ApiException("VELUX ACTIVE returned no response body.");
        }
        return body.getAsJsonObject();
    }

    private static @Nullable String findGateway(Home home, Module module) {
        String bridge = module.bridge;
        if (bridge != null && !bridge.isBlank()) {
            return bridge;
        }
        List<@Nullable Module> modules = home.modules;
        String match = null;
        if (modules != null) {
            for (Module candidate : modules) {
                if (candidate == null || candidate.type != ModuleType.GATEWAY) {
                    continue;
                }
                List<String> bridged = candidate.modulesBridged;
                String id = candidate.id;
                if (id != null && !id.isBlank() && bridged != null && bridged.contains(module.id)) {
                    if (match != null) {
                        return null;
                    }
                    match = id;
                }
            }
        }
        return match;
    }

    private static @Nullable Room findRoom(Home home, Module module) {
        List<@Nullable Room> rooms = home.rooms;
        if (rooms == null) {
            return null;
        }
        String roomId = module.roomId;
        Room match = null;
        for (Room room : rooms) {
            if (room == null) {
                continue;
            }
            if (roomId != null && !roomId.isBlank()) {
                if (roomId.equals(room.id)) {
                    return room;
                }
            } else {
                List<String> ids = room.moduleIds;
                if (ids == null) {
                    ids = room.modules;
                }
                if (ids != null && ids.contains(module.id)) {
                    if (match != null) {
                        return null;
                    }
                    match = room;
                }
            }
        }
        return match;
    }
}
