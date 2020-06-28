/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal;

import java.time.Instant;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeMove;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeStop;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * JAX-RS targets for communicating with the HD Power View Hub
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class HDPowerViewWebTargets {

    private WebTarget base;
    private WebTarget shades;
    private WebTarget shade;
    private WebTarget sceneActivate;
    private WebTarget scenes;
    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);

    /*
     * the hub returns a 423 error (resource locked) daily just after midnight;
     * which means it is temporarily undergoing maintenance; so we use "soft"
     * exception handling during the five minute maintenance period after a 423
     * error is received
     */
    private final int maintenancePeriod = 300;
    private Instant maintenanceScheduledEnd = Instant.now().minusSeconds(2 * maintenancePeriod);

    public final Gson gson;

    private static final String CONN_HDR = "Connection";
    private static final String CONN_VAL = "close"; // versus "keep-alive"

    public HDPowerViewWebTargets(Client client, String ipAddress) {
        base = client.target("http://" + ipAddress + "/api");
        shades = base.path("shades/");
        shade = base.path("shades/{id}");
        sceneActivate = base.path("scenes");
        scenes = base.path("scenes/");
        gson = new Gson();
    }

    public @Nullable Shades getShades() throws JsonParseException, ProcessingException, HubMaintenanceException {
        String json = invoke(shades.request().header(CONN_HDR, CONN_VAL).buildGet(), shades, null);
        return gson.fromJson(json, Shades.class);
    }

    public @Nullable Shade moveShade(String shadeIdString, ShadePosition position)
            throws ProcessingException, HubMaintenanceException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shade.resolveTemplate("id", shadeId);
        String json = gson.toJson(new ShadeMove(shadeId, position));
        logger.trace("JSON request = {}", json);
        json = invoke(target.request().header(CONN_HDR, CONN_VAL)
                .buildPut(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE)), target, json);
        logger.trace("JSON response = {}", json);
        return gson.fromJson(json, Shade.class);
    }

    public @Nullable Scenes getScenes() throws JsonParseException, ProcessingException, HubMaintenanceException {
        String json = invoke(scenes.request().header(CONN_HDR, CONN_VAL).buildGet(), scenes, null);
        return gson.fromJson(json, Scenes.class);
    }

    public void activateScene(int sceneId) throws ProcessingException, HubMaintenanceException {
        WebTarget target = sceneActivate.queryParam("sceneId", sceneId);
        invoke(target.request().header(CONN_HDR, CONN_VAL).buildGet(), target, null);
    }

    private String invoke(Invocation invocation, WebTarget target, @Nullable String jsonRequest)
            throws ProcessingException, HubMaintenanceException {
        if (logger.isTraceEnabled()) {
            logger.trace("API command {} {}", jsonRequest == null ? "GET" : "PUT", target.getUri());
            if (jsonRequest != null) {
                logger.trace("JSON request = {}", jsonRequest);
            }
        }
        Response response;
        try {
            synchronized (this) {
                response = invocation.invoke();
            }
        } catch (ProcessingException e) {
            if (Instant.now().isBefore(maintenanceScheduledEnd)) {
                // throw "softer" exception during maintenance window
                logger.debug("Hub still undergoing maintenance");
                throw new HubMaintenanceException("Hub still undergoing maintenance");
            }
            throw e;
        }
        int statusCode = response.getStatus();
        if (statusCode == 423) {
            // set end of maintenance window, and throw a "softer" exception
            maintenanceScheduledEnd = Instant.now().plusSeconds(maintenancePeriod);
            logger.debug("Hub undergoing maintenance");
            if (response.hasEntity()) {
                response.readEntity(String.class);
            }
            response.close();
            throw new HubMaintenanceException("Hub undergoing maintenance");
        }
        if (statusCode != 200) {
            logger.warn("Hub returned HTTP error '{}'", statusCode);
            if (response.hasEntity()) {
                response.readEntity(String.class);
            }
            response.close();
            throw new ProcessingException(String.format("HTTP %d error", statusCode));
        }
        if (!response.hasEntity()) {
            logger.warn("Hub returned no content");
            response.close();
            throw new ProcessingException("Missing response entity");
        }
        @SuppressWarnings("null")
        String jsonResponse = response.readEntity(String.class);
        if (logger.isTraceEnabled()) {
            logger.trace("JSON response = {}", jsonResponse);
        }
        return jsonResponse;
    }

    public @Nullable Shade refreshShade(String shadeIdString) throws ProcessingException, HubMaintenanceException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shade.resolveTemplate("id", shadeId).queryParam("refresh", true);
        String json = invoke(target.request().header(CONN_HDR, CONN_VAL).buildGet(), target, null);
        return gson.fromJson(json, Shade.class);
    }

    public @Nullable Shade stopShade(String shadeIdString) throws ProcessingException, HubMaintenanceException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shade.resolveTemplate("id", shadeId);
        String json = gson.toJson(new ShadeStop(shadeId));
        logger.trace("JSON request = {}", json);
        json = invoke(target.request().header(CONN_HDR, CONN_VAL)
                .buildPut(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE)), target, json);
        return gson.fromJson(json, Shade.class);
    }
}
