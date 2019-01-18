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
package org.openhab.binding.hdpowerview.internal;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    public Shades getShades() throws IOException {
        Response response = invoke(shades.request().buildGet(), shades);
        if (response != null) {
            String result = response.readEntity(String.class);
            return gson.fromJson(result, Shades.class);
        } else {
            return null;
        }
    }

    public Response moveShade(String shadeIdString, ShadePosition position) throws IOException {
        int shadeId = Integer.parseInt(shadeIdString);
        WebTarget target = shadeMove.resolveTemplate("id", shadeId);

        String body = gson.toJson(new ShadeMove(shadeId, position));
        return invoke(target.request().buildPut(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE)), shadeMove);
    }

    public Scenes getScenes() throws JsonParseException, IOException {
        Response response = invoke(scenes.request().buildGet(), scenes);
        if (response != null) {
            String result = response.readEntity(String.class);
            return gson.fromJson(result, Scenes.class);
        } else {
            return null;
        }
    }

    public void activateScene(int sceneId) {
        WebTarget target = sceneActivate.queryParam("sceneId", sceneId);
        invoke(target.request().buildGet(), sceneActivate);
    }

    private Response invoke(Invocation invocation, WebTarget target) {
        Response response;
        synchronized (this) {
            response = invocation.invoke();
        }

        if (response.getStatus() != 200) {
            logger.error("Bridge returned {} while invoking {} : {}", response.getStatus(), target.getUri(),
                    response.readEntity(String.class));
            return null;
        } else if (!response.hasEntity()) {
            logger.error("Bridge returned null response while invoking {}", target.getUri());
            return null;
        }

        return response;
    }

}
