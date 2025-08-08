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
package org.openhab.binding.tuya.internal.cloud;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TuyaContentListener} is a {@link BufferingResponseListener} implementation for the Tuya Cloud
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaContentListener extends BufferingResponseListener {
    private final Logger logger = LoggerFactory.getLogger(TuyaContentListener.class);
    private final CompletableFuture<String> future;

    public TuyaContentListener(CompletableFuture<String> future) {
        this.future = future;
    }

    @Override
    public void onComplete(Result result) {
        Response response = result.getResponse();
        if (logger.isTraceEnabled()) {
            logger.trace("Received from '{}': {}", result.getRequest().getURI(), responseToLogString(response));
        }
        Request request = result.getRequest();
        if (result.isFailed()) {
            logger.debug("Requesting '{}' (method='{}', content='{}') failed: {}", request.getURI(),
                    request.getMethod(), request.getContent(), result.getFailure().getMessage());
            future.completeExceptionally(new ConnectionException("Request failed " + result.getFailure().getMessage()));
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
                    if (content != null) {
                        future.complete(new String(content, StandardCharsets.UTF_8));
                    } else {
                        future.completeExceptionally(new ConnectionException("Content is null."));
                    }
                    break;
                default:
                    logger.debug("Requesting '{}' (method='{}', content='{}') failed: {} {}", request.getURI(),
                            request.getMethod(), request.getContent(), response.getStatus(), response.getReason());
                    future.completeExceptionally(
                            new ConnectionException("Invalid status code " + response.getStatus()));
            }
        }
    }

    private String responseToLogString(Response response) {
        return "Code = {" + response.getStatus() + "}, Headers = {"
                + response.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + getContentAsString() + "}";
    }
}
