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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private final URL url;
    private final Random rand = new Random();
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(MieleGatewayCommunicationController.class);

    public MieleGatewayCommunicationController(String host) throws MalformedURLException {
        url = new URL("http://" + host + "/remote/json-rpc");
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
        JsonObject req = new JsonObject();
        int id = rand.nextInt(Integer.MAX_VALUE);
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("id", id);
        req.addProperty("method", methodName);

        JsonArray params = new JsonArray();
        for (Object o : args) {
            params.add(gson.toJsonTree(o));
        }
        req.add("params", params);

        String requestData = req.toString();
        String responseData = null;
        try {
            responseData = post(url, Collections.emptyMap(), requestData);
        } catch (IOException e) {
            throw new MieleRpcException("Exception occurred while posting data", e);
        }

        logger.trace("The request '{}' yields '{}'", requestData, responseData);
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
                        : null);
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

    private String post(URL url, Map<String, String> headers, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.addRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.connect();

        OutputStream out = null;

        try {
            out = connection.getOutputStream();

            out.write(data.getBytes());
            out.flush();

            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.debug("An unexpected status code was returned: '{}'", statusCode);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        String responseEncoding = connection.getHeaderField("Content-Encoding");
        responseEncoding = (responseEncoding == null ? "" : responseEncoding.trim());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream in = connection.getInputStream();
        try {
            in = connection.getInputStream();
            if ("gzip".equalsIgnoreCase(responseEncoding)) {
                in = new GZIPInputStream(in);
            }
            in = new BufferedInputStream(in);

            byte[] buff = new byte[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                bos.write(buff, 0, n);
            }
            bos.flush();
            bos.close();
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return bos.toString();
    }
}
