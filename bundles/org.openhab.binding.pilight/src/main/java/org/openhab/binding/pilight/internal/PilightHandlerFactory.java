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
package org.openhab.binding.pilight.internal;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.handler.PilightBridgeHandler;
import org.openhab.binding.pilight.internal.handler.PilightContactHandler;
import org.openhab.binding.pilight.internal.handler.PilightDimmerHandler;
import org.openhab.binding.pilight.internal.handler.PilightGenericHandler;
import org.openhab.binding.pilight.internal.handler.PilightSwitchHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PilightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
@Component(configurationPid = "binding.pilight", service = ThingHandlerFactory.class)
public class PilightHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_CONTACT,
            THING_TYPE_DIMMER, THING_TYPE_GENERIC, THING_TYPE_SWITCH);

    private final ChannelTypeRegistry channelTypeRegistry;

    @Activate
    public PilightHandlerFactory(@Reference ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new PilightBridgeHandler((Bridge) thing);
        }

        if (THING_TYPE_CONTACT.equals(thingTypeUID)) {
            return new PilightContactHandler(thing);
        }

        if (THING_TYPE_DIMMER.equals(thingTypeUID)) {
            return new PilightDimmerHandler(thing);
        }

        if (THING_TYPE_GENERIC.equals(thingTypeUID)) {
            return new PilightGenericHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_SWITCH.equals(thingTypeUID)) {
            return new PilightSwitchHandler(thing);
        }

        return null;
    }
}
