/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.gme.internal.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePriceEntry;

/**
 * Manages authentication and token caching for the GME API.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeAuthManager {

    private final GmeApiClient apiClient;
    private final String username;
    private final String password;

    private @Nullable String token;

    public GmeAuthManager(GmeApiClient apiClient, String username, String password) {
        this.apiClient = apiClient;
        this.username = username;
        this.password = password;
    }

    public String getToken() throws InterruptedException, TimeoutException, ExecutionException {
        String currentToken = token;
        if (currentToken != null && !currentToken.isBlank()) {
            return currentToken;
        }

        String newToken = apiClient.authenticate(username, password);
        token = newToken;
        return newToken;
    }

    public List<GmePriceEntry> requestMarketPrices(LocalDate date)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        return requestMarketPrices(date, GmeGranularity.PT60);
    }

    public List<GmePriceEntry> requestMarketPrices(LocalDate date, GmeGranularity granularity)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String currentToken = getToken();

        try {
            return apiClient.requestMarketPrices(date, currentToken, granularity);
        } catch (GmeApiException e) {
            if (!e.isAuthenticationError()) {
                throw e;
            }

            invalidateToken();

            String newToken = getToken();
            return apiClient.requestMarketPrices(date, newToken, granularity);
        }
    }

    public List<GmePriceEntry> requestPun(LocalDate date)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String currentToken = getToken();

        try {
            return apiClient.requestPun(date, currentToken);
        } catch (GmeApiException e) {
            if (!e.isAuthenticationError()) {
                throw e;
            }

            invalidateToken();

            String newToken = getToken();
            return apiClient.requestPun(date, newToken);
        }
    }

    public void invalidateToken() {
        token = null;
    }
}
