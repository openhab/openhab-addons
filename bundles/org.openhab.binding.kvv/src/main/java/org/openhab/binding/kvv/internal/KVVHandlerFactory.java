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
package org.openhab.binding.kvv.internal;

import static org.openhab.binding.kvv.internal.KVVBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.kvv.internal.KVVBindingConstants.THING_TYPE_STATION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KVVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.kvv", service = ThingHandlerFactory.class)
public class KVVHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(KVVHandlerFactory.class);

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return KVVBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new KVVBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_STATION.equals(thingTypeUID)) {
            return new KVVStationHandler(thing);
        }

        return null;
    }
}
