/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal;

import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dmx.internal.handler.ArtnetBridgeHandler;
import org.openhab.binding.dmx.internal.handler.ChaserThingHandler;
import org.openhab.binding.dmx.internal.handler.ColorThingHandler;
import org.openhab.binding.dmx.internal.handler.DimmerThingHandler;
import org.openhab.binding.dmx.internal.handler.Lib485BridgeHandler;
import org.openhab.binding.dmx.internal.handler.SacnBridgeHandler;
import org.openhab.binding.dmx.internal.handler.TunableWhiteThingHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DmxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.dmx")
@NonNullByDefault
public class DmxHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(ArtnetBridgeHandler.SUPPORTED_THING_TYPES, Lib485BridgeHandler.SUPPORTED_THING_TYPES,
                    SacnBridgeHandler.SUPPORTED_THING_TYPES, ChaserThingHandler.SUPPORTED_THING_TYPES,
                    ColorThingHandler.SUPPORTED_THING_TYPES, DimmerThingHandler.SUPPORTED_THING_TYPES,
                    TunableWhiteThingHandler.SUPPORTED_THING_TYPES)
            .flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_ARTNET_BRIDGE)) {
            return new ArtnetBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIB485_BRIDGE)) {
            return new Lib485BridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_SACN_BRIDGE)) {
            return new SacnBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            return new DimmerThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_COLOR)) {
            return new ColorThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TUNABLEWHITE)) {
            return new TunableWhiteThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CHASER)) {
            return new ChaserThingHandler(thing);
        }
        return null;
    }
}
