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
package org.openhab.binding.homeconnectdirect.internal.servlet.routing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SUPPORTED_THING_TYPES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_HTML_UTF8;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HEADER_X_FRAME_OPTIONS;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.SAME_ORIGIN;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Context object for request handlers.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class RequestHandlerContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Map<String, String> pathVariables;
    private final ConfigurationAdmin configurationAdmin;
    private final Gson gson;
    private final ThingRegistry thingRegistry;

    public RequestHandlerContext(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> pathVariables, ConfigurationAdmin configurationAdmin, Gson gson,
            ThingRegistry thingRegistry) {
        this.request = request;
        this.response = response;
        this.pathVariables = pathVariables;
        this.configurationAdmin = configurationAdmin;
        this.gson = gson;
        this.thingRegistry = thingRegistry;
    }

    public @Nullable String getVariable(String name) {
        return pathVariables.get(name);
    }

    public @Nullable String getQueryParameter(String name) {
        return request.getParameter(name);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public HomeConnectDirectConfiguration getConfiguration() {
        return ConfigurationUtils.getConfiguration(configurationAdmin);
    }

    public String getRequestBody() throws RequestHandlerException {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RequestHandlerException("Could not read request body.", e);
        }
    }

    public <T> @Nullable T getRequestObject(Class<T> type) throws RequestHandlerException {
        try {
            return gson.fromJson(getRequestBody(), type);
        } catch (JsonSyntaxException e) {
            throw new RequestHandlerException("Could not parse request body to " + type.getSimpleName(), e);
        }
    }

    public void sendHtml(String content) throws RequestHandlerException {
        response.setContentType(CONTENT_TYPE_HTML_UTF8);
        response.setCharacterEncoding(UTF_8.name());
        response.setHeader(HEADER_X_FRAME_OPTIONS, SAME_ORIGIN);

        try {
            response.getWriter().write(content);
        } catch (IOException e) {
            throw new RequestHandlerException("Failed to send HTML content.", e);
        }
    }

    public void sendNoContent() {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    public void sendCreated() {
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    public void sendJson(Object responseObject) throws RequestHandlerException {
        sendJson(responseObject, HttpStatus.OK_200);
    }

    public void sendJson(Object responseObject, int status) throws RequestHandlerException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setStatus(status);

        try {
            response.getWriter().write(gson.toJson(responseObject));
        } catch (IOException e) {
            throw new RequestHandlerException("Failed to send JSON content.", e);
        }
    }

    public List<Thing> getApplianceThings() {
        return thingRegistry.stream().filter(thing -> SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())).toList();
    }

    public @Nullable Thing getApplianceThing(String uid) {
        return thingRegistry.stream().filter(thing -> SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID()))
                .filter(thing -> thing.getUID().toString().equals(uid)).findFirst().orElse(null);
    }

    public @Nullable String getHaIdFromThing(Thing thing) {
        var configuration = thing.getConfiguration();
        var haId = configuration.get("haId");
        return haId != null ? haId.toString() : null;
    }

    public Gson getGson() {
        return gson;
    }
}
