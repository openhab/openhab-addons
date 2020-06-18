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
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * JAX-RS targets for communicating with the HD Power View Hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewWebTargets {

    private WebTarget base;
    private WebTarget shades;
    private WebTarget shadeMove;
    private WebTarget sceneActivate;
    private WebTarget scenes;
    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);
    private final Gson gson;

    public HDPowerViewWebTargets(Client client, String ipAddress) {
        base = client.target("http://" + ipAddress + "/api");
        shades = base.path("shades/");
        shadeMove = base.path("shades/{id}");
        sceneActivate = base.path("scenes");
        scenes = base.path("scenes/");
        gson = new Gson();
    }

    public @Nullable Shades getShades() throws JsonParseException, ProcessingException {
        @Nullable
        String json = invoke(shades.request().buildGet(), shades).readEntity(String.class);
        return gson.fromJson(json, Shades.class);
    }

    public Response moveShade(String shadeIdString, ShadePosition position) throws ProcessingException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shadeMove.resolveTemplate("id", shadeId);
        String json = gson.toJson(new ShadeMove(shadeId, position));
        return invoke(target.request().buildPut(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE)), shadeMove);
    }

    public @Nullable Scenes getScenes() throws JsonParseException, ProcessingException {
        @Nullable
        String json = invoke(scenes.request().buildGet(), scenes).readEntity(String.class);
        return gson.fromJson(json, Scenes.class);
    }

    public void activateScene(int sceneId) throws ProcessingException {
        WebTarget target = sceneActivate.queryParam("sceneId", sceneId);
        invoke(target.request().buildGet(), sceneActivate).readEntity(String.class);
    }

    private Response invoke(Invocation invocation, WebTarget target) throws ProcessingException {
        Response response;
        synchronized (this) {
            response = invocation.invoke();
        }
//        if (response.getStatus() == 423) {
//            /*
//             * the hub seems to return a 423 error (resource locked) once per day around
//             * midnight this is probably some kind of regular re-initialization process, so
//             * use logger.debug() instead of logger.warn()
//             */
//            logger.debug("Bridge returned '{}' while invoking {} : {} : {}", response.getStatus(), target.getUri(),
//                    response.getStringHeaders().toString(), response.readEntity(String.class));
//            throw new ProcessingException("Returned an HTTP error");
//        } 
        if (response.getStatus() != 200) {
            logger.warn("Bridge returned '{}' while invoking {} : {} : {}", response.getStatus(), target.getUri(),
                    response.getStringHeaders().toString(), response.readEntity(String.class));
            throw new ProcessingException("Returned an HTTP error");
        } 
        if (!response.hasEntity()) {
            logger.warn("Bridge returned null response while invoking {}", target.getUri());
            throw new ProcessingException("Missing response entity");
        }
        return response;
    }
}
