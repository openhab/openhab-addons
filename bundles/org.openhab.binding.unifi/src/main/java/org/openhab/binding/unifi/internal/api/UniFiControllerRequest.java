/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link UniFiControllerRequest} encapsulates a request sent by the {@link UniFiController}.
 *
 * @author Matthew Bowman - Initial contribution
 *
 * @param <T> The response type expected as a result of the request's execution
 */
@NonNullByDefault
class UniFiControllerRequest<T> {

    private static final String CONTROLLER_PARSE_ERROR = "@text/error.controller.parse_error";

    private static final String CONTENT_TYPE_APPLICATION_JSON_UTF_8 = MimeTypes.Type.APPLICATION_JSON_UTF_8.asString();

    private static final String PROPERTY_DATA = "data";

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerRequest.class);

    private final Gson gson;

    private final HttpClient httpClient;

    private final String host;

    private final int port;

    private final boolean unifios;
    private final int timeoutSeconds;

    private final HttpMethod method;

    private String path = "/";

    private String csrfToken;

    private final Map<String, String> queryParameters = new HashMap<>();

    private final Map<String, Object> bodyParameters = new HashMap<>();

    private final Class<T> resultType;

    // Public API

    public UniFiControllerRequest(final Class<T> resultType, final Gson gson, final HttpClient httpClient,
            final HttpMethod method, final String host, final int port, final String csrfToken, final boolean unifios,
            int timeoutSeconds) {
        this.resultType = resultType;
        this.gson = gson;
        this.httpClient = httpClient;
        this.method = method;
        this.host = host;
        this.port = port;
        this.csrfToken = csrfToken;
        this.unifios = unifios;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setAPIPath(final String relativePath) {
        if (unifios) {
            this.path = "/proxy/network" + relativePath;
        } else {
            this.path = relativePath;
        }
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setBodyParameter(final String key, final Object value) {
        this.bodyParameters.put(key, value);
    }

    public void setQueryParameter(final String key, final Object value) {
        this.queryParameters.put(key, String.valueOf(value));
    }

    public @Nullable T execute() throws UniFiException {
        T result = (T) null;
        final String json = getContent();
        // mgb: only try and unmarshall non-void result types
        if (!Void.class.equals(resultType)) {
            try {
                final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                if (jsonObject.has(PROPERTY_DATA) && jsonObject.get(PROPERTY_DATA).isJsonArray()) {
                    result = (T) gson.fromJson(jsonObject.getAsJsonArray(PROPERTY_DATA), resultType);
                }
            } catch (final JsonParseException e) {
                logger.debug(
                        "Could not parse content retrieved from the server. Is the configuration pointing to the right server/port?, {}",
                        e.getMessage());
                if (logger.isTraceEnabled()) {
                    prettyPrintJson(json);
                }
                throw new UniFiCommunicationException(CONTROLLER_PARSE_ERROR);
            }
        }
        return result;
    }

    // Private API

    private String getContent() throws UniFiException {
        String content;
        final InputStreamResponseListener listener = new InputStreamResponseListener();
        final Response response = getContentResponse(listener);
        final int status = response.getStatus();
        switch (status) {
            case HttpStatus.OK_200:
                content = responseToString(listener);
                if (logger.isTraceEnabled()) {
                    logger.trace("<< {} {} \n{}", status, HttpStatus.getMessage(status), prettyPrintJson(content));
                }

                final String csrfToken = response.getHeaders().get("X-CSRF-Token");
                if (csrfToken != null && !csrfToken.isEmpty()) {
                    this.csrfToken = csrfToken;
                }
                break;
            case HttpStatus.BAD_REQUEST_400:
                logger.info("UniFi returned a status 400: {}", prettyPrintJson(responseToString(listener)));
                throw new UniFiInvalidCredentialsException("Invalid Credentials");
            case HttpStatus.UNAUTHORIZED_401:
                throw new UniFiExpiredSessionException("Expired Credentials");
            case HttpStatus.FORBIDDEN_403:
                throw new UniFiNotAuthorizedException("Unauthorized Access");
            default:
                logger.info("UniFi returned a status code {}: {}", status, prettyPrintJson(responseToString(listener)));
                throw new UniFiException("Unknown HTTP status code " + status + " returned by the controller");
        }
        return content;
    }

    private Response getContentResponse(final InputStreamResponseListener listener) throws UniFiException {
        final Request request = newRequest();
        logger.trace(">> {} {}", request.getMethod(), request.getURI());
        Response response;
        try {
            request.send(listener);
            response = listener.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            throw new UniFiCommunicationException(e);
        } catch (final ExecutionException e) {
            // mgb: unwrap the cause and try to cleanly handle it
            final Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                throw new UniFiCommunicationException(e);
            } else if (cause instanceof UnknownHostException) {
                // invalid hostname
                throw new UniFiInvalidHostException(cause);
            } else if (cause instanceof ConnectException) {
                // cannot connect
                throw new UniFiCommunicationException(cause);
            } else if (cause instanceof SSLException) {
                // cannot establish ssl connection
                throw new UniFiSSLException(cause);
            } else if (cause instanceof HttpResponseException httpResponseException
                    && httpResponseException.getResponse() instanceof ContentResponse) {
                // the UniFi controller violates the HTTP protocol
                // - it returns 401 UNAUTHORIZED without the WWW-Authenticate response header
                // - this causes an ExecutionException to be thrown
                // - we unwrap the response from the exception for proper handling of the 401 status code
                response = httpResponseException.getResponse();
            } else {
                // catch all
                throw new UniFiException(cause);
            }
        }
        return response;
    }

    private static String responseToString(final InputStreamResponseListener listener) throws UniFiException {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
        try (InputStream input = listener.getInputStream()) {
            input.transferTo(responseContent);
        } catch (final IOException e) {
            throw new UniFiException(e);
        }
        return new String(responseContent.toByteArray(), StandardCharsets.UTF_8);
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    private Request newRequest() {
        final HttpURI uri = new HttpURI(HttpScheme.HTTPS.asString(), host, port, path);
        final Request request = httpClient.newRequest(uri.toString()).timeout(timeoutSeconds, TimeUnit.SECONDS)
                .method(method);
        for (final Entry<String, String> entry : queryParameters.entrySet()) {
            request.param(entry.getKey(), entry.getValue());
        }
        if (!bodyParameters.isEmpty()) {
            final String jsonBody = gson.toJson(bodyParameters);

            logger.debug("Body parameters for request '{}': {}", request.getPath(), jsonBody);
            request.content(
                    new StringContentProvider(CONTENT_TYPE_APPLICATION_JSON_UTF_8, jsonBody, StandardCharsets.UTF_8));
        }

        if (!csrfToken.isEmpty()) {
            request.header("x-csrf-token", this.csrfToken);
        }

        return request;
    }

    private String prettyPrintJson(final String content) {
        try {
            final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

            return prettyGson.toJson(json);
        } catch (final RuntimeException e) {
            logger.debug("RuntimeException pretty printing JSON. Returning the raw content.", e);
            // If could not parse the string as JSON, just return the string
            return content;
        }
    }
}
