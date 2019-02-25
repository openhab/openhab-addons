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
import org.osgi.framework.BundleContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * A class describing configuration for channels attached to a HTTP Endpoint Thing.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
public class HttpChannelConfig {
    @SuppressWarnings("unused")
    private @Nullable String urlSuffix;

    @SuppressWarnings("unused")
    private @Nullable String stateResponseTransform;

    @SuppressWarnings("unused")
    private @Nullable String commandRequestTransform;
    @SuppressWarnings("unused")
    private @Nullable String commandResponseTransform;

    /**
     * Returnes a parsed state request configuration, if one was specified.
     *
     * @param endpointConfig the main Thing config
     * @param bundleContext the OSGi bundle context
     * @return an optional {@link StateRequest}
     * @throws InvalidConfigurationException if any part of the configuration is not valid
     */
    public Optional<StateRequest> getStateRequest(final HttpEndpointConfig endpointConfig, final BundleContext bundleContext) {
        return Optional.ofNullable(endpointConfig.getBaseUrl())
                .map(urlStr -> urlStr + Optional.ofNullable(this.urlSuffix).orElse(""))
                .map(stateUrl -> {
                    final Optional<Transform> stateResponseTransform = parseTransform(
                            bundleContext, "stateResponseTransform", this.stateResponseTransform);
                    try {
                        return new StateRequest(
                                new URL(stateUrl),
                                Optional.ofNullable(endpointConfig.getUsername()),
                                Optional.ofNullable(endpointConfig.getPassword()),
                                Duration.ofMillis(endpointConfig.getConnectTimeout()),
                                Duration.ofMillis(endpointConfig.getRequestTimeout()),
                                Duration.ofMillis(endpointConfig.getStateRefreshInterval()),
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
     * @param endpointConfig the main Thing config
     * @param bundleContext the OSGi bundle context
     * @return an optional {@link CommandRequest}
     * @throws InvalidConfigurationException if any part of the configuration is not valid
     */
    public Optional<CommandRequest> getCommandRequest(final HttpEndpointConfig endpointConfig, final BundleContext bundleContext) {
        return Optional.ofNullable(endpointConfig.getBaseUrl())
                .map(urlStr -> urlStr + Optional.ofNullable(this.urlSuffix).orElse(""))
                .map(commandUrl -> {
                    final Optional<Transform> commandRequestTransform = parseTransform(
                            bundleContext, "commandRequestTransform", this.commandRequestTransform);
                    final Optional<Transform> commandResponseTransform = parseTransform(
                            bundleContext, "commandResponseTransform", this.commandResponseTransform);
                    try {
                        return new CommandRequest(
                                endpointConfig.getCommandMethod(),
                                new URL(commandUrl),
                                Optional.ofNullable(endpointConfig.getUsername()),
                                Optional.ofNullable(endpointConfig.getPassword()),
                                Duration.ofMillis(endpointConfig.getConnectTimeout()),
                                Duration.ofMillis(endpointConfig.getRequestTimeout()),
                                endpointConfig.getCommandContentType(),
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
