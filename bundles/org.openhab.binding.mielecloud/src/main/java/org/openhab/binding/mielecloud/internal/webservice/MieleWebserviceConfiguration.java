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
package org.openhab.binding.mielecloud.internal.webservice;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Represents a webservice configuration.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class MieleWebserviceConfiguration {
    private final HttpClientFactory httpClientFactory;
    private final LanguageProvider languageProvider;
    private final OAuthTokenRefresher tokenRefresher;
    private final String serviceHandle;
    private final ScheduledExecutorService scheduler;

    private MieleWebserviceConfiguration(MieleWebserviceConfigurationBuilder builder) {
        this.httpClientFactory = getOrThrow(builder.httpClientFactory, "httpClientFactory");
        this.languageProvider = getOrThrow(builder.languageProvider, "languageProvider");
        this.tokenRefresher = getOrThrow(builder.tokenRefresher, "tokenRefresher");
        this.serviceHandle = getOrThrow(builder.serviceHandle, "serviceHandle");
        this.scheduler = getOrThrow(builder.scheduler, "scheduler");
    }

    private static <T> T getOrThrow(@Nullable T object, String objectName) {
        if (object == null) {
            throw new IllegalArgumentException(objectName + " must not be null");
        }
        return object;
    }

    /**
     * Gets the factory to use for HttpClient construction.
     */
    public HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    /**
     * Gets the provider for the language to use when making requests to the API.
     */
    public LanguageProvider getLanguageProvider() {
        return languageProvider;
    }

    /**
     * Gets the refresher for OAuth tokens.
     */
    public OAuthTokenRefresher getTokenRefresher() {
        return tokenRefresher;
    }

    /**
     * Gets the handle referring to the OAuth tokens in the framework's persistent storage.
     */
    public String getServiceHandle() {
        return serviceHandle;
    }

    /**
     * Gets the system wide scheduler.
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public static MieleWebserviceConfigurationBuilder builder() {
        return new MieleWebserviceConfigurationBuilder();
    }

    public static final class MieleWebserviceConfigurationBuilder {
        @Nullable
        private HttpClientFactory httpClientFactory;
        @Nullable
        private LanguageProvider languageProvider;
        @Nullable
        private OAuthTokenRefresher tokenRefresher;
        @Nullable
        private String serviceHandle;
        @Nullable
        private ScheduledExecutorService scheduler;

        private MieleWebserviceConfigurationBuilder() {
        }

        public MieleWebserviceConfigurationBuilder withHttpClientFactory(HttpClientFactory httpClientFactory) {
            this.httpClientFactory = httpClientFactory;
            return this;
        }

        public MieleWebserviceConfigurationBuilder withLanguageProvider(LanguageProvider languageProvider) {
            this.languageProvider = languageProvider;
            return this;
        }

        public MieleWebserviceConfigurationBuilder withTokenRefresher(OAuthTokenRefresher tokenRefresher) {
            this.tokenRefresher = tokenRefresher;
            return this;
        }

        public MieleWebserviceConfigurationBuilder withServiceHandle(String serviceHandle) {
            this.serviceHandle = serviceHandle;
            return this;
        }

        public MieleWebserviceConfigurationBuilder withScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public MieleWebserviceConfiguration build() {
            return new MieleWebserviceConfiguration(this);
        }
    }
}
