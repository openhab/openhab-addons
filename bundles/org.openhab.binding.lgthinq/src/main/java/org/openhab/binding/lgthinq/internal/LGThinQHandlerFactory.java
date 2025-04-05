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
package org.openhab.binding.lgthinq.internal;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.handler.LGThinQAirConditionerHandler;
import org.openhab.binding.lgthinq.internal.handler.LGThinQBridgeHandler;
import org.openhab.binding.lgthinq.internal.handler.LGThinQDishWasherHandler;
import org.openhab.binding.lgthinq.internal.handler.LGThinQFridgeHandler;
import org.openhab.binding.lgthinq.internal.handler.LGThinQWasherDryerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible for creating {@link ThingHandler} instances for LG ThinQ devices.
 * This factory supports various appliance types and maps each to its corresponding handler.
 * It extends {@link BaseThingHandlerFactory} and implements {@link ThingHandlerFactory}.
 *
 * <p>
 * Supported device types include:
 * </p>
 * <ul>
 * <li>Air Conditioners</li>
 * <li>Heat Pumps</li>
 * <li>Washing Machines & Towers</li>
 * <li>Dryers & Dryer Towers</li>
 * <li>Refrigerators</li>
 * <li>Dishwashers</li>
 * <li>Bridges</li>
 * </ul>
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lgthinq")
public class LGThinQHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LGThinQHandlerFactory.class);

    private final HttpClientFactory httpClientFactory;
    private final LGThinQStateDescriptionProvider stateDescriptionProvider;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    /**
     * Constructs the handler factory with required dependencies.
     *
     * @param stateDescriptionProvider Provides state descriptions for ThinQ devices.
     * @param httpClientFactory Handles HTTP requests for API communication.
     * @param itemChannelLinkRegistry Manages item-channel links for device integration.
     */
    @Activate
    public LGThinQHandlerFactory(@Reference LGThinQStateDescriptionProvider stateDescriptionProvider,
            @Reference HttpClientFactory httpClientFactory,
            @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpClientFactory = httpClientFactory;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    /**
     * Determines whether this factory supports the given {@link ThingTypeUID}.
     *
     * @param thingTypeUID The Thing type to check.
     * @return {@code true} if the type is supported, {@code false} otherwise.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates the appropriate {@link ThingHandler} for the specified {@link Thing}.
     *
     * @param thing The Thing to create a handler for.
     * @return The corresponding handler instance, or {@code null} if the type is unsupported.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID) || THING_TYPE_HEAT_PUMP.equals(thingTypeUID)) {
            return new LGThinQAirConditionerHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new LGThinQBridgeHandler((Bridge) thing, httpClientFactory);
        } else if (THING_TYPE_WASHING_MACHINE.equals(thingTypeUID) || THING_TYPE_WASHING_TOWER.equals(thingTypeUID)
                || THING_TYPE_DRYER.equals(thingTypeUID) || THING_TYPE_DRYER_TOWER.equals(thingTypeUID)) {
            return new LGThinQWasherDryerHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_FRIDGE.equals(thingTypeUID)) {
            return new LGThinQFridgeHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_DISHWASHER.equals(thingTypeUID)) {
            return new LGThinQDishWasherHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        }

        logger.warn("Unsupported Thing type: {}", thingTypeUID.getId());
        return null;
    }

    /**
     * Creates a new {@link Thing} instance based on its type and configuration.
     *
     * @param thingTypeUID The Thing type.
     * @param configuration The initial configuration.
     * @param thingUID The unique identifier for the Thing (nullable).
     * @param bridgeUID The bridge's UID, if applicable (nullable).
     * @return The created Thing instance, or {@code null} if unsupported.
     */
    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        }

        if (THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID) || THING_TYPE_HEAT_PUMP.equals(thingTypeUID)
                || THING_TYPE_WASHING_MACHINE.equals(thingTypeUID) || THING_TYPE_WASHING_TOWER.equals(thingTypeUID)
                || THING_TYPE_DRYER.equals(thingTypeUID) || THING_TYPE_DRYER_TOWER.equals(thingTypeUID)
                || THING_TYPE_FRIDGE.equals(thingTypeUID) || THING_TYPE_DISHWASHER.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }

        return null;
    }
}
