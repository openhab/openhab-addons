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
package org.openhab.binding.tradfri.internal;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tradfri.internal.handler.TradfriAirPurifierHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriBlindHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriControllerHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriGatewayHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriLightHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriPlugHandler;
import org.openhab.binding.tradfri.internal.handler.TradfriSensorHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TradfriHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Added support for remote controller and motion sensor devices (read-only battery level)
 * @author Manuel Raffel - Added support for blinds
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tradfri")
@NonNullByDefault
public class TradfriHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (GATEWAY_TYPE_UID.equals(thingTypeUID)) {
            return new TradfriGatewayHandler((Bridge) thing);
        } else if (THING_TYPE_DIMMER.equals(thingTypeUID) || THING_TYPE_REMOTE_CONTROL.equals(thingTypeUID)
                || THING_TYPE_OPEN_CLOSE_REMOTE_CONTROL.equals(thingTypeUID)) {
            return new TradfriControllerHandler(thing);
        } else if (THING_TYPE_MOTION_SENSOR.equals(thingTypeUID)) {
            return new TradfriSensorHandler(thing);
        } else if (THING_TYPE_BLINDS.equals(thingTypeUID)) {
            return new TradfriBlindHandler(thing);
        } else if (SUPPORTED_LIGHT_TYPES_UIDS.contains(thingTypeUID)) {
            return new TradfriLightHandler(thing);
        } else if (SUPPORTED_PLUG_TYPES_UIDS.contains(thingTypeUID)) {
            return new TradfriPlugHandler(thing);
        } else if (THING_TYPE_AIR_PURIFIER.equals(thingTypeUID)) {
            return new TradfriAirPurifierHandler(thing);
        }
        return null;
    }
}
