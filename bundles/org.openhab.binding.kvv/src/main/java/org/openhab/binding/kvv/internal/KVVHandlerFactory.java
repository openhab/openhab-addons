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
package org.openhab.binding.kvv.internal;

import static org.openhab.binding.kvv.internal.KVVBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.kvv.internal.KVVBindingConstants.THING_TYPE_STOP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link KVVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.kvv", service = ThingHandlerFactory.class)
public class KVVHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return KVVBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new KVVBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_STOP.equals(thingTypeUID)) {
            return new KVVStopHandler(thing);
        }

        return null;
    }
}
