/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.miele.internal.exceptions.MieleRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link MieleGatewayCommunicationController} class is used for communicating with
 * the XGW 3000 gateway through JSON-RPC.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MieleGatewayCommunicationController {

    private final URI uri;
    private final Random rand = new Random();
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(MieleGatewayCommunicationController.class);
    private final HttpClient httpClient;

    public MieleGatewayCommunicationController(HttpClient httpClient, String host) throws URISyntaxException {
        uri = new URI("http://" + host + "/remote/json-rpc");
        this.httpClient = httpClient;
    }

    public JsonElement invokeOperation(FullyQualifiedApplianceIdentifier applianceIdentifier, String modelID,
            String methodName) throws MieleRpcException {
        Object[] args = new Object[4];
        args[0] = applianceIdentifier.getUid();
        args[1] = MieleBindingConstants.MIELE_CLASS + modelID;
        args[2] = methodName;
        args[3] = null;

        return invokeRPC("HDAccess/invokeDCOOperation", args);
    }

    public JsonElement invokeRPC(String methodName, Object[] args) throws MieleRpcException {
        JsonElement result = null;
        JsonObject requestBodyAsJson = new JsonObject();
        int id = rand.nextInt(Integer.MAX_VALUE);
        requestBodyAsJson.addProperty("jsonrpc", "2.0");
        requestBodyAsJson.addProperty("id", id);
        requestBodyAsJson.addProperty("method", methodName);

        JsonArray params = new JsonArray();
        for (Object o : args) {
            params.add(gson.toJsonTree(o));
        }
        requestBodyAsJson.add("params", params);

        String requestBody = requestBodyAsJson.toString();
        Request request = httpClient.newRequest(uri).method(HttpMethod.POST)
                .content(new StringContentProvider(requestBody), "application/json");

        String responseData = null;
        try {
            final ContentResponse contentResponse = request.send();
            final int httpStatus = contentResponse.getStatus();
            if (httpStatus != 200) {
                if (httpStatus == 503) {
                    throw new MieleRpcException("Gateway is temporarily unavailable");
                }
                throw new MieleRpcException("Unexpected HTTP status code " + httpStatus);
            }
            responseData = contentResponse.getContentAsString();
        } catch (TimeoutException e) {
            throw new MieleRpcException("Timeout when calling gateway", e);
        } catch (ExecutionException e) {
            throw new MieleRpcException("Failure when calling gateway", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MieleRpcException("Interrupted while calling gateway", e);
        }

        logger.trace("The request '{}' yields '{}'", requestBody, responseData);
        JsonObject parsedResponse = null;
        try {
            parsedResponse = (JsonObject) JsonParser.parseReader(new StringReader(responseData));
        } catch (JsonParseException e) {
            throw new MieleRpcException("Error parsing JSON response", e);
        }

        JsonElement error = parsedResponse.get("error");
        if (error != null && !error.isJsonNull()) {
            if (error.isJsonPrimitive()) {
                throw new MieleRpcException("Remote exception occurred: '" + error.getAsString() + "'");
            } else if (error.isJsonObject()) {
                JsonObject o = error.getAsJsonObject();
                Integer code = (o.has("code") ? o.get("code").getAsInt() : null);
                String message = (o.has("message") ? o.get("message").getAsString() : null);
                String data = (o.has("data")
                        ? (o.get("data") instanceof JsonObject ? o.get("data").toString() : o.get("data").getAsString())
                        : "");
                throw new MieleRpcException(
                        "Remote exception occurred: '" + code + "':'" + message + "':'" + data + "'");
            } else {
                throw new MieleRpcException("Unknown remote exception occurred: '" + error.toString() + "'");
            }
        }

        result = parsedResponse.get("result");
        if (result == null) {
            throw new MieleRpcException("Result is missing in response");
        }

        return result;
    }
}
