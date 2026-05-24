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
package org.openhab.binding.restify.internal.handler;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.THING_TYPE_ENDPOINT;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.endpoint.Endpoint;
import org.openhab.binding.restify.internal.endpoint.RegistrationException;
import org.openhab.binding.restify.internal.handler.EndpointConfiguration.AuthorizationType;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method;
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

    private @Nullable String registeredPath;
    private @Nullable Method registeredMethod;

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
        var path = trimToEmpty(localConfig.path);
        var method = localConfig.method;
        if (!path.startsWith("/")) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.path.invalid".formatted(THING_TYPE_ENDPOINT.getId()), path);
        }
        if (path.equals("/")) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.path.only-root".formatted(THING_TYPE_ENDPOINT.getId()));
        }
        if (path.startsWith("/_")) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.path.reserved-prefix".formatted(THING_TYPE_ENDPOINT.getId()), path);
        }

        if (method == null) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.method.invalid".formatted(THING_TYPE_ENDPOINT.getId()));
        }

        var transformation = new ChannelTransformation(localConfig.transformationPattern);
        if (transformation.isEmpty()) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.transformationPattern.invalid".formatted(THING_TYPE_ENDPOINT.getId()),
                    String.valueOf(localConfig.transformationPattern));
        }

        var authorization = createAuthorization(localConfig);
        var contentType = resolveContentType(localConfig.contentType);
        var endpoint = new Endpoint(authorization, transformation, contentType);
        try {
            dispatcherServlet.register(path, method, endpoint);
        } catch (RegistrationException ex) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.endpoint.duplicate".formatted(THING_TYPE_ENDPOINT.getId()),
                    method.name(), path);
        }
        registeredPath = path;
        registeredMethod = method;
        updateStatus(ThingStatus.ONLINE);
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
                    "thing-type.config.restify.%s.authorization.basic.invalid".formatted(THING_TYPE_ENDPOINT.getId()));
        }
        return new Authorization.Basic(username, password);
    }

    private Authorization.Bearer createBearerAuthorization(EndpointConfiguration localConfig)
            throws InitializationException {
        var token = trimToEmpty(localConfig.token);
        if (token.isBlank()) {
            throw new InitializationException(
                    "thing-type.config.restify.%s.authorization.bearer.invalid".formatted(THING_TYPE_ENDPOINT.getId()));
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        var localRegisteredPath = registeredPath;
        var localRegisteredMethod = registeredMethod;
        registeredPath = null;
        registeredMethod = null;
        if (localRegisteredPath != null && localRegisteredMethod != null) {
            dispatcherServlet.unregister(localRegisteredPath, localRegisteredMethod);
        }
        super.dispose();
    }
}
