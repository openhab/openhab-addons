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
package org.openhab.binding.sungrow.internal;

import static org.openhab.binding.sungrow.internal.SungrowBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sungrow.internal.impl.SungrowBridgeHandler;
import org.openhab.binding.sungrow.internal.impl.SungrowPlantHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SungrowHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Kemper - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sungrow", service = ThingHandlerFactory.class)
public class SungrowHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> THING_IDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_PLANT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_IDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PLANT.equals(thingTypeUID)) {
            return new SungrowPlantHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new SungrowBridgeHandler((Bridge) thing);
        } else {
            return null;
        }
    }
}
