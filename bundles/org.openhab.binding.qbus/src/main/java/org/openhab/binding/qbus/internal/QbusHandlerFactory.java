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
package org.openhab.binding.qbus.internal;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.handler.QbusBistabielHandler;
import org.openhab.binding.qbus.internal.handler.QbusCO2Handler;
import org.openhab.binding.qbus.internal.handler.QbusDimmerHandler;
import org.openhab.binding.qbus.internal.handler.QbusRolHandler;
import org.openhab.binding.qbus.internal.handler.QbusSceneHandler;
import org.openhab.binding.qbus.internal.handler.QbusThermostatHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link QbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.qbus")
@NonNullByDefault
public class QbusHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (BRIDGE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusBridgeHandler((Bridge) thing);
        } else if (SCENE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusSceneHandler(thing);
        } else if (BISTABIEL_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusBistabielHandler(thing);
        } else if (THERMOSTAT_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusThermostatHandler(thing);
        } else if (DIMMER_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusDimmerHandler(thing);
        } else if (CO2_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusCO2Handler(thing);
        } else if (ROLLERSHUTTER_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusRolHandler(thing);
        } else if (ROLLERSHUTTER_SLATS_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new QbusRolHandler(thing);
        }

        return null;
    }
}
