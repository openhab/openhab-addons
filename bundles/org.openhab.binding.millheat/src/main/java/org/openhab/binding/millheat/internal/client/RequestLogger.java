/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.client;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Logs HttpClient request/response traffic.
 *
 * @author Gili Tzabari - Initial contribution https://stackoverflow.com/users/14731/gili
 *         https://stackoverflow.com/questions/50318736/how-to-log-httpclient-requests-response-including-body
 * @author Arne Seime - adapted for Millheat binding
 */
@NonNullByDefault
public final class RequestLogger {
    private final Logger logger = LoggerFactory.getLogger(RequestLogger.class);
    private final AtomicLong nextId = new AtomicLong();
    private final Gson gson;
    private final String prefix;

    public RequestLogger(final String prefix, final Gson gson) {
        this.gson = gson;
        this.prefix = prefix;
    }

    private void dump(final Request request) {
        final long idV = nextId.getAndIncrement();
        if (logger.isDebugEnabled()) {
            final String id = prefix + "-" + idV;
            final StringBuilder group = new StringBuilder();
            request.onRequestBegin(theRequest -> group.append(
                    String.format("Request %s\n%s > %s %s\n", id, id, theRequest.getMethod(), theRequest.getURI())));
            request.onRequestHeaders(theRequest -> {
                for (final HttpField header : theRequest.getHeaders()) {
                    group.append(String.format("%s > %s\n", id, header));
                }
            });
            final StringBuilder contentBuffer = new StringBuilder();
            request.onRequestContent((theRequest, content) -> contentBuffer
                    .append(reformatJson(getCharset(theRequest.getHeaders()).decode(content).toString())));
            request.onRequestSuccess(theRequest -> {
                if (contentBuffer.length() > 0) {
                    group.append("\n");
                    group.append(contentBuffer);
                }
                String dataToLog = group.toString();
                logger.debug(dataToLog);
                contentBuffer.delete(0, contentBuffer.length());
                group.delete(0, group.length());
            });
            request.onResponseBegin(theResponse -> {
                group.append(String.format("Response %s\n%s < %s %s", id, id, theResponse.getVersion(),
                        theResponse.getStatus()));
                if (theResponse.getReason() != null) {
                    group.append(" ");
                    group.append(theResponse.getReason());
                }
                group.append("\n");
            });
            request.onResponseHeaders(theResponse -> {
                for (final HttpField header : theResponse.getHeaders()) {
                    group.append(String.format("%s < %s\n", id, header));
                }
            });
            request.onResponseContent((theResponse, content) -> contentBuffer
                    .append(reformatJson(getCharset(theResponse.getHeaders()).decode(content).toString())));
            request.onResponseSuccess(theResponse -> {
                if (contentBuffer.length() > 0) {
                    group.append("\n");
                    group.append(contentBuffer);
                }
                String dataToLog = group.toString();
                logger.debug(dataToLog);
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
            final JsonElement json = JsonParser.parseString(jsonString);
            return gson.toJson(json);
        } catch (final JsonSyntaxException e) {
            logger.debug("Could not reformat malformed JSON due to '{}'", e.getMessage());
            return jsonString;
        }
    }
}
