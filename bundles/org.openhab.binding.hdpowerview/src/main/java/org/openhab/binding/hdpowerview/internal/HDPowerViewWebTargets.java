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
 * 
 */
@NonNullByDefault
public class HDPowerViewWebTargets {

    private WebTarget base;
    private WebTarget shades;
    private WebTarget shade;
    private WebTarget sceneActivate;
    private WebTarget scenes;
    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);
    public final Gson gson;

    public HDPowerViewWebTargets(Client client, String ipAddress) {
        base = client.target("http://" + ipAddress + "/api");
        shades = base.path("shades/");
        shade = base.path("shades/{id}");
        sceneActivate = base.path("scenes");
        scenes = base.path("scenes/");
        gson = new Gson();
    }

    public @Nullable Shades getShades() throws JsonParseException, ProcessingException, HubMaintenanceException {
        String json = invoke(shades.request().buildGet(), shades);
        return gson.fromJson(json, Shades.class);
    }

    public @Nullable Shade moveShade(String shadeIdString, ShadePosition position)
            throws ProcessingException, HubMaintenanceException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shade.resolveTemplate("id", shadeId);
        String json = gson.toJson(new ShadeMove(shadeId, position));
        json = invoke(target.request().buildPut(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE)), target);
        return gson.fromJson(json, Shade.class);
    }

    public @Nullable Scenes getScenes() throws JsonParseException, ProcessingException, HubMaintenanceException {
        String json = invoke(scenes.request().buildGet(), scenes);
        return gson.fromJson(json, Scenes.class);
    }

    public void activateScene(int sceneId) throws ProcessingException, HubMaintenanceException {
        WebTarget target = sceneActivate.queryParam("sceneId", sceneId);
        invoke(target.request().buildGet(), target);
    }

    private String invoke(Invocation invocation, WebTarget target) throws ProcessingException, HubMaintenanceException {
        logger.trace("API request = {}", target.getUri());
        Response response;
        synchronized (this) {
            response = invocation.invoke();
        }
        int statusCode = response.getStatus();
        if (statusCode == 423) {
            /*
             * the hub returns a 423 error (resource locked) daily just after midnight;
             * which means it is temporarily busy for maintenance, so we should not worry
             * too much about this message
             */
            logger.debug("Hub is undergoing temporary maintenance");
            if (response.hasEntity()) {
                response.readEntity(String.class);
            }
            response.close();
            throw new HubMaintenanceException("Hub temporary maintenance");
        }
        if (statusCode != 200) {
            logger.warn("Hub returned HTTP error '{}' while invoking {}", statusCode, target.getUri());
            if (response.hasEntity()) {
                response.readEntity(String.class);
            }
            response.close();
            throw new ProcessingException(String.format("HTTP %d error", statusCode));
        }
        if (!response.hasEntity()) {
            logger.warn("Hub returned no content while invoking {}", target.getUri());
            response.close();
            throw new ProcessingException("Missing response entity");
        }
        @Nullable
        String json = response.readEntity(String.class);
        logger.trace("JSON response = {}", json);
        return json;
    }

    public @Nullable Shade refreshShade(String shadeIdString) throws ProcessingException, HubMaintenanceException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shade.resolveTemplate("id", shadeId).queryParam("refresh", true);
        String json = invoke(target.request().buildGet(), target);
        return gson.fromJson(json, Shade.class);
    }
}
