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
package org.openhab.binding.onecta.internal;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefresher;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;

/**
 * The {@link OnectaConfiguration} class contains global static variables, which are used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaConfiguration {

    private @Nullable static Thing bridgeThing = null;
    private @Nullable static HttpClientFactory httpClientFactory = null;

    private @Nullable static HttpClient httpClient = null;
    private @Nullable static OAuthTokenRefresher openHabOAuthTokenRefresher = null;

    private @NonNull static OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();
    private @Nullable static OnectaTranslationProvider translation;

    public static void setTranslation(OnectaTranslationProvider translationPar) {
        translation = translationPar;
    }

    public static OnectaTranslationProvider getTranslation() {
        Optional<OnectaTranslationProvider> optionalTranslation = Optional.ofNullable(translation);
        return optionalTranslation.orElseThrow(() -> new RuntimeException("Translation provider is not available"));
    }

    public static void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        OnectaConfiguration.httpClientFactory = httpClientFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    public static void setBridgeThing(Thing bridgeThing) {
        OnectaConfiguration.bridgeThing = bridgeThing;
    }

    public static @Nullable HttpClient getHttpClient() {
        return httpClient;
    }

    public static @Nullable HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    public static void setOAuthTokenRefresher(OAuthTokenRefresher openHabOAuthTokenRefresher) {
        OnectaConfiguration.openHabOAuthTokenRefresher = openHabOAuthTokenRefresher;
    }

    public static OnectaConnectionClient getOnectaConnectionClient() {
        return onectaConnectionClient;
    }

    public static @Nullable OAuthTokenRefresher getOAuthTokenRefresher() {
        return openHabOAuthTokenRefresher;
    }
}
