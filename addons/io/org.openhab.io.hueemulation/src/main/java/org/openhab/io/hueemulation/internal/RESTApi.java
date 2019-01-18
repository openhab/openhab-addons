/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueNewLights;
import org.openhab.io.hueemulation.internal.dto.HueStateChange;
import org.openhab.io.hueemulation.internal.dto.HueUnauthorizedConfig;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeRequest;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCreateUser;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse.HueErrorMessage;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessCreateGroup;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseCreateUser;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseStartSearchLights;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseStateChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

/**
 * Handles all REST API Requests
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RESTApi {
    public static final String PATH = "/api";
    private final Logger logger = LoggerFactory.getLogger(HueEmulationService.class);
    private final HueDataStore ds;
    private final Gson gson;
    private final UserManagement userManagement;
    private final ConfigManagement configManagement;
    private @NonNullByDefault({}) EventPublisher eventPublisher;

    public static enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    public RESTApi(HueDataStore ds, UserManagement userManagement, ConfigManagement configManagement, Gson gson) {
        this.ds = ds;
        this.userManagement = userManagement;
        this.configManagement = configManagement;
        this.gson = gson;
    }

    public void setEventPublisher(@Nullable EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cuts of the first part of a path and returns the remaining one.
     */
    private Path remaining(Path path) {
        if (path.getNameCount() > 1) {
            return path.subpath(1, path.getNameCount());
        } else {
            return Paths.get("/");
        }
    }

    /**
     * Handles /api and forwards any deeper path
     *
     * @param isDebug
     */
    @SuppressWarnings("null")
    public int handle(HttpMethod method, String body, Writer out, Path path, boolean isDebug)
            throws IOException, JsonParseException {
        if (!"api".equals(path.getName(0).toString())) {
            return 404;
        }

        if (path.getNameCount() == 1) { // request for API key
            if (method != HttpMethod.POST) {
                return 405;
            }
            if (!ds.config.linkbutton) {
                return 10403;
            }

            final HueCreateUser userRequest;
            userRequest = gson.fromJson(body, HueCreateUser.class);
            if (userRequest.devicetype == null || userRequest.devicetype.isEmpty()) {
                throw new JsonParseException("devicetype not given");
            }

            String apiKey = userRequest.username;
            if (apiKey == null || apiKey.length() == 0) {
                apiKey = UUID.randomUUID().toString();
            }
            userManagement.addUser(apiKey, userRequest.devicetype);

            try (JsonWriter writer = new JsonWriter(out)) {
                HueSuccessResponseCreateUser h = new HueSuccessResponseCreateUser(apiKey);
                gson.toJson(Collections.singleton(new HueResponse(h)), new TypeToken<List<?>>() {
                }.getType(), writer);
            }
            return 200;
        }

        updateDataStore();

        Path userPath = remaining(path);

        return handleUser(method, body, out, userPath.getName(0).toString(), remaining(userPath), path, isDebug);
    }

    /**
     * Handles /api/config and /api/{user-name} and forwards any deeper path
     */
    public int handleUser(HttpMethod method, String body, Writer out, String userName, Path remainingPath, Path fullURI,
            boolean isDebug) throws IOException, JsonParseException {

        if ("config".equals(userName)) { // Reduced config
            try (JsonWriter writer = new JsonWriter(out)) {
                gson.toJson(ds.config, new TypeToken<HueUnauthorizedConfig>() {
                }.getType(), writer);
            }
            return 200;
        }

        if (!userManagement.authorizeUser(userName)) {
            if (ds.config.linkbutton && ds.config.createNewUserOnEveryEndpoint) {
                userManagement.addUser(userName, "Formerly authorized device");
            } else {
                return 403;
            }
        }

        if (remainingPath.getNameCount() == 0) { /** /api/{username} */
            switch (method) {
                case GET:
                    out.write(gson.toJson(ds));
                    return 200;
                default:
                    return 405;
            }

        }

        String function = remainingPath.getName(0).toString();

        switch (function) {
            case "lights":
                return handleLights(method, body, out, remaining(remainingPath), fullURI, isDebug);
            case "groups":
                return handleGroups(method, body, out, remaining(remainingPath));
            case "config":
                return handleConfig(method, body, out, remaining(remainingPath), userName);
            default:
                return 404;
        }
    }

    /**
     * Handles /api/{user-name}/config and /api/{user-name}/config/whitelist
     * The own whitelisted user can remove itself with a DELETE
     */
    public int handleConfig(HttpMethod method, String body, Writer out, Path remainingPath, String authorizedUser)
            throws IOException, JsonParseException {
        if (remainingPath.getNameCount() == 0) {
            switch (method) {
                case GET:
                    out.write(gson.toJson(ds.config));
                    return 200;
                case PUT:
                    final HueChangeRequest changes;
                    changes = gson.fromJson(body, HueChangeRequest.class);
                    if (changes.devicename != null) {
                        ds.config.devicename = changes.devicename;
                    }
                    if (changes.dhcp != null) {
                        ds.config.dhcp = changes.dhcp;
                    }
                    if (changes.linkbutton != null) {
                        ds.config.linkbutton = changes.linkbutton;
                        configManagement.checkPairingTimeout();
                    }
                    configManagement.writeToFile();
                    return 200;
                default:
                    return 405;
            }
        } else if (remainingPath.getNameCount() >= 1 && "whitelist".equals(remainingPath.getName(0).toString())) {
            return handleConfigWhitelist(method, out, remaining(remainingPath), authorizedUser);
        } else {
            return 404;
        }
    }

    public int handleConfigWhitelist(HttpMethod method, Writer out, Path remainingPath, String authorizedUser)
            throws IOException {
        switch (remainingPath.getNameCount()) {
            case 0:
                switch (method) {
                    case GET:
                        out.write(gson.toJson(ds.config.whitelist));
                        return 200;
                    default:
                        return 405;
                }
            case 1:
                String username = remainingPath.getName(0).toString();
                switch (method) {
                    case GET:
                        ds.config.whitelist.get(username);
                        out.write(gson.toJson(ds.config.whitelist));
                        return 200;
                    case DELETE:
                        // Only own user can be removed
                        if (username.equals(authorizedUser)) {
                            userManagement.removeUser(authorizedUser);
                            return 200;
                        } else {
                            return 403;
                        }
                    default:
                        return 405;
                }
            default:
                return 405;
        }
    }

    @SuppressWarnings({ "null", "unused" })
    public int handleLights(HttpMethod method, String body, Writer out, Path remainingPath, Path fullURI,
            boolean isDebug) throws IOException, JsonParseException {
        /** /api/{username}/lights */
        if (remainingPath.getNameCount() == 0) {
            switch (method) {
                case GET:
                    if (isDebug) {
                        out.write("Exposed lights:\n\n");
                        for (HueDevice hueDevice : ds.lights.values()) {
                            out.write(hueDevice.toString());
                            out.write("\n");
                        }
                    } else {
                        ds.lights.values().forEach(v -> v.updateState());
                        out.write(gson.toJson(ds.lights));
                    }
                    return 200;
                case POST:
                    try (JsonWriter writer = new JsonWriter(out)) {
                        List<HueResponse> responses = new ArrayList<>();
                        responses.add(new HueResponse(new HueSuccessResponseStartSearchLights()));
                        gson.toJson(responses, new TypeToken<List<?>>() {
                        }.getType(), writer);
                    }
                    return 200;
                default:
                    return 405;
            }
        }

        String id = remainingPath.getName(0).toString();

        /** /api/{username}/lights/new */
        if ("new".equals(id)) {
            switch (method) {
                case GET:
                    out.write(gson.toJson(new HueNewLights()));
                    return 200;
                default:
                    return 405;
            }
        }

        final int hueID;
        try {
            hueID = new Integer(id);
        } catch (NumberFormatException e) {
            return 404;
        }

        HueDevice hueDevice = ds.lights.get(hueID);
        if (hueDevice == null) {
            return 404;
        }

        /** /api/{username}/lights/{id} */
        if (remainingPath.getNameCount() == 1) {
            hueDevice.updateState();
            out.write(gson.toJson(hueDevice));
            return 200;
        }

        if (remainingPath.getNameCount() == 2) {
            switch (method) {
                case PUT:
                    return handleLightChangeState(fullURI, method, body, out, hueID, hueDevice);
                default:
                    return 405;
            }
        }

        return 404;
    }

    @SuppressWarnings({ "null", "unused" })
    public int handleGroups(HttpMethod method, String body, Writer out, Path remainingPath) throws IOException {
        /** /api/{username}/groups */
        if (remainingPath.getNameCount() == 0) {
            switch (method) {
                case GET:
                    out.write(gson.toJson(ds.groups));
                    return 200;
                case POST:
                    int hueid = ds.generateNextGroupHueID();
                    try (JsonWriter writer = new JsonWriter(out)) {
                        List<HueResponse> responses = new ArrayList<>();
                        responses.add(new HueResponse(new HueSuccessCreateGroup(hueid)));
                        gson.toJson(responses, new TypeToken<List<?>>() {
                        }.getType(), writer);
                    }
                    return 200;
                default:
                    return 405;
            }
        }

        String id = remainingPath.getName(0).toString();

        final int hueID;
        try {
            hueID = new Integer(id);
        } catch (NumberFormatException e) {
            return 404;
        }

        /** /api/{username}/groups/{id} */
        if (remainingPath.getNameCount() == 1) {
            Object value = ds.groups.get(hueID);
            if (value == null) {
                return 404;
            } else {
                out.write(gson.toJson(value));
                return 200;
            }
        }
        return 404;
    }

    /**
     * Hue API call to set the state of a light.
     * Enpoint: /api/{username}/lights/{id}/state
     */
    @SuppressWarnings({ "null", "unused" })
    private int handleLightChangeState(Path fullURI, HttpMethod method, String body, Writer out, int hueID,
            HueDevice hueDevice) throws IOException, JsonParseException {
        HueStateChange state = gson.fromJson(body, HueStateChange.class);
        if (state == null) {
            throw new JsonParseException("No state change data received!");
        }

        // logger.debug("Received state change: {}", gson.toJson(state));

        // Apply new state and collect success, error items
        Map<String, Object> successApplied = new TreeMap<>();
        List<String> errorApplied = new ArrayList<>();
        Command command = hueDevice.applyState(state, successApplied, errorApplied);

        // If a command could be created, post it to the framework now
        if (command != null) {
            logger.debug("sending {} to {}", command, hueDevice.item.getName());
            eventPublisher.post(ItemEventFactory.createCommandEvent(hueDevice.item.getName(), command, "hueemulation"));
        }

        // Generate the response. The response consists of a list with an entry each for all
        // submitted change requests. If for example "on" and "bri" was send, 2 entries in the response are
        // expected.
        Path contextPath = fullURI.subpath(2, fullURI.getNameCount() - 1);
        List<HueResponse> responses = new ArrayList<>();
        successApplied.forEach((t, v) -> {
            responses.add(new HueResponse(new HueSuccessResponseStateChanged(contextPath.resolve(t).toString(), v)));
        });
        errorApplied.forEach(v -> {
            responses.add(new HueResponse(new HueErrorMessage(HueResponse.NOT_AVAILABLE,
                    contextPath.resolve(v).toString(), "Could not set")));
        });

        try (JsonWriter writer = new JsonWriter(out)) {
            gson.toJson(responses, new TypeToken<List<?>>() {
            }.getType(), writer);
        }
        return 200;
    }

    /**
     * Update changing parameters of the data store like the time.
     */
    public void updateDataStore() {
        ds.config.UTC = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ds.config.localtime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
