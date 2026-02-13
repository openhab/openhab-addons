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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.JsonSchemaValidator;
import org.openhab.binding.restify.internal.endpoint.EndpointParser;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EndpointHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, immediate = true)
public class EndpointHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENDPOINT);

    private final EndpointParser endpointParser;
    private final DispatcherServlet dispatcherServlet;
    private final JsonSchemaValidator schemaValidator;

    @Activate
    public EndpointHandlerFactory(@Reference EndpointParser endpointParser,
            @Reference DispatcherServlet dispatcherServlet, @Reference JsonSchemaValidator schemaValidator) {
        this.endpointParser = endpointParser;
        this.dispatcherServlet = dispatcherServlet;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ENDPOINT.equals(thingTypeUID)) {
            return new EndpointHandler(thing, endpointParser, dispatcherServlet, schemaValidator);
        }

        return null;
    }
}
