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
package org.openhab.binding.onewire.internal;

import static org.openhab.binding.onewire.internal.OwBindingConstants.SUPPORTED_THING_TYPES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onewire.internal.handler.AdvancedMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BAE091xSensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicMultisensorThingHandler;
import org.openhab.binding.onewire.internal.handler.BasicThingHandler;
import org.openhab.binding.onewire.internal.handler.EDSSensorThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OwHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.onewire")
public class OwHandlerFactory extends BaseThingHandlerFactory {
    private final OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Activate
    public OwHandlerFactory(@Reference OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (OwserverBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new OwserverBridgeHandler((Bridge) thing);
        } else if (BasicMultisensorThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new BasicMultisensorThingHandler(thing, dynamicStateDescriptionProvider);
        } else if (AdvancedMultisensorThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new AdvancedMultisensorThingHandler(thing, dynamicStateDescriptionProvider);
        } else if (BasicThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new BasicThingHandler(thing, dynamicStateDescriptionProvider);
        } else if (EDSSensorThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new EDSSensorThingHandler(thing, dynamicStateDescriptionProvider);
        } else if (BAE091xSensorThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new BAE091xSensorThingHandler(thing, dynamicStateDescriptionProvider);
        }

        return null;
    }
}
