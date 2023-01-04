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
package org.openhab.binding.luxom.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.handler.LuxomBridgeHandler;
import org.openhab.binding.luxom.internal.handler.LuxomDimmerHandler;
import org.openhab.binding.luxom.internal.handler.LuxomSwitchHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link LuxomHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.luxom", service = ThingHandlerFactory.class)
public class LuxomHandlerFactory extends BaseThingHandlerFactory {
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return LuxomBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (LuxomBindingConstants.BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
            return new LuxomBridgeHandler((Bridge) thing);
        } else if (LuxomBindingConstants.THING_TYPE_SWITCH.equals(thing.getThingTypeUID())) {
            return new LuxomSwitchHandler(thing);
        } else if (LuxomBindingConstants.THING_TYPE_DIMMER.equals(thing.getThingTypeUID())) {
            return new LuxomDimmerHandler(thing);
        }

        return null;
    }
}
