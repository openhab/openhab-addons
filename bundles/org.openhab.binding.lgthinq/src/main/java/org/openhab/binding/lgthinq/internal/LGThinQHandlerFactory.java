/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.lgthinq.internal.handler.*;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelGroupTypeProvider;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelTypeProvider;
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
 * The {@link LGThinQHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.lgthinq")
public class LGThinQHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LGThinQHandlerFactory.class);
    
	private HttpClientFactory httpClientFactory;
	
    private final LGThinQStateDescriptionProvider stateDescriptionProvider;

    @Nullable
    @Reference
    protected ThinqChannelTypeProvider thinqChannelProvider;
    @Nullable
    @Reference
    protected ThinqChannelGroupTypeProvider thinqChannelGroupProvider;
    @Nullable
    @Reference
    protected ItemChannelLinkRegistry itemChannelLinkRegistry;

	@Activate
	public LGThinQHandlerFactory(final @Reference LGThinQStateDescriptionProvider stateDescriptionProvider,
			@Reference final HttpClientFactory httpClientFactory) {
		this.stateDescriptionProvider = stateDescriptionProvider;
		this.httpClientFactory = httpClientFactory;
	}

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return LGThinQBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID) || THING_TYPE_HEAT_PUMP.equals(thingTypeUID)) {
            return new LGThinQAirConditionerHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new LGThinQBridgeHandler((Bridge) thing, httpClientFactory);
        } else if (THING_TYPE_WASHING_MACHINE.equals(thingTypeUID) || THING_TYPE_WASHING_TOWER.equals(thingTypeUID)) {
            return new LGThinQWasherDryerHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(thinqChannelProvider), Objects.requireNonNull(thinqChannelGroupProvider),
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_DRYER.equals(thingTypeUID) || THING_TYPE_DRYER_TOWER.equals(thingTypeUID)) {
            return new LGThinQWasherDryerHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(thinqChannelProvider), Objects.requireNonNull(thinqChannelGroupProvider),
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        } else if (THING_TYPE_FRIDGE.equals(thingTypeUID)) {
            return new LGThinQFridgeHandler(thing, stateDescriptionProvider,
                    Objects.requireNonNull(itemChannelLinkRegistry), httpClientFactory);
        }
        logger.error("Thing not supported by this Factory: {}", thingTypeUID.getId());
        return null;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID) || THING_TYPE_HEAT_PUMP.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else if (THING_TYPE_WASHING_MACHINE.equals(thingTypeUID) || THING_TYPE_WASHING_TOWER.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else if (THING_TYPE_DRYER.equals(thingTypeUID) || THING_TYPE_DRYER_TOWER.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else if (THING_TYPE_FRIDGE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        return null;
    }

}
