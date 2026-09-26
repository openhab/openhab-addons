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

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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

    private final Logger logger = LoggerFactory.getLogger(TedeeWebhookServlet.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public TedeeWebhookServlet(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getContentLengthLong() > MAX_PAYLOAD_SIZE) {
            logger.warn("Tedee webhook payload is too large");
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        String body = readBody(request);
        if (body.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty request body");
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();

            String event = root.get("event").getAsString();
            if (!isSupportedEvent(event)) {
                logger.debug("Ignoring unsupported Tedee webhook event '{}'", event);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            JsonObject data = root.getAsJsonObject("data");
            int deviceType = data.get("deviceType").getAsInt();
            int deviceId = data.get("deviceId").getAsInt();

            if (deviceType != 2) {
                logger.debug("Ignoring Tedee webhook for unsupported device type {} and device {}", deviceType,
                        deviceId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            TedeeLockHandler handler = findLockHandler(deviceId);
            if (handler == null) {
                logger.debug("No Tedee lock handler found for device {}", deviceId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            switch (event) {
                case "lock-status-changed" -> {
                    int state = data.get("state").getAsInt();
                    int jammed = data.get("jammed").getAsInt();
                    int doorState = data.get("doorState").getAsInt();

                    logger.debug("Processing lock-status webhook for lock {}: state={}, doorState={}, jammed={}",
                            deviceId, state, doorState, jammed);
                    handler.handleWebhook(state, doorState, jammed);
                }
                case "device-connection-changed" -> {
                    int isConnected = data.get("isConnected").getAsInt();

                    logger.debug("Processing connection webhook for lock {}: isConnected={}", deviceId, isConnected);
                    handler.handleConnectionWebhook(isConnected);
                }
                case "device-battery-level-changed" -> {
                    int batteryLevel = data.get("batteryLevel").getAsInt();

                    logger.debug("Processing battery webhook for lock {}: batteryLevel={}", deviceId, batteryLevel);
                    handler.handleBatteryWebhook(batteryLevel);
                }
                case "device-battery-start-charging" -> {
                    logger.debug("Processing battery start-charging webhook for lock {}", deviceId);
                    handler.handleChargingWebhook(true);
                }
                case "device-battery-stop-charging" -> {
                    logger.debug("Processing battery stop-charging webhook for lock {}", deviceId);
                    handler.handleChargingWebhook(false);
                }
                case "device-battery-fully-charged" -> {
                    logger.debug("Processing battery fully-charged webhook for lock {}", deviceId);
                    handler.handleChargingWebhook(false);
                }
                case "device-settings-changed" -> {
                    logger.debug("Processing settings-changed webhook for lock {}", deviceId);
                    handler.handleSettingsWebhook();
                }
                default -> throw new IllegalStateException("Unsupported Tedee webhook event: " + event);
            }

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Invalid Tedee webhook payload");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
        }
    }

    private static boolean isSupportedEvent(String event) {
        return event != null && switch (event) {
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
                throw new IOException("Tedee webhook payload too large");
            }
            body.append(buffer, 0, read);
        }

        return body.toString();
    }

    private @Nullable TedeeLockHandler findLockHandler(int deviceId) {
        for (Thing thing : thingRegistry.getAll()) {
            if (!TedeeBindingConstants.LOCK.equals(thing.getThingTypeUID())) {
                continue;
            }

            Object configuredId = thing.getConfiguration().get("deviceId");
            if (!(configuredId instanceof Number number) || number.intValue() != deviceId) {
                continue;
            }

            if (thing.getHandler() instanceof TedeeLockHandler handler) {
                return handler;
            }
        }

        return null;
    }
}
