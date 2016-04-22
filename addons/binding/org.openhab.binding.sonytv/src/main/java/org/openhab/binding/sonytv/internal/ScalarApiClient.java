/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv.internal;

import java.net.ConnectException;
import java.net.NoRouteToHostException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link ScalarApiClient} is an abstraction over Sony Bravia's JSON-RPC API.
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class ScalarApiClient {

    private static final String PSK_HEADER = "X-Auth-PSK";

    private Gson gson = new Gson();
    private final Client client = ClientBuilder.newClient();
    private WebTarget api;
    private WebTarget system;
    private WebTarget avContent;
    private String preSharedKey;

    public ScalarApiClient(final String baseURL, final String preSharedKey) throws Error {
        this.api = client.target(Preconditions.checkNotNull(baseURL));
        this.system = api.path("system");
        this.avContent = api.path("avContent");
        this.preSharedKey = Preconditions.checkNotNull(preSharedKey);
        getPower();
    }

    public SystemInformation getSystemInformation() throws Error {
        final JsonObject result = rpc(system, "getSystemInformation").get(0).getAsJsonObject();
        final SystemInformation info = new SystemInformation();
        info.setProduct(result.get("product").getAsString());
        info.setRegion(result.get("region").getAsString());
        info.setLanguage(result.get("language").getAsString());
        info.setModel(result.get("model").getAsString());
        info.setSerial(result.get("serial").getAsString());
        info.setMac(result.get("macAddr").getAsString());
        info.setName(result.get("name").getAsString());
        info.setGeneration(result.get("generation").getAsString());
        info.setArea(result.get("area").getAsString());
        info.setCid(result.get("cid").getAsString());
        return info;
    }

    public StringType getActiveInput() throws Error {
        final JsonArray result = rpc(avContent, "getPlayingContentInfo");
        if (result == null) {
            return new StringType("");
        } else {
            return new StringType(result.get(0).getAsJsonObject().get("uri").getAsString());
        }
    }

    public void setActiveInput(final StringType input) throws Error {
        final JsonObject request = new JsonObject();
        request.addProperty("uri", input.toString());
        rpc(avContent, "setPlayContent", request);
    }

    public OnOffType getPower() throws Error {
        final JsonArray result = rpc(system, "getPowerStatus");
        if (result.get(0).getAsJsonObject().get("status").getAsString().equals("active")) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    public void setPower(final OnOffType power) throws Error {
        final JsonObject request = new JsonObject();
        request.addProperty("status", power.equals(OnOffType.ON));
        rpc(system, "setPowerStatus", request);
    }

    private JsonArray rpc(final WebTarget target, final String method, final JsonElement... arguments) throws Error {
        final Invocation.Builder invocation = target.request(MediaType.APPLICATION_JSON_TYPE);
        invocation.header(PSK_HEADER, preSharedKey);
        final Response httpResponse;
        try {
            httpResponse = invocation.post(makeJsonRpcRequest(method, arguments));
        } catch (final ProcessingException exception) {
            if (exception.getCause() instanceof NoRouteToHostException
                    || exception.getCause() instanceof ConnectException) {
                throw new ConnectionError(exception.getCause());
            } else {
                throw exception;
            }
        }
        final String responseText;
        final JsonObject response;
        if (httpResponse.hasEntity()) {
            responseText = httpResponse.readEntity(String.class);
            response = gson.fromJson(responseText, JsonObject.class);
        } else {
            responseText = null;
            response = null;
        }
        if (httpResponse.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL)) {
            if (response != null && response.has("result") && response.get("result").isJsonArray()) {
                return response.getAsJsonArray("result");
            } else {
                return null;
            }
        } else {
            throw new InvocationError(httpResponse.getStatusInfo(), responseText);
        }
    }

    private Entity<String> makeJsonRpcRequest(final String method, final JsonElement... arguments) {
        final JsonArray params = new JsonArray();
        for (final JsonElement argument : arguments) {
            params.add(argument);
        }
        final JsonObject request = new JsonObject();
        request.addProperty("version", "1.0");
        request.addProperty("id", 1);
        request.addProperty("method", method);
        request.add("params", params);
        return Entity.entity(gson.toJson(request), MediaType.APPLICATION_JSON_TYPE);
    }

    public static class Error extends Exception {
        private static final long serialVersionUID = 1L;

        public Error() {
        }

        public Error(final Throwable cause) {
            super(cause);
        }
    }

    public static class ConnectionError extends Error {
        private static final long serialVersionUID = 1L;

        public ConnectionError(final Throwable cause) {
            super(cause);
        }
    }

    public static class InvocationError extends Error {
        private static final long serialVersionUID = 1L;

        private final Response.StatusType status;
        private final String response;

        public InvocationError(final Response.StatusType status, final String response) {
            this.status = Preconditions.checkNotNull(status);
            this.response = response;
        }

        public Response.StatusType getStatus() {
            return status;
        }

        public String getReason() {
            return response;
        }

        @Override
        public String getMessage() {
            final StringBuilder messageBuilder = new StringBuilder("ScalarAPI error: ");
            messageBuilder.append(status.toString());
            if (response != null) {
                messageBuilder.append(": ");
                messageBuilder.append(response);
            }
            return messageBuilder.toString();
        }

    }

}