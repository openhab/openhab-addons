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

    private @Nullable Thing bridgeThing = null;
    private @Nullable HttpClientFactory httpClientFactory = null;

    private @Nullable HttpClient httpClient = null;
    private @Nullable OAuthTokenRefresher openHabOAuthTokenRefresher = null;

    private @NonNull OnectaConnectionClient onectaConnectionClient;
    private @Nullable OnectaTranslationProvider translation;

    public OnectaConfiguration(HttpClientFactory httpClientFactory, OAuthTokenRefresher openHabOAuthTokenRefresher,
            OnectaTranslationProvider translation) {
        this.httpClientFactory = httpClientFactory;
        this.openHabOAuthTokenRefresher = openHabOAuthTokenRefresher;
        this.translation = translation;
        this.onectaConnectionClient = new OnectaConnectionClient(this);
    }

    public void setTranslation(OnectaTranslationProvider translationPar) {
        translation = translationPar;
    }

    public OnectaTranslationProvider getTranslation() {
        Optional<OnectaTranslationProvider> optionalTranslation = Optional.ofNullable(translation);
        return optionalTranslation.orElseThrow(() -> new RuntimeException("Translation provider is not available"));
    }

    public @Nullable HttpClient getHttpClient() {
        return httpClientFactory.getCommonHttpClient();
    }

    public @Nullable HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    public OnectaConnectionClient getOnectaConnectionClient() {
        return onectaConnectionClient;
    }

    public @Nullable OAuthTokenRefresher getOAuthTokenRefresher() {
        return openHabOAuthTokenRefresher;
    }

    public void setBridgeThing(Thing bridgeThing) {
        this.bridgeThing = bridgeThing;
    }
}
