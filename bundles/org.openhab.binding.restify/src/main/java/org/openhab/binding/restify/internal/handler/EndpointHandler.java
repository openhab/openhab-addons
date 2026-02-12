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
import org.openhab.binding.restify.internal.JsonSchemaValidator;
import org.openhab.binding.restify.internal.config.ConfigException;
import org.openhab.binding.restify.internal.config.ConfigParser;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.schema.Error;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EndpointHandler.class);

    private final ConfigParser configParser;
    private final DispatcherServlet dispatcherServlet;
    private final JsonSchemaValidator schemaValidator;

    private @Nullable EndpointConfiguration config;

    public EndpointHandler(Thing thing, ConfigParser configParser, DispatcherServlet dispatcherServlet,
            JsonSchemaValidator schemaValidator) {
        super(thing);
        this.configParser = configParser;
        this.dispatcherServlet = dispatcherServlet;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (ConfigException ex) {
            logger.error("Wrong config!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Wrong config!");
        } catch (InitializationException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, ex.translationKey());
        } catch (Exception ex) {
            logger.error("Cannot initialize handler: {}", ex.getMessage(), ex);
            updateStatus(OFFLINE, HANDLER_INITIALIZING_ERROR, ex.getLocalizedMessage());
        }
    }

    private void internalInitialize() throws ConfigException, InitializationException {
        config = getConfigAs(EndpointConfiguration.class);
        if (!config.path.startsWith("/")) {
            throw new InitializationException("thing-type.restify.%s.path".formatted(THING_TYPE_ENDPOINT.getId()),
                    config.path);
        }

        var errors = schemaValidator.validateEndpointConfig(config.endpoint);
        if (!errors.isEmpty()) {
            var errorMessages = errors.stream().map(Error::getMessage).toList();
            throw new InitializationException("thing-type.restify.%s.config".formatted(THING_TYPE_ENDPOINT.getId()),
                    String.join(", ", errorMessages));
        }

        var response = configParser.parseEndpointConfig(config.endpoint);
        dispatcherServlet.register(config.path, config.method, response);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // todo create channels and handle REFRESH command
    }

    @Override
    public void dispose() {
        var localConfig = config;
        config = null;
        if (localConfig != null) {
            dispatcherServlet.unregister(localConfig.path, localConfig.method);
        }
        super.dispose();
    }
}
