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
package org.openhab.binding.mikrotik.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.handler.MikrotikInterfaceThingHandler;
import org.openhab.binding.mikrotik.internal.handler.MikrotikRouterosBridgeHandler;
import org.openhab.binding.mikrotik.internal.handler.MikrotikWirelessClientThingHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MikrotikHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mikrotik", service = ThingHandlerFactory.class)
public class MikrotikHandlerFactory extends BaseThingHandlerFactory {
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikRouterosBridgeHandler.supportsThingType(thingTypeUID)
                || MikrotikWirelessClientThingHandler.supportsThingType(thingTypeUID)
                || MikrotikInterfaceThingHandler.supportsThingType(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (MikrotikRouterosBridgeHandler.supportsThingType(thingTypeUID)) {
            return new MikrotikRouterosBridgeHandler((Bridge) thing);
        } else if (MikrotikWirelessClientThingHandler.supportsThingType(thingTypeUID)) {
            return new MikrotikWirelessClientThingHandler(thing);
        } else if (MikrotikInterfaceThingHandler.supportsThingType(thingTypeUID)) {
            return new MikrotikInterfaceThingHandler(thing);
        }
        return null;
    }
}
