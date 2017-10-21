/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal.devices;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link BaseDevice} class defines a base class for Blebox devices
 * used across the whole binding.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public abstract class BaseDevice {
    protected String name;
    protected final String type;
    protected final String ipAddress;
    private Gson gson = new Gson();

    public BaseDevice(String type, String ipAddress) {
        this.type = type;
        this.ipAddress = ipAddress;
    }

    public <T extends BaseResponse, N extends BaseRequest> T postJson(N requestObject, String path, Class<T> type,
            String responseRoot) throws ClientProtocolException, IOException, URISyntaxException {
        URI url = buildUrl(path);
        String json = null;

        JsonElement je = gson.toJsonTree(requestObject);
        JsonObject jo = new JsonObject();

        if (requestObject.getRootElement() != null) {
            jo.add(requestObject.getRootElement(), je);
            json = jo.toString();
        } else {
            json = je.toString();
        }

        final Request request = Request.Post(url).connectTimeout(BleboxBindingConstants.TIMEOUT)
                .socketTimeout(BleboxBindingConstants.TIMEOUT)
                .bodyString(json, ContentType.APPLICATION_FORM_URLENCODED);

        Content response = request.execute().returnContent();

        if (responseRoot != null) {
            JsonParser p = new JsonParser();
            JsonElement jsonContainer = p.parse(response.asString());
            JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

            return gson.fromJson(jsonQuery, type);
        } else {
            return gson.fromJson(response.asString(), type);
        }
    }

    public <T extends BaseResponse> T getJson(String path, Class<T> type, String responseRoot)
            throws ClientProtocolException, IOException, URISyntaxException {
        URI url = buildUrl(path);

        final Request request = Request.Get(url).connectTimeout(BleboxBindingConstants.TIMEOUT)
                .socketTimeout(BleboxBindingConstants.TIMEOUT);
        Content response = request.execute().returnContent();

        if (responseRoot != null) {
            JsonParser p = new JsonParser();
            JsonElement jsonContainer = p.parse(response.asString());
            JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

            return gson.fromJson(jsonQuery, type);
        } else {
            return gson.fromJson(response.asString(), type);
        }
    }

    public <T extends BaseResponse> T[] getJsonArray(String path, Class<T[]> type, String responseRoot)
            throws ClientProtocolException, IOException, URISyntaxException {
        URI url = buildUrl(path);

        final Request request = Request.Get(url).connectTimeout(BleboxBindingConstants.TIMEOUT)
                .socketTimeout(BleboxBindingConstants.TIMEOUT);
        Content response = request.execute().returnContent();

        if (responseRoot != null) {
            JsonParser p = new JsonParser();
            JsonElement jsonContainer = p.parse(response.asString());
            JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

            return gson.fromJson(jsonQuery, type);
        } else {
            return gson.fromJson(response.asString(), type);
        }
    }

    public URI buildUrl(String path) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(ipAddress).setPath(path);
        URI requestURL = null;

        requestURL = builder.build();

        return requestURL;
    }
}
