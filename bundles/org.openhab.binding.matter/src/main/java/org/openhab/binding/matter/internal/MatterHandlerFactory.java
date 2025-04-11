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
package org.openhab.binding.matter.internal;

import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_BRIDGE_ENDPOINT;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_CONTROLLER;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_ENDPOINT;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_NODE;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.handler.ControllerHandler;
import org.openhab.binding.matter.internal.handler.EndpointHandler;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.MatterUIDUtils;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, MatterHandlerFactory.class })
public class MatterHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(MatterHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CONTROLLER, THING_TYPE_NODE,
            THING_TYPE_ENDPOINT, THING_TYPE_BRIDGE_ENDPOINT);

    private final MatterStateDescriptionOptionProvider stateDescriptionProvider;
    private final MatterWebsocketService websocketService;
    private final MatterChannelTypeProvider channelGroupTypeProvider;
    private final Set<ControllerHandler> controllers = new HashSet<>();

    @Activate
    public MatterHandlerFactory(@Reference MatterWebsocketService websocketService,
            @Reference MatterStateDescriptionOptionProvider stateDescriptionProvider,
            @Reference MatterChannelTypeProvider channelGroupTypeProvider) {
        this.websocketService = websocketService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        ThingTypeUID baseTypeUID = MatterUIDUtils.baseTypeForThingType(thingTypeUID);
        return SUPPORTED_THING_TYPES_UIDS.contains(baseTypeUID != null ? baseTypeUID : thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            ControllerHandler controllerHandler = new ControllerHandler((Bridge) thing, websocketService);
            controllers.add(controllerHandler);
            return controllerHandler;
        }

        ThingTypeUID baseTypeUID = MatterUIDUtils.baseTypeForThingType(thingTypeUID);
        ThingTypeUID derivedTypeUID = baseTypeUID != null ? baseTypeUID : thingTypeUID;

        if (THING_TYPE_NODE.equals(derivedTypeUID)) {
            return new NodeHandler((Bridge) thing, stateDescriptionProvider, channelGroupTypeProvider);
        }

        if (THING_TYPE_ENDPOINT.equals(derivedTypeUID)) {
            return new EndpointHandler(thing, stateDescriptionProvider, channelGroupTypeProvider);
        }

        // TODO remove this once we move users to the new endpoint thing type
        if (THING_TYPE_BRIDGE_ENDPOINT.equals(derivedTypeUID)) {
            logger.warn(
                    "IMPORTANT: 'bridge-endpoint' is deprecated, use 'endpoint' instead.  This will be removed in a future release.");
            return new EndpointHandler(thing, stateDescriptionProvider, channelGroupTypeProvider);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ControllerHandler) {
            controllers.remove(thingHandler);
        }
        super.removeHandler(thingHandler);
    }

    public Set<ControllerHandler> getControllers() {
        return controllers;
    }
}
