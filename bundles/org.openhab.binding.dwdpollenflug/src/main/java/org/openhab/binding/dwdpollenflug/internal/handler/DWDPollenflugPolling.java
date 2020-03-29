/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal.handler;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.dwdpollenflug.internal.DWDPollingException;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflug;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflugJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugPolling} polls data from DWD
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugPolling implements Runnable {
    private static final String DWD_URL = "https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json";

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugPolling.class);

    private final DWDPollenflugBridgeHandler bridgeHandler;

    private final HttpClient client;

    private final Gson gson;

    private ReentrantLock pollingLock = new ReentrantLock();

    public DWDPollenflugPolling(DWDPollenflugBridgeHandler bridgeHandler, HttpClient client) {
        this.bridgeHandler = bridgeHandler;
        this.client = client;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        pollingLock.lock();
        logger.debug("Polling.");
        requestRefresh().handle((pollenflug, e) -> {
            if (pollenflug == null) {
                if (e == null) {
                    bridgeHandler.onBridgeCommunicationError();
                } else {
                    bridgeHandler.onBridgeCommunicationError((DWDPollingException) e);
                }
            } else {
                bridgeHandler.onBridgeOnline();
                bridgeHandler.notifyRegionListeners(pollenflug);
            }

            pollingLock.unlock();
            return null;
        });
    }

    private CompletableFuture<DWDPollenflug> requestRefresh() {
        CompletableFuture<DWDPollenflug> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(DWD_URL));

        request.method(HttpMethod.GET).timeout(2000, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    Throwable e = result.getFailure();
                    if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                        f.completeExceptionally(new DWDPollingException("Request timeout", e));
                    } else {
                        f.completeExceptionally(new DWDPollingException("Request failed", e));
                    }
                } else if (response.getStatus() != 200) {
                    f.completeExceptionally(new DWDPollingException(getContentAsString()));
                } else {
                    try {
                        DWDPollenflugJSON pollenflugJSON = gson.fromJson(getContentAsString(), DWDPollenflugJSON.class);
                        f.complete(new DWDPollenflug(pollenflugJSON));
                    } catch (JsonSyntaxException ex2) {
                        f.completeExceptionally(new DWDPollingException("Parsing of response failed"));
                    }
                }
            }
        });

        return f;
    }
}
