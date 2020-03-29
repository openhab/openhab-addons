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
package org.openhab.binding.dwdpollenflug.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.net.URI;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflugindex;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDRegion;

/**
 * The {@link DWDPollenflugBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDDataAccess {
    private static final String DWD_URL = "https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json";

    private final HttpClient client;
    private final Gson gson;

    public DWDDataAccess(HttpClient client) {
        this.client = client;
        this.gson = new Gson();
    }

    public CompletableFuture<DWDRegion> refresh(int regionID) {
        final CompletableFuture<DWDRegion> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(DWD_URL));

        request.method(HttpMethod.GET).timeout(2000, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    Throwable e = result.getFailure();
                    if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                        f.completeExceptionally(new DWDDataException("Request timeout", e));
                    } else {
                        f.completeExceptionally(new DWDDataException("Request failed", e));
                    }
                } else if (response.getStatus() != 200) {
                    f.completeExceptionally(new DWDDataException(getContentAsString()));
                } else {
                    DWDPollenflugindex pollenflugindex = gson.fromJson(getContentAsString(), DWDPollenflugindex.class);
                    if (pollenflugindex == null) {
                        f.completeExceptionally(new DWDDataException("Parsing failed"));
                    } else {
                        pollenflugindex.init();
                        f.complete(pollenflugindex.getRegion(regionID));
                    }
                }
            }
        });

        return f;
    }
}
