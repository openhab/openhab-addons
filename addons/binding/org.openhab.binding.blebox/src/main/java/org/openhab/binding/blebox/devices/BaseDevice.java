/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.devices;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

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
    public String Name;
    public String Type;

    public String IpAddress;

    public BaseDevice(String type, String ipAddress) {
        this.Type = type;
        this.IpAddress = ipAddress;
    }

    public <T extends BaseResponse, N extends BaseRequest> T postJson(N requestObject, String path, Class<T> type,
            String responseRoot) {
        try {
            Gson gson = new Gson();
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

            final Request request = Request.Post(url).connectTimeout(2000).socketTimeout(2000).bodyString(json,
                    ContentType.APPLICATION_FORM_URLENCODED);

            Content response = request.execute().returnContent();

            if (responseRoot != null) {
                JsonParser p = new JsonParser();
                JsonElement jsonContainer = p.parse(response.asString());
                JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

                return gson.fromJson(jsonQuery, type);
            } else {
                return gson.fromJson(response.asString(), type);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public <T extends BaseResponse> T getJson(String path, Class<T> type, String responseRoot) {
        try {
            Gson gson = new Gson();
            URI url = buildUrl(path);

            final Request request = Request.Get(url).connectTimeout(2000).socketTimeout(2000);
            Content response = request.execute().returnContent();

            if (responseRoot != null) {
                JsonParser p = new JsonParser();
                JsonElement jsonContainer = p.parse(response.asString());
                JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

                return gson.fromJson(jsonQuery, type);
            } else {
                return gson.fromJson(response.asString(), type);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public <T extends BaseResponse> T[] getJsonArray(String path, Class<T[]> type, String responseRoot) {
        try {
            Gson gson = new Gson();
            URI url = buildUrl(path);

            final Request request = Request.Get(url).connectTimeout(2000).socketTimeout(2000);
            Content response = request.execute().returnContent();

            if (responseRoot != null) {
                JsonParser p = new JsonParser();
                JsonElement jsonContainer = p.parse(response.asString());
                JsonElement jsonQuery = ((JsonObject) jsonContainer).get(responseRoot);

                return gson.fromJson(jsonQuery, type);
            } else {
                return gson.fromJson(response.asString(), type);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public URI buildUrl(String path) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(IpAddress).setPath(path);
        URI requestURL = null;
        try {
            requestURL = builder.build();
        } catch (URISyntaxException use) {
        }

        return requestURL;
    }
}
