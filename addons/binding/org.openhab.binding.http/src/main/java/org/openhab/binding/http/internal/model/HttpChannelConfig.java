/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.osgi.framework.BundleContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_COMMAND_METHOD;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_CONNECT_TIMEOUT;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_CONTENT_TYPE;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_REQUEST_TIMEOUT;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_STATE_REFRESH_INTERVAL;

/**
 * A class describing configuration for the HTTP handler.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
public class HttpChannelConfig {
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
            final HttpMethod commandMethod;
            try {
                commandMethod = HttpMethod.valueOf(this.commandMethod);
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
