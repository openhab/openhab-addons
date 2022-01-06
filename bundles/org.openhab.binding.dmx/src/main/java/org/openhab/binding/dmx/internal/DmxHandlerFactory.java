/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
public class DmxHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DmxBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_ARTNET_BRIDGE)) {
            ArtnetBridgeHandler handler = new ArtnetBridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_LIB485_BRIDGE)) {
            Lib485BridgeHandler handler = new Lib485BridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_SACN_BRIDGE)) {
            SacnBridgeHandler handler = new SacnBridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            DimmerThingHandler handler = new DimmerThingHandler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_COLOR)) {
            ColorThingHandler handler = new ColorThingHandler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_TUNABLEWHITE)) {
            TunableWhiteThingHandler handler = new TunableWhiteThingHandler(thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CHASER)) {
            ChaserThingHandler handler = new ChaserThingHandler(thing);
            return handler;
        }
        return null;
    }
}
