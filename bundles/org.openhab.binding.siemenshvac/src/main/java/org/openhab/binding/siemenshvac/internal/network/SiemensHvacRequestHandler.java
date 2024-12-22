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
package org.openhab.binding.siemenshvac.internal.network;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacRequestHandler {
    private SiemensHvacConnector hvacConnector;

    private int retryCount = 0;

    @Nullable
    private Request request = null;

    @Nullable
    private Response response = null;

    @Nullable
    private Result result = null;

    private Instant startRequest;

    /**
     * Callback to execute on complete response
     */
    private final SiemensHvacCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public SiemensHvacRequestHandler(SiemensHvacCallback callback, SiemensHvacConnector hvacConnector) {
        this.callback = callback;
        this.hvacConnector = hvacConnector;
        startRequest = Instant.now();
    }

    public SiemensHvacConnector getHvacConnector() {
        return hvacConnector;
    }

    public SiemensHvacCallback getCallback() {
        return callback;
    }

    public void incrementRetryCount() {
        retryCount++;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @Nullable
    public Response getResponse() {
        return response;
    }

    @Nullable
    public Request getRequest() {
        return request;
    }

    @Nullable
    public Result getResult() {
        return result;
    }

    public void setResponse(@Nullable Response response) {
        this.response = response;
    }

    public void setRequest(@Nullable Request request) {
        this.request = request;
    }

    public void setResult(@Nullable Result result) {
        this.result = result;
    }

    public long getElapsedTime() {
        Instant finish = Instant.now();
        return Duration.between(startRequest, finish).toSeconds();
    }
}
