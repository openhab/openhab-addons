/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.BRIDGE_TYPE_NETWORK;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_RADIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.handler.DDWRTDeviceThingHandler;
import org.openhab.binding.ddwrt.internal.handler.DDWRTNetworkBridgeHandler;
import org.openhab.binding.ddwrt.internal.handler.DDWRTRadioThingHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DDWRTHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ddwrt", service = ThingHandlerFactory.class)
public class DDWRTHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_NETWORK.equals(thingTypeUID)) {
            return new DDWRTNetworkBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new DDWRTDeviceThingHandler(thing);
        } else if (THING_TYPE_RADIO.equals(thingTypeUID)) {
            return new DDWRTRadioThingHandler(thing);
        }

        return null;
    }
}
