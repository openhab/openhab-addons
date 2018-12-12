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
    /**
     * An exception class describing a failure to parse configuration.
     */
    public static class InvalidConfigurationException extends RuntimeException {
        public InvalidConfigurationException(final String message) {
            super(message);
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

    private HttpMethod commandMethod = DEFAULT_COMMAND_METHOD;
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

    /**
     * Returnes a parsed state request configuration, if one was specified.
     *
     * @param bundleContext the OSGi bundle context
     * @return an optional {@link StateRequest}
     * @throws InvalidConfigurationException if any part of the configuration is not valid
     */
    public Optional<StateRequest> getStateRequest(final BundleContext bundleContext) {
        return Optional.ofNullable(this.stateUrl).map(stateUrl -> {
            final Optional<Transform> stateResponseTransform = parseTransform(
                    bundleContext, "stateResponseTransform", this.stateResponseTransform);
            try {
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
                throw new InvalidConfigurationException("Invalid stateUrl: " + e.getMessage());
            }
        });
    }

    /**
     * Returnes a parsed command request configuration, if one was specified.
     *
     * @param bundleContext the OSGi bundle context
     * @return an optional {@link CommandRequest}
     * @throws InvalidConfigurationException if any part of the configuration is not valid
     */
    public Optional<CommandRequest> getCommandRequest(final BundleContext bundleContext) {
        return Optional.ofNullable(this.commandUrl).map(commandUrl -> {
            final Optional<Transform> commandRequestTransform = parseTransform(
                    bundleContext, "commandRequestTransform", this.commandRequestTransform);
            final Optional<Transform> commandResponseTransform = parseTransform(
                    bundleContext, "commandResponseTransform", this.commandResponseTransform);
            try {
                return new CommandRequest(
                        this.commandMethod,
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
                throw new InvalidConfigurationException("Invalid commandUrl: " + e.getMessage());
            }
        });
    }

    private Optional<Transform> parseTransform(final BundleContext bundleContext,
                                               final String configKeyName,
                                               final String transformStr)
            throws InvalidConfigurationException
    {
        try {
            return Optional.ofNullable(transformStr).map(s -> Transform.parse(bundleContext, s));
        } catch (final IllegalArgumentException e) {
            throw new InvalidConfigurationException(String.format("Invalid %s: %s", configKeyName, e.getMessage()));
        }
    }
}
