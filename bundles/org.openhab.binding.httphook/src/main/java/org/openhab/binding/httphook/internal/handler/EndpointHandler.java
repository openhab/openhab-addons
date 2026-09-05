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
package org.openhab.binding.httphook.internal.handler;

import static org.openhab.binding.httphook.internal.HttpHookBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.httphook.internal.endpoint.Endpoint;
import org.openhab.binding.httphook.internal.endpoint.RegistrationException;
import org.openhab.binding.httphook.internal.handler.EndpointConfiguration.AuthorizationType;
import org.openhab.binding.httphook.internal.servlet.Authorization;
import org.openhab.binding.httphook.internal.servlet.DispatcherServlet;
import org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EndpointHandler.class);

    private final DispatcherServlet dispatcherServlet;

    private final List<RegisteredEndpoint> registeredEndpoints = new ArrayList<>();

    public EndpointHandler(Thing thing, DispatcherServlet dispatcherServlet) {
        super(thing);
        this.dispatcherServlet = dispatcherServlet;
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (InitializationException ex) {
            logger.error("{}", ex.getLocalizedMessage(), ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, ex.translationKey());
        } catch (Exception ex) {
            logger.error("Cannot initialize handler: {}", ex.getMessage(), ex);
            updateStatus(OFFLINE, HANDLER_INITIALIZING_ERROR, ex.getLocalizedMessage());
        }
    }

    private void internalInitialize() throws InitializationException {
        var localConfig = getConfigAs(EndpointConfiguration.class);
        var authorization = createAuthorization(localConfig);
        var endpointChannels = thing.getChannels().stream().filter(this::isResponseChannel).toList();
        if (endpointChannels.isEmpty()) {
            throw new InitializationException(
                    "thing-type.config.httphook.%s.channel.missing".formatted(THING_TYPE_ENDPOINT.getId()));
        }
        try {
            for (var channel : endpointChannels) {
                register(channel, authorization);
            }
        } catch (InitializationException ex) {
            unregisterEndpoints();
            throw ex;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private boolean isResponseChannel(Channel channel) {
        return CHANNEL_TYPE_RESPONSE.equals(channel.getChannelTypeUID());
    }

    private void register(Channel channel, @Nullable Authorization authorization) throws InitializationException {
        var channelConfig = channel.getConfiguration().as(EndpointChannelConfiguration.class);
        var method = channelConfig.method;
        if (method == null) {
            throw new InitializationException(
                    "channel-type.config.httphook.%s.method.invalid".formatted(CHANNEL_TYPE_RESPONSE.getId()));
        }
        var transformation = new ChannelTransformation(channelConfig.transformationPattern);
        if (transformation.isEmpty()) {
            throw new InitializationException("channel-type.config.httphook.%s.transformationPattern.invalid"
                    .formatted(CHANNEL_TYPE_RESPONSE.getId()), String.valueOf(channelConfig.transformationPattern));
        }

        var path = pathFor(channel.getUID());
        var contentType = resolveContentType(channelConfig.contentType);
        var endpoint = new Endpoint(authorization, transformation, contentType);
        try {
            dispatcherServlet.register(path, method, endpoint);
        } catch (RegistrationException ex) {
            throw new InitializationException(
                    "thing-type.config.httphook.%s.endpoint.duplicate".formatted(THING_TYPE_ENDPOINT.getId()),
                    method.name(), path);
        }
        registeredEndpoints.add(new RegisteredEndpoint(path, method));
    }

    private @Nullable Authorization createAuthorization(EndpointConfiguration localConfig)
            throws InitializationException {
        var authorizationType = localConfig.authorizationType;
        if (authorizationType == null) {
            authorizationType = AuthorizationType.NONE;
        }
        return switch (authorizationType) {
            case NONE -> null;
            case BASIC -> createBasicAuthorization(localConfig);
            case BEARER -> createBearerAuthorization(localConfig);
        };
    }

    private Authorization.Basic createBasicAuthorization(EndpointConfiguration localConfig)
            throws InitializationException {
        var username = trimToEmpty(localConfig.username);
        var password = trimToEmpty(localConfig.password);
        if (username.isBlank() || password.isBlank()) {
            throw new InitializationException(
                    "thing-type.config.httphook.%s.authorization.basic.invalid".formatted(THING_TYPE_ENDPOINT.getId()));
        }
        return new Authorization.Basic(username, password);
    }

    private Authorization.Bearer createBearerAuthorization(EndpointConfiguration localConfig)
            throws InitializationException {
        var token = trimToEmpty(localConfig.token);
        if (token.isBlank()) {
            throw new InitializationException("thing-type.config.httphook.%s.authorization.bearer.invalid"
                    .formatted(THING_TYPE_ENDPOINT.getId()));
        }
        return new Authorization.Bearer(token);
    }

    private static String resolveContentType(@Nullable String contentType) {
        var trimmedContentType = trimToEmpty(contentType);
        return trimmedContentType.isBlank() ? "application/json" : trimmedContentType;
    }

    private static String trimToEmpty(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private String pathFor(ChannelUID channelUID) {
        return "/" + thing.getUID().getId() + "/" + channelUID.getId();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        unregisterEndpoints();
        super.dispose();
    }

    private void unregisterEndpoints() {
        registeredEndpoints.forEach(endpoint -> dispatcherServlet.unregister(endpoint.path(), endpoint.method()));
        registeredEndpoints.clear();
    }

    private record RegisteredEndpoint(String path, Method method) {
    }
}
