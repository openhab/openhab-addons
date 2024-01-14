/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.http.internal.http;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.thing.binding.generic.ChannelHandlerContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpResponseListener} is responsible for processing the result of a HTTP request
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpResponseListener extends BufferingResponseListener {
    private final Logger logger = LoggerFactory.getLogger(HttpResponseListener.class);
    private final CompletableFuture<@Nullable ChannelHandlerContent> future;
    private final HttpStatusListener httpStatusListener;
    private final String fallbackEncoding;

    /**
     * the HttpResponseListener is responsible
     *
     * @param future Content future to complete with the result of the request
     * @param fallbackEncoding a fallback encoding for the content (UTF-8 if null)
     * @param bufferSize the buffer size for the content in kB (default 2048 kB)
     */
    public HttpResponseListener(CompletableFuture<@Nullable ChannelHandlerContent> future,
            @Nullable String fallbackEncoding, int bufferSize, HttpStatusListener httpStatusListener) {
        super(bufferSize * 1024);
        this.future = future;
        this.fallbackEncoding = fallbackEncoding != null ? fallbackEncoding : StandardCharsets.UTF_8.name();
        this.httpStatusListener = httpStatusListener;
    }

    @Override
    public void onComplete(@NonNullByDefault({}) Result result) {
        Response response = result.getResponse();
        if (logger.isTraceEnabled()) {
            logger.trace("Received from '{}': {}", result.getRequest().getURI(), responseToLogString(response));
        }
        Request request = result.getRequest();
        if (response == null || (result.isFailed() && response.getStatus() != HttpStatus.UNAUTHORIZED_401)) {
            logger.debug("Requesting '{}' (method='{}', content='{}') failed: {}", request.getURI(),
                    request.getMethod(), request.getContent(), result.getFailure().getMessage());
            future.complete(null);
            httpStatusListener.onHttpError(result.getFailure().getMessage());
        } else {
            switch (response.getStatus()) {
                case HttpStatus.OK_200:
                case HttpStatus.CREATED_201:
                case HttpStatus.ACCEPTED_202:
                case HttpStatus.NON_AUTHORITATIVE_INFORMATION_203:
                case HttpStatus.NO_CONTENT_204:
                case HttpStatus.RESET_CONTENT_205:
                case HttpStatus.PARTIAL_CONTENT_206:
                case HttpStatus.MULTI_STATUS_207:
                    byte[] content = getContent();
                    String encoding = getEncoding();
                    if (content != null) {
                        future.complete(new ChannelHandlerContent(content,
                                encoding == null ? fallbackEncoding : encoding, getMediaType()));
                    } else {
                        future.complete(null);
                    }
                    httpStatusListener.onHttpSuccess();
                    break;
                case HttpStatus.UNAUTHORIZED_401:
                    logger.debug("Requesting '{}' (method='{}', content='{}') failed: Authorization error",
                            request.getURI(), request.getMethod(), request.getContent());
                    future.completeExceptionally(new HttpAuthException());
                    break;
                default:
                    logger.debug("Requesting '{}' (method='{}', content='{}') failed: {} {}", request.getURI(),
                            request.getMethod(), request.getContent(), response.getStatus(), response.getReason());
                    future.complete(null);
                    httpStatusListener.onHttpError(response.getReason());
            }
        }
    }

    private String responseToLogString(Response response) {
        String logString = "Code = {" + response.getStatus() + "}, Headers = {"
                + response.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + getContentAsString() + "}";
        return logString;
    }
}
