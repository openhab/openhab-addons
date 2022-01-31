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
package org.openhab.binding.omnilink.internal;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.AudioSourceHandler;
import org.openhab.binding.omnilink.internal.handler.AudioZoneHandler;
import org.openhab.binding.omnilink.internal.handler.ButtonHandler;
import org.openhab.binding.omnilink.internal.handler.ConsoleHandler;
import org.openhab.binding.omnilink.internal.handler.HumiditySensorHandler;
import org.openhab.binding.omnilink.internal.handler.LockHandler;
import org.openhab.binding.omnilink.internal.handler.LuminaAreaHandler;
import org.openhab.binding.omnilink.internal.handler.OmniAreaHandler;
import org.openhab.binding.omnilink.internal.handler.OmnilinkBridgeHandler;
import org.openhab.binding.omnilink.internal.handler.TempSensorHandler;
import org.openhab.binding.omnilink.internal.handler.ThermostatHandler;
import org.openhab.binding.omnilink.internal.handler.UnitHandler;
import org.openhab.binding.omnilink.internal.handler.ZoneHandler;
import org.openhab.binding.omnilink.internal.handler.units.DimmableUnitHandler;
import org.openhab.binding.omnilink.internal.handler.units.FlagHandler;
import org.openhab.binding.omnilink.internal.handler.units.OutputHandler;
import org.openhab.binding.omnilink.internal.handler.units.UpbRoomHandler;
import org.openhab.binding.omnilink.internal.handler.units.dimmable.UpbUnitHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link OmnilinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.omnilink")
public class OmnilinkHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_AUDIO_SOURCE)) {
            return new AudioSourceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AUDIO_ZONE)) {
            return new AudioZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new OmnilinkBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_BUTTON)) {
            return new ButtonHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CONSOLE)) {
            return new ConsoleHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIMMABLE)) {
            return new DimmableUnitHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FLAG)) {
            return new FlagHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HUMIDITY_SENSOR)) {
            return new HumiditySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LOCK)) {
            return new LockHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LUMINA_AREA)) {
            return new LuminaAreaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OMNI_AREA)) {
            return new OmniAreaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OUTPUT)) {
            return new OutputHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROOM)) {
            return new UpbRoomHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMP_SENSOR)) {
            return new TempSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new ThermostatHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_UNIT)) {
            return new UnitHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_UNIT_UPB)) {
            return new UpbUnitHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new ZoneHandler(thing);
        } else {
            return null;
        }
    }
}
