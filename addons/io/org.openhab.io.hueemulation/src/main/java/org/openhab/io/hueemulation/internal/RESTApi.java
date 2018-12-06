/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import javax.servlet.http.HttpServletRequest;

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
     */
    @SuppressWarnings("null")
    public int handle(HttpServletRequest req, Writer out, Path path) throws IOException {
        if (!"api".equals(path.getName(0).toString())) {
            return 404;
        }

        if (path.getNameCount() == 1) { // request for API key
            if (!"POST".equals(req.getMethod())) {
                return 405;
            }
            if (ds.config.linkbutton) {
                final HueCreateUser userRequest;
                try {
                    userRequest = gson.fromJson(req.getReader(), HueCreateUser.class);
                } catch (JsonParseException e) {
                    return 400;
                }
                if (userRequest == null || userRequest.devicetype == null || userRequest.devicetype.isEmpty()) {
                    return 400;
                }

                String apiKey = userRequest.username;
                if (apiKey == null || apiKey.length() == 0) {
                    apiKey = UUID.randomUUID().toString();
                }
                userManagement.addUser(apiKey, userRequest.devicetype);

                try (JsonWriter writer = new JsonWriter(out)) {
                    HueSuccessResponseCreateUser h = new HueSuccessResponseCreateUser(userRequest.username);
                    gson.toJson(Collections.singleton(new HueResponse(h)), new TypeToken<List<?>>() {
                    }.getType(), writer);
                }
                return 200;
            } else {
                return 10403;
            }
        }

        updateDataStore();

        Path userPath = remaining(path);

        return handleUser(req, out, userPath.getName(0).toString(), remaining(userPath));
    }

    /**
     * Handles /api/config and /api/{user-name} and forwards any deeper path
     */
    public int handleUser(HttpServletRequest req, Writer out, String userName, Path remainingPath) throws IOException {

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
            if (req.getMethod().equals("GET")) {
                out.write(gson.toJson(ds));
                return 200;
            } else {
                return 405;
            }

        }

        String function = remainingPath.getName(0).toString();

        switch (function) {
            case "lights":
                return handleLights(req, out, remaining(remainingPath));
            case "groups":
                return handleGroups(req, out, remaining(remainingPath));
            case "config":
                return handleConfig(req, out, remaining(remainingPath), userName);
            default:
                return 404;
        }
    }

    /**
     * Handles /api/{user-name}/config and /api/{user-name}/config/whitelist
     * The own whitelisted user can remove itself with a DELETE
     */
    public int handleConfig(HttpServletRequest req, Writer out, Path remainingPath, String authorizedUser)
            throws IOException {
        if (remainingPath.getNameCount() == 0) {
            if (req.getMethod().equals("GET")) {
                out.write(gson.toJson(ds.config));
                return 200;
            } else if (req.getMethod().equals("PUT")) {
                final HueChangeRequest changes;
                try {
                    changes = gson.fromJson(req.getReader(), HueChangeRequest.class);
                } catch (com.google.gson.JsonParseException e) {
                    return 400;
                }
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
            } else {
                return 405;
            }
        } else if (remainingPath.getNameCount() >= 1 && "whitelist".equals(remainingPath.getName(0).toString())) {
            return handleConfigWhitelist(req, out, remaining(remainingPath), authorizedUser);
        } else {
            return 404;
        }
    }

    public int handleConfigWhitelist(HttpServletRequest req, Writer out, Path remainingPath, String authorizedUser)
            throws IOException {
        if (remainingPath.getNameCount() == 0) {
            if (req.getMethod().equals("GET")) {
                out.write(gson.toJson(ds.config.whitelist));
                return 200;
            } else {
                return 405;
            }
        } else if (remainingPath.getNameCount() == 1) {
            String username = remainingPath.getName(0).toString();
            if (req.getMethod().equals("GET")) {
                ds.config.whitelist.get(username);
                out.write(gson.toJson(ds.config.whitelist));
                return 200;
            } else if (req.getMethod().equals("DELETE")) {
                // Only own user can be removed
                if (username.equals(authorizedUser)) {
                    userManagement.removeUser(authorizedUser);
                    return 200;
                } else {
                    return 403;
                }
            } else {
                return 405;
            }
        } else {
            return 405;
        }
    }

    @SuppressWarnings({ "null", "unused" })
    public int handleLights(HttpServletRequest req, Writer out, Path remainingPath) throws IOException {
        /** /api/{username}/lights */
        if (remainingPath.getNameCount() == 0) {
            if (req.getMethod().equals("GET")) { // Return complete object
                out.write(gson.toJson(ds.lights));
                return 200;
            } else if (req.getMethod().equals("POST")) { // Starts a search for new lights
                try (JsonWriter writer = new JsonWriter(out)) {
                    List<HueResponse> responses = new ArrayList<>();
                    responses.add(new HueResponse(new HueSuccessResponseStartSearchLights()));
                    gson.toJson(responses, new TypeToken<List<?>>() {
                    }.getType(), writer);
                }
                return 200;
            } else {
                return 405;
            }
        }

        String id = remainingPath.getName(0).toString();

        /** /api/{username}/lights/new */
        if ("new".equals(id)) {
            if (req.getMethod().equals("GET")) {
                out.write(gson.toJson(new HueNewLights()));
                return 200;
            } else {
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
            out.write(gson.toJson(hueDevice));
            return 200;
        }

        if (remainingPath.getNameCount() == 2) {
            // Only lights allowed for /state so far
            if (req.getMethod().equals("PUT")) {
                return handleLightChangeState(req, out, hueID, hueDevice);
            } else {
                return 405;
            }
        }

        return 404;
    }

    @SuppressWarnings({ "null", "unused" })
    public int handleGroups(HttpServletRequest req, Writer out, Path remainingPath) throws IOException {
        /** /api/{username}/groups */
        if (remainingPath.getNameCount() == 0) {
            if (req.getMethod().equals("GET")) { // Return complete object
                out.write(gson.toJson(ds.groups));
                return 200;
            } else {
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
    private int handleLightChangeState(HttpServletRequest req, Writer out, int hueID, HueDevice hueDevice)
            throws IOException {
        HueStateChange state;
        try {
            state = gson.fromJson(req.getReader(), HueStateChange.class);
        } catch (com.google.gson.JsonParseException e) {
            return 400;
        }
        if (state == null) {
            return 400;
        }

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
        final Path path = Paths.get(req.getRequestURI());
        Path contextPath = path.subpath(2, path.getNameCount() - 1);
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
