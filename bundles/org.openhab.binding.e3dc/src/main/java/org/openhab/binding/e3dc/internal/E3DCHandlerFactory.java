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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.handler.E3DCBatteryHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCBatteryModuleHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCBridgeHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCPowerMeterHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCPowerPlantHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCWallboxHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * Creates the handlers of this binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.e3dc", service = ThingHandlerFactory.class)
public class E3DCHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return E3DCBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (E3DCBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new E3DCBridgeHandler(bridge);
        }
        if (E3DCBindingConstants.THING_TYPE_POWER_PLANT.equals(thingTypeUID)) {
            return new E3DCPowerPlantHandler(thing);
        }
        if (E3DCBindingConstants.THING_TYPE_WALLBOX.equals(thingTypeUID)) {
            return new E3DCWallboxHandler(thing);
        }
        if (E3DCBindingConstants.THING_TYPE_POWER_METER.equals(thingTypeUID)) {
            return new E3DCPowerMeterHandler(thing);
        }
        if (E3DCBindingConstants.THING_TYPE_BATTERY.equals(thingTypeUID)) {
            return new E3DCBatteryHandler(thing);
        }
        if (E3DCBindingConstants.THING_TYPE_BATTERY_MODULE.equals(thingTypeUID)) {
            return new E3DCBatteryModuleHandler(thing);
        }
        return null;
    }
}
