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
package org.openhab.binding.unifi.internal.api.model;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.unifi.internal.api.UniFiCommunicationException;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiExpiredSessionException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidCredentialsException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidHostException;
import org.openhab.binding.unifi.internal.api.UniFiNotAuthorizedException;
import org.openhab.binding.unifi.internal.api.UniFiSSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link UniFiControllerRequest} encapsulates a request sent by the {@link UniFiController}.
 *
 * @author Matthew Bowman - Initial contribution
 *
 * @param <T> The response type expected as a result of the request's execution
 */
@NonNullByDefault
public class UniFiControllerRequest<T> {

    private static final String CONTENT_TYPE_APPLICATION_JSON = MimeTypes.Type.APPLICATION_JSON.asString();

    private static final long TIMEOUT_SECONDS = 5;

    private static final String PROPERTY_DATA = "data";

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerRequest.class);

    private final Gson gson;

    private final HttpClient httpClient;

    private final String host;

    private final int port;

    private String path = "/";

    private final boolean unifios;

    private String csrfToken;

    private Map<String, String> queryParameters = new HashMap<>();

    private Map<String, String> bodyParameters = new HashMap<>();

    private final Class<T> resultType;

    // Public API

    public UniFiControllerRequest(Class<T> resultType, Gson gson, HttpClient httpClient, String host, int port,
            String csrfToken, boolean unifios) {
        this.resultType = resultType;
        this.gson = gson;
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
        this.csrfToken = csrfToken;
        this.unifios = unifios;
    }

    public void setAPIPath(String relativePath) {
        if (unifios) {
            this.path = "/proxy/network" + relativePath;
        } else {
            this.path = relativePath;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBodyParameter(String key, Object value) {
        this.bodyParameters.put(key, String.valueOf(value));
    }

    public void setQueryParameter(String key, Object value) {
        this.queryParameters.put(key, String.valueOf(value));
    }

    public @Nullable T execute() throws UniFiException {
        T result = null;
        String json = getContent();
        // mgb: only try and unmarshall non-void result types
        if (!Void.class.equals(resultType)) {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (jsonObject.has(PROPERTY_DATA) && jsonObject.get(PROPERTY_DATA).isJsonArray()) {
                result = gson.fromJson(jsonObject.getAsJsonArray(PROPERTY_DATA), resultType);
            }
        }
        return result;
    }

    // Private API

    private String getContent() throws UniFiException {
        String content;
        ContentResponse response = getContentResponse();
        int status = response.getStatus();
        switch (status) {
            case HttpStatus.OK_200:
                content = response.getContentAsString();
                if (logger.isTraceEnabled()) {
                    logger.trace("<< {} {} \n{}", status, HttpStatus.getMessage(status), prettyPrintJson(content));
                }

                String csrfToken = response.getHeaders().get("X-CSRF-Token");
                if (csrfToken != null && !csrfToken.isEmpty()) {
                    this.csrfToken = csrfToken;
                }
                break;
            case HttpStatus.BAD_REQUEST_400:
                throw new UniFiInvalidCredentialsException("Invalid Credentials");
            case HttpStatus.UNAUTHORIZED_401:
                throw new UniFiExpiredSessionException("Expired Credentials");
            case HttpStatus.FORBIDDEN_403:
                throw new UniFiNotAuthorizedException("Unauthorized Access");
            default:
                throw new UniFiException("Unknown HTTP status code " + status + " returned by the controller");
        }
        return content;
    }

    private ContentResponse getContentResponse() throws UniFiException {
        Request request = newRequest();
        logger.trace(">> {} {}", request.getMethod(), request.getURI());
        ContentResponse response;
        try {
            response = request.send();
        } catch (TimeoutException | InterruptedException e) {
            throw new UniFiCommunicationException(e);
        } catch (ExecutionException e) {
            // mgb: unwrap the cause and try to cleanly handle it
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException) {
                // invalid hostname
                throw new UniFiInvalidHostException(cause);
            } else if (cause instanceof ConnectException) {
                // cannot connect
                throw new UniFiCommunicationException(cause);
            } else if (cause instanceof SSLException) {
                // cannot establish ssl connection
                throw new UniFiSSLException(cause);
            } else if (cause instanceof HttpResponseException
                    && ((HttpResponseException) cause).getResponse() instanceof ContentResponse) {
                // the UniFi controller violates the HTTP protocol
                // - it returns 401 UNAUTHORIZED without the WWW-Authenticate response header
                // - this causes an ExceptionException to be thrown
                // - we unwrap the response from the exception for proper handling of the 401 status code
                response = (ContentResponse) ((HttpResponseException) cause).getResponse();
            } else {
                // catch all
                throw new UniFiException(cause);
            }
        }
        return response;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    private Request newRequest() {
        HttpMethod method = bodyParameters.isEmpty() ? HttpMethod.GET : HttpMethod.POST;
        HttpURI uri = new HttpURI(HttpScheme.HTTPS.asString(), host, port, path);
        Request request = httpClient.newRequest(uri.toString()).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .method(method);
        for (Entry<String, String> entry : queryParameters.entrySet()) {
            request.param(entry.getKey(), entry.getValue());
        }
        if (!bodyParameters.isEmpty()) {
            String jsonBody = getRequestBodyAsJson();
            ContentProvider content = new StringContentProvider(CONTENT_TYPE_APPLICATION_JSON, jsonBody,
                    StandardCharsets.UTF_8);
            request = request.content(content);
        }

        if (!csrfToken.isEmpty()) {
            request.header("x-csrf-token", this.csrfToken);
        }

        return request;
    }

    private String getRequestBodyAsJson() {
        JsonObject jsonObject = new JsonObject();
        JsonElement jsonElement = null;
        for (Entry<String, String> entry : bodyParameters.entrySet()) {
            try {
                jsonElement = JsonParser.parseString(entry.getValue());
            } catch (JsonSyntaxException e) {
                jsonElement = new JsonPrimitive(entry.getValue());
            }
            jsonObject.add(entry.getKey(), jsonElement);
        }
        return jsonObject.toString();
    }

    private static String prettyPrintJson(String content) {
        JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        return prettyGson.toJson(json);
    }
}
