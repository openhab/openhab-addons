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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * {@link RachioWebhookServlet} implements the callback for the Rachio Cloud event API.
 *
 * @author Markus Michels - Initial contribution
 */
@Component(service = {}, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@NonNullByDefault
public class RachioWebhookServlet extends HttpServlet {
    private static final long serialVersionUID = -4654253998990066051L;
    private static final String WEBHOOK_SIGNATURE_HEADER = "x-signature";
    private static final int MAX_WEBHOOK_PAYLOAD_BYTES = 256 * 1024;
    private final Logger logger = LoggerFactory.getLogger(RachioWebhookServlet.class);
    private final Gson gson = new Gson();
    private final RachioWebhookDuplicateEventCache duplicateEventCache = new RachioWebhookDuplicateEventCache();

    private final RachioHandlerFactory rachioHandlerFactory;
    private final Object registrationLock = new Object();
    private @Nullable HttpService httpService;
    private boolean servletRegistered;

    /**
     * OSGi activation callback.
     */
    @Activate
    public RachioWebhookServlet(@Reference RachioHandlerFactory rachioHandlerFactory) {
        this.rachioHandlerFactory = rachioHandlerFactory;
    }

    /**
     * OSGi HttpService bind callback.
     *
     * @param httpService the HTTP service used for manual servlet registration
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    protected void bindHttpService(HttpService httpService) {
        synchronized (registrationLock) {
            if (Objects.equals(this.httpService, httpService) && servletRegistered) {
                logger.debug("RachioWebhook: Webhook servlet already registered at {}, skipping duplicate bind",
                        SERVLET_WEBHOOK_PATH);
                return;
            }
            if (servletRegistered) {
                unregisterServletLocked();
            }

            this.httpService = httpService;
            registerServletLocked(httpService);
        }
    }

    protected void unbindHttpService(HttpService httpService) {
        synchronized (registrationLock) {
            if (!Objects.equals(this.httpService, httpService)) {
                logger.debug("RachioWebhook: Ignoring HttpService unbind for non-current service");
                return;
            }

            unregisterServletLocked();
            this.httpService = null;
        }
    }

    /**
     * OSGi deactivation callback.
     */
    @Deactivate
    protected void deactivate() {
        synchronized (registrationLock) {
            unregisterServletLocked();
            httpService = null;
        }
    }

    private void registerServletLocked(HttpService httpService) {
        try {
            logger.debug("RachioWebhook: Registering webhook servlet alias {}", SERVLET_WEBHOOK_PATH);
            httpService.registerServlet(SERVLET_WEBHOOK_PATH, this, null, httpService.createDefaultHttpContext());
            servletRegistered = true;
        } catch (ServletException | NamespaceException e) {
            servletRegistered = false;
            logger.warn("RachioWebhook: Could not register webhook servlet alias {}: {}", SERVLET_WEBHOOK_PATH,
                    e.getMessage());
        }
    }

    private void unregisterServletLocked() {
        HttpService currentHttpService = httpService;
        if (!servletRegistered || currentHttpService == null) {
            return;
        }

        try {
            logger.debug("RachioWebhook: Unregistering webhook servlet alias {}", SERVLET_WEBHOOK_PATH);
            currentHttpService.unregister(SERVLET_WEBHOOK_PATH);
        } catch (IllegalArgumentException e) {
            logger.debug("RachioWebhook: Webhook servlet alias {} was already unregistered", SERVLET_WEBHOOK_PATH);
        } finally {
            servletRegistered = false;
        }
    }

    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (request == null || resp == null) {
            return;
        }

        setHeaders(resp);

        String ipAddress = getClientIpAddress(request);
        String path = request.getRequestURI();
        logger.trace("RachioWebhook: Request from {}:{}{} ({}:{}, {})", ipAddress, request.getRemotePort(), path,
                request.getRemoteHost(), request.getServerPort(), request.getProtocol());

        if (!SERVLET_WEBHOOK_PATH.equalsIgnoreCase(path)) {
            logger.debug("RachioWebhook: Invalid request received - path = {}", path);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            logger.debug("RachioWebhook: Invalid request received - method = {}", request.getMethod());
            resp.setHeader("Allow", "POST, OPTIONS");
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        byte[] rawBody = request.getInputStream().readNBytes(MAX_WEBHOOK_PAYLOAD_BYTES + 1);
        if (rawBody.length > MAX_WEBHOOK_PAYLOAD_BYTES) {
            logger.warn("RachioWebhook: Rejecting webhook request from {} because payload size exceeds limit {} bytes",
                    ipAddress, MAX_WEBHOOK_PAYLOAD_BYTES);
            resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        String data = new String(rawBody, StandardCharsets.UTF_8);
        RachioEventGsonDTO event = null;
        try {
            logger.trace("RachioWebhook: Received {} byte webhook payload", rawBody.length);
            event = parseEvent(data);
            String signature = request.getHeader(WEBHOOK_SIGNATURE_HEADER);
            boolean modernWebhookShape = signature != null || event.hasStrongModernWebhookMarkers();
            if (!modernWebhookShape && event.isLegacyNotificationEvent()) {
                event.normalizeLegacyNotificationEvent();
                logger.trace("RachioWebhook: Processing legacy NotificationService event ({})", describeEvent(event));
                if (isBlank(event.subType)) {
                    logger.debug(
                            "RachioWebhook: Legacy NotificationService event has no subtype; processing recognized type '{}'",
                            event.type);
                }
                if (!rachioHandlerFactory.legacyWebHookEvent(ipAddress, event)) {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("");
                return;
            }

            if (signature == null || signature.isBlank()) {
                logger.warn("RachioWebhook: Payload classification summary: {}", describeLegacyClassification(event));
                logger.warn(
                        "RachioWebhook: Rejecting webhook request from {} because the x-signature header is missing",
                        ipAddress);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!rachioHandlerFactory.isValidWebHookSignature(signature, rawBody, event)) {
                logger.warn("RachioWebhook: Rejecting webhook request from {} because signature validation failed",
                        ipAddress);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            event.normalize();
            logger.trace("RachioEvent {}.{} for device '{}': {}", event.category, event.type, event.deviceId,
                    event.summary);

            event.apiResult.setRateLimit(request.getHeader(RACHIO_JSON_RATE_LIMIT),
                    request.getHeader(RACHIO_JSON_RATE_REMAINING), request.getHeader(RACHIO_JSON_RATE_RESET));

            if (isDuplicateEvent(event)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("");
                return;
            }

            logger.trace("RachioWebhook: Processing validated webhook event ({})", describeEvent(event));
            if (rachioHandlerFactory.webHookEvent(ipAddress, event)) {
                markEventProcessed(event);
            } else {
                logger.debug(
                        "RachioWebhook: Unable to route validated webhook event; acknowledging without processing ({})",
                        describeEvent(event));
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("");
        } catch (JsonSyntaxException e) {
            logger.warn("RachioWebhook: Rejecting webhook request from {} because JSON parsing failed: {}", ipAddress,
                    e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (RuntimeException e) {
            RachioEventGsonDTO failedEvent = event;
            if (failedEvent != null) {
                logger.debug(
                        "RachioWebhook: Exception processing validated webhook event; event remains retryable ({}): {}",
                        describeEvent(failedEvent), e.getMessage(), e);
            } else {
                logger.debug("RachioWebhook: Exception processing validated webhook callback: {}", e.getMessage(), e);
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress != null ? ipAddress : "unknown";
    }

    private RachioEventGsonDTO parseEvent(String data) {
        try {
            return parseEventDirectly(data);
        } catch (JsonSyntaxException e) {
            String legacyData = createLegacyJson(data);
            if (legacyData == null) {
                throw e;
            }
            logger.debug("RachioWebhook: Attempting legacy malformed webhook parser after direct JSON parsing failed");
            return parseEventDirectly(legacyData);
        }
    }

    private RachioEventGsonDTO parseEventDirectly(String data) {
        RachioEventGsonDTO event = gson.fromJson(data, RachioEventGsonDTO.class);
        if (event == null) {
            throw new JsonSyntaxException("Webhook payload did not contain an event object");
        }
        return event;
    }

    private @Nullable String createLegacyJson(String data) {
        String unwrappedJson = unwrapLegacyJsonString(data);
        if (unwrappedJson != null) {
            return unwrappedJson;
        }
        return convertLegacyStringifiedObjects(data);
    }

    private @Nullable String unwrapLegacyJsonString(String data) {
        try {
            JsonElement root = JsonParser.parseString(data);
            if (!root.isJsonPrimitive()) {
                return null;
            }
            JsonPrimitive primitive = root.getAsJsonPrimitive();
            if (!primitive.isString()) {
                return null;
            }
            String value = primitive.getAsString().trim();
            return looksLikeJsonObject(value) ? value : null;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private @Nullable String convertLegacyStringifiedObjects(String data) {
        try {
            JsonElement root = JsonParser.parseString(data);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonObject normalized = root.getAsJsonObject().deepCopy();
            boolean changed = replaceStringifiedObject(normalized, "payload");
            changed |= replaceStringifiedObject(normalized, "network");
            changed |= replaceStringifiedObject(normalized, "zoneRunStatus");
            changed |= replaceStringifiedObject(normalized, "eventParms");
            changed |= replaceStringifiedObject(normalized, "deltaProperties");
            return changed ? gson.toJson(normalized) : null;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private boolean replaceStringifiedObject(JsonObject object, String memberName) {
        JsonElement member = object.get(memberName);
        if (member == null || !member.isJsonPrimitive()) {
            return false;
        }
        JsonPrimitive primitive = member.getAsJsonPrimitive();
        if (!primitive.isString()) {
            return false;
        }
        String value = primitive.getAsString().trim();
        if (!looksLikeJsonObject(value)) {
            return false;
        }

        try {
            JsonElement parsed = JsonParser.parseString(value);
            if (!parsed.isJsonObject()) {
                return false;
            }
            object.add(memberName, parsed);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    private boolean looksLikeJsonObject(String value) {
        return value.startsWith("{") && value.endsWith("}");
    }

    private boolean isDuplicateEvent(RachioEventGsonDTO event) {
        if (isBlank(event.eventId)) {
            logger.trace("RachioWebhook: Validated webhook event has no eventId; duplicate detection skipped ({})",
                    describeEvent(event));
            return false;
        }
        if (duplicateEventCache.isProcessed(event.eventId)) {
            logger.debug("RachioWebhook: Skipping duplicate processed webhook event ({})", describeEvent(event));
            return true;
        }
        return false;
    }

    private void markEventProcessed(RachioEventGsonDTO event) {
        if (isBlank(event.eventId)) {
            logger.trace("RachioWebhook: Processed webhook event has no eventId; duplicate cache not updated ({})",
                    describeEvent(event));
            return;
        }
        duplicateEventCache.markProcessed(event.eventId);
        logger.trace("RachioWebhook: Marked webhook event as processed ({})", describeEvent(event));
    }

    static String describeEvent(RachioEventGsonDTO event) {
        return "eventId=" + printable(event.eventId) + ", eventType=" + printable(event.eventType) + ", resourceType="
                + printable(event.resourceType) + ", resourceId=" + printable(event.resourceId) + ", deviceId="
                + printable(event.deviceId) + ", externalIdPresent=" + !isBlank(event.externalId);
    }

    static String describeLegacyClassification(RachioEventGsonDTO event) {
        return "legacyTypeRecognized=" + event.isLegacyNotificationTypeRecognized() + ", eventIdPresent="
                + !isBlank(event.eventId) + ", resourceIdPresent=" + !isBlank(event.resourceId) + ", timestampPresent="
                + !isBlank(event.timestamp) + ", payloadPresent=" + (event.payload != null) + ", eventTypePresent="
                + !isBlank(event.eventType) + ", resourceTypePresent=" + !isBlank(event.resourceType) + ", type='"
                + event.getLegacyNotificationTypeForLogging() + "', subTypePresent=" + !isBlank(event.subType)
                + ", deviceIdPresent=" + !isBlank(event.deviceId) + ", externalIdPresent=" + !isBlank(event.externalId);
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }

    private static String printable(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return "n/a";
        }
        return value;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(SERVLET_WEBHOOK_CHARSET);
        response.setContentType(SERVLET_WEBHOOK_APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, " + WEBHOOK_SIGNATURE_HEADER);
    }
}
