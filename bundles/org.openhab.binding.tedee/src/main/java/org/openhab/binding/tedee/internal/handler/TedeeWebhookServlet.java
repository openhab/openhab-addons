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

package org.openhab.binding.tedee.internal.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Receives webhook events sent by the Tedee Bridge.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
@Component(service = Servlet.class, property = {
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/tedee/webhook" })
public class TedeeWebhookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_PAYLOAD_SIZE = 4096;
    private static final int LOCK_DEVICE_TYPE = 2;

    private static final class PayloadTooLargeException extends IOException {
        private static final long serialVersionUID = 1L;

        PayloadTooLargeException() {
            super("Tedee webhook payload too large");
        }
    }

    private final Logger logger = LoggerFactory.getLogger(TedeeWebhookServlet.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public TedeeWebhookServlet(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    @NonNullByDefault({})
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getContentLengthLong() > MAX_PAYLOAD_SIZE) {
            logger.warn("Tedee webhook payload is too large");
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        final String body;
        try {
            body = readBody(request);
        } catch (PayloadTooLargeException e) {
            logger.warn("Tedee webhook payload is too large");
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Webhook payload too large");
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();

            String event = requiredString(root, "event");
            if (!isSupportedEvent(event)) {
                logger.debug("Ignoring unsupported Tedee webhook event '{}'", event);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            JsonObject data = requiredObject(root, "data");
            int deviceType = requiredInt(data, "deviceType");
            int deviceId = requiredInt(data, "deviceId");

            if (deviceType != LOCK_DEVICE_TYPE) {
                logger.debug("Ignoring Tedee webhook for unsupported device type {} and device {}", deviceType,
                        deviceId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            List<TedeeLockHandler> handlers = findAuthorizedLockHandlers(deviceId,
                    request.getHeader(TedeeBindingConstants.WEBHOOK_SECRET_HEADER));
            if (handlers.isEmpty()) {
                logger.debug("Rejected Tedee webhook for device {} because no authorized local handler was found",
                        deviceId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid webhook secret");
                return;
            }

            for (TedeeLockHandler handler : handlers) {
                processEvent(handler, event, data, deviceId);
            }

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Invalid Tedee webhook payload");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
        }
    }

    private void processEvent(TedeeLockHandler handler, String event, JsonObject data, int deviceId) {
        switch (event) {
            case "lock-status-changed" -> {
                int state = requiredInt(data, "state");
                int jammed = requiredInt(data, "jammed");
                int doorState = requiredInt(data, "doorState");

                logger.debug("Processing lock-status webhook for lock {}: state={}, doorState={}, jammed={}", deviceId,
                        state, doorState, jammed);
                handler.handleWebhook(state, doorState, jammed);
            }
            case "device-connection-changed" -> {
                int isConnected = requiredInt(data, "isConnected");

                logger.debug("Processing connection webhook for lock {}: isConnected={}", deviceId, isConnected);
                handler.handleConnectionWebhook(isConnected);
            }
            case "device-battery-level-changed" -> {
                int batteryLevel = requiredInt(data, "batteryLevel");

                logger.debug("Processing battery webhook for lock {}: batteryLevel={}", deviceId, batteryLevel);
                handler.handleBatteryWebhook(batteryLevel);
            }
            case "device-battery-start-charging" -> {
                logger.debug("Processing battery start-charging webhook for lock {}", deviceId);
                handler.handleChargingWebhook(true);
            }
            case "device-battery-stop-charging", "device-battery-fully-charged" -> {
                logger.debug("Processing battery charging-ended webhook '{}' for lock {}", event, deviceId);
                handler.handleChargingWebhook(false);
            }
            case "device-settings-changed" -> {
                logger.debug("Processing settings-changed webhook for lock {}", deviceId);
                handler.handleSettingsWebhook();
            }
            default -> throw new IllegalStateException("Unsupported Tedee webhook event: " + event);
        }
    }

    private static boolean isSupportedEvent(String event) {
        return switch (event) {
            case "lock-status-changed", "device-connection-changed", "device-battery-level-changed",
                    "device-battery-start-charging", "device-battery-stop-charging", "device-battery-fully-charged",
                    "device-settings-changed" ->
                true;
            default -> false;
        };
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int total = 0;

        var reader = request.getReader();

        int read;
        while ((read = reader.read(buffer)) != -1) {
            total += read;
            if (total > MAX_PAYLOAD_SIZE) {
                throw new PayloadTooLargeException();
            }
            body.append(buffer, 0, read);
        }

        return body.toString();
    }

    private static JsonObject requiredObject(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonObject()) {
            throw new IllegalStateException("Missing or invalid webhook object: " + name);
        }
        return value.getAsJsonObject();
    }

    private static String requiredString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive()) {
            throw new IllegalStateException("Missing or invalid webhook string: " + name);
        }

        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (!primitive.isString()) {
            throw new IllegalStateException("Missing or invalid webhook string: " + name);
        }
        return primitive.getAsString();
    }

    private static int requiredInt(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalStateException("Missing or invalid webhook number: " + name);
        }

        try {
            return value.getAsInt();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid webhook number: " + name, e);
        }
    }

    private List<TedeeLockHandler> findAuthorizedLockHandlers(int deviceId, @Nullable String webhookSecret) {
        List<TedeeLockHandler> handlers = new ArrayList<>();

        for (Thing thing : thingRegistry.getAll()) {
            if (!TedeeBindingConstants.LOCK.equals(thing.getThingTypeUID())) {
                continue;
            }

            ThingUID bridgeUID = thing.getBridgeUID();
            Thing bridgeThing = bridgeUID == null ? null : thingRegistry.get(bridgeUID);
            Bridge bridge = bridgeThing instanceof Bridge matchingBridge ? matchingBridge : null;
            if (!(bridge != null && bridge.getHandler() instanceof TedeeBridgeHandler bridgeHandler
                    && bridgeHandler.isWebhookSecret(webhookSecret))) {
                continue;
            }

            Object configuredId = thing.getConfiguration().get("deviceId");
            if (!(configuredId instanceof Number number) || number.intValue() != deviceId) {
                continue;
            }

            if (thing.getHandler() instanceof TedeeLockHandler handler) {
                handlers.add(handler);
            }
        }

        return handlers;
    }
}
