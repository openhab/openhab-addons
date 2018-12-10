/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_COMMAND_METHOD;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_CONNECT_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_CONTENT_TYPE;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_REQUEST_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_STATE_REFRESH_INTERVAL;

/**
 * A class describing configuration for the HTTP handler.
 *
 * @author Brian J. Tarricone
 */
@NonNullByDefault
public class HttpChannelConfig {
    /**
     * Enumeration describing the HTTP method.
     */
    public enum Method {
        POST,
        GET,
        @SuppressWarnings("unused")
        PUT
    }

    /**
     * A class describing configuration for the HTTP request to make when fetching {@link State}.
     */
    public static class StateRequest {
        private final URL url;
        private final Optional<String> username;
        private final Optional<String> password;
        private final Duration connectTimeout;
        private final Duration requestTimeout;
        private final Duration refreshInterval;
        private final Optional<Transform> responseTransform;

        StateRequest(final URL url,
                     final Optional<String> username,
                     final Optional<String> password,
                     final Duration connectTimeout,
                     final Duration requestTimeout,
                     final Duration refreshInterval,
                     final Optional<Transform> responseTransform)
        {
            this.url = url;
            this.username = username;
            this.password = password;
            this.connectTimeout = connectTimeout;
            this.requestTimeout = requestTimeout;
            this.refreshInterval = refreshInterval;
            this.responseTransform = responseTransform;
        }

        public URL getUrl() {
            return url;
        }

        public Optional<String> getUsername() {
            return username;
        }

        public Optional<String> getPassword() {
            return password;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public Duration getRequestTimeout() {
            return requestTimeout;
        }

        public Duration getRefreshInterval() {
            return refreshInterval;
        }

        public Optional<Transform> getResponseTransform() {
            return responseTransform;
        }
    }

    /**
     * A class describing configuration for the HTTP request to make when sending a {@link Command}.
     */
    public static class CommandRequest {
        private final Method method;
        private final URL url;
        private final Optional<String> username;
        private final Optional<String> password;
        private final Duration connectTimeout;
        private final Duration requestTimeout;
        private final String contentType;
        private final Optional<Transform> requestTransform;
        private final Optional<Transform> responseTransform;

        CommandRequest(final Method method,
                       final URL url,
                       final Optional<String> username,
                       final Optional<String> password,
                       final Duration connectTimeout,
                       final Duration requestTimeout,
                       final String contentType,
                       final Optional<Transform> requestTransform,
                       final Optional<Transform> responseTransform)
        {
            this.method = method;
            this.url = url;
            this.username = username;
            this.password = password;
            this.connectTimeout = connectTimeout;
            this.requestTimeout = requestTimeout;
            this.contentType = contentType;
            this.requestTransform = requestTransform;
            this.responseTransform = responseTransform;
        }

        public Method getMethod() {
            return method;
        }

        public URL getUrl() {
            return url;
        }

        public Optional<String> getUsername() {
            return username;
        }

        public Optional<String> getPassword() {
            return password;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public Duration getRequestTimeout() {
            return requestTimeout;
        }

        public String getContentType() {
            return contentType;
        }

        public Optional<Transform> getRequestTransform() {
            return requestTransform;
        }

        public Optional<Transform> getResponseTransform() {
            return responseTransform;
        }
    }

    @SuppressWarnings("unused")
    private @Nullable String stateUrl;
    @SuppressWarnings("unused")
    private @Nullable String stateUsername;
    @SuppressWarnings("unused")
    private @Nullable String statePassword;
    private long stateRefreshInterval = DEFAULT_STATE_REFRESH_INTERVAL.toMillis();
    private long stateConnectTimeout = DEFAULT_CONNECT_TIMEOUT.toMillis();
    private long stateRequestTimeout = DEFAULT_REQUEST_TIMEOUT.toMillis();
    @SuppressWarnings("unused")
    private @Nullable String stateResponseTransform;

    private String commandMethod = DEFAULT_COMMAND_METHOD.name();
    @SuppressWarnings("unused")
    private @Nullable String commandUrl;
    @SuppressWarnings("unused")
    private @Nullable String commandUsername;
    @SuppressWarnings("unused")
    private @Nullable String commandPassword;
    private long commandConnectTimeout = DEFAULT_CONNECT_TIMEOUT.toMillis();
    private long commandRequestTimeout = DEFAULT_REQUEST_TIMEOUT.toMillis();
    private String commandContentType = DEFAULT_CONTENT_TYPE;
    @SuppressWarnings("unused")
    private @Nullable String commandRequestTransform;
    @SuppressWarnings("unused")
    private @Nullable String commandResponseTransform;

    public Optional<StateRequest> getStateRequest(final BundleContext bundleContext) throws IllegalArgumentException {
        return Optional.ofNullable(this.stateUrl).map(stateUrl -> {
            try {
                final Optional<Transform> stateResponseTransform = Optional.ofNullable(this.stateResponseTransform)
                        .map(s -> Transform.parse(bundleContext, s));
                return new StateRequest(
                        new URL(stateUrl),
                        Optional.ofNullable(stateUsername),
                        Optional.ofNullable(statePassword),
                        Duration.ofMillis(this.stateConnectTimeout),
                        Duration.ofMillis(this.stateRequestTimeout),
                        Duration.ofMillis(this.stateRefreshInterval),
                        stateResponseTransform
                );
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid stateUrl: " + e.getMessage());
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid stateResponseTransform: " + e.getMessage(), e);
            }
        });
    }

    public Optional<CommandRequest> getCommandRequest(final BundleContext bundleContext) throws IllegalArgumentException {
        return Optional.ofNullable(this.commandUrl).map(commandUrl -> {
            final Method commandMethod;
            try {
                commandMethod = Method.valueOf(this.commandMethod);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandMethod", e);
            }
            final Optional<Transform> commandRequestTransform;
            try {
                commandRequestTransform = Optional.ofNullable(this.commandRequestTransform)
                        .map(s -> Transform.parse(bundleContext, s));
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandRequestTransform: " + e.getMessage());
            }
            final Optional<Transform> commandResponseTransform;
            try {
                commandResponseTransform = Optional.ofNullable(this.commandResponseTransform)
                        .map(s -> Transform.parse(bundleContext, s));
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandResponseTransform: " + e.getMessage());
            }
            try {
                return new CommandRequest(
                        commandMethod,
                        new URL(commandUrl),
                        Optional.ofNullable(commandUsername),
                        Optional.ofNullable(commandPassword),
                        Duration.ofMillis(this.commandConnectTimeout),
                        Duration.ofMillis(this.commandRequestTimeout),
                        this.commandContentType,
                        commandRequestTransform,
                        commandResponseTransform
                );
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid commandUrl: " + e.getMessage());
            }
        });
    }
}
