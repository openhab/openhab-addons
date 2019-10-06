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
package org.openhab.binding.sensibo.internal.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Logs HttpClient request/response traffic.
 *
 * @author Gili Tzabari - Initial contribution https://stackoverflow.com/users/14731/gili
 *         https://stackoverflow.com/questions/50318736/how-to-log-httpclient-requests-response-including-body
 * @author Arne Seime - adapted for Sensibo binding
 */
@NonNullByDefault
public final class RequestLogger {
    private final Logger logger = LoggerFactory.getLogger(RequestLogger.class);
    private final AtomicLong nextId = new AtomicLong();
    private final JsonParser parser;
    private final Gson gson;
    private final String prefix;

    public RequestLogger(final String prefix) {
        parser = new JsonParser();
        gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        this.prefix = prefix;
    }

    private void dump(final Request request) {
        final long idV = nextId.getAndIncrement();
        if (logger.isDebugEnabled()) {
            final String id = prefix + "-" + idV;
            final StringBuilder group = new StringBuilder();
            request.onRequestBegin(theRequest -> group.append(
                    "Request " + id + "\n" + id + " > " + theRequest.getMethod() + " " + theRequest.getURI() + "\n"));
            request.onRequestHeaders(theRequest -> {
                for (final HttpField header : theRequest.getHeaders()) {
                    group.append(id + " > " + header + "\n");
                }
            });
            final StringBuilder contentBuffer = new StringBuilder();
            request.onRequestContent((theRequest, content) -> contentBuffer
                    .append(reformatJson(getCharset(theRequest.getHeaders()).decode(content).toString())));
            request.onRequestSuccess(theRequest -> {
                if (contentBuffer.length() > 0) {
                    group.append("\n" + contentBuffer.toString());
                }
                String debugStatement = group.toString();
                logger.debug(debugStatement);
                contentBuffer.delete(0, contentBuffer.length());
                group.delete(0, group.length());
            });
            request.onResponseBegin(theResponse -> {
                group.append("Response " + id + "\n" + id + " < " + theResponse.getVersion() + " "
                        + theResponse.getStatus());
                if (theResponse.getReason() != null) {
                    group.append(" " + theResponse.getReason());
                }
                group.append("\n");
            });
            request.onResponseHeaders(theResponse -> {
                for (final HttpField header : theResponse.getHeaders()) {
                    group.append(id + " < " + header + "\n");
                }
            });
            request.onResponseContent((theResponse, content) -> contentBuffer
                    .append(reformatJson(getCharset(theResponse.getHeaders()).decode(content).toString())));
            request.onResponseSuccess(theResponse -> {
                if (contentBuffer.length() > 0) {
                    group.append("\n" + contentBuffer.toString());
                }
                String debugStatement = group.toString();
                logger.debug(debugStatement);
            });
        }
    }

    private Charset getCharset(final HttpFields headers) {
        final String contentType = headers.get(HttpHeader.CONTENT_TYPE);
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        final String[] tokens = contentType.toLowerCase(Locale.US).split("charset=");
        if (tokens.length != 2) {
            return StandardCharsets.UTF_8;
        }

        final String encoding = tokens[1].replaceAll("[;\"]", "");
        return Charset.forName(encoding);
    }

    public Request listenTo(final Request request) {
        dump(request);
        return request;
    }

    private String reformatJson(final String jsonString) {
        try {
            final JsonElement json = parser.parse(jsonString);
            return gson.toJson(json);
        } catch (final JsonSyntaxException e) {
            logger.debug("Could not reformat malformed JSON due to '{}'", e.getMessage());
            return jsonString;
        }
    }
}
