/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.noolite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.noolite.NooLiteBindingConstants;
import org.openhab.binding.noolite.handler.NooliteHandler;
import org.openhab.binding.noolite.handler.NooliteMTRF64BridgeHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NooliteHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.noolite", service = ThingHandlerFactory.class)
public class NooliteHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NooLiteBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NooLiteBindingConstants.THING_TYPE_BRIDGEMTRF64.equals(thingTypeUID)) {
            NooliteMTRF64BridgeHandler handler = new NooliteMTRF64BridgeHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(NooLiteBindingConstants.THING_TYPE_DEVICE)) {
            return new NooliteHandler(thing);
        }

        return null;
    }
}
