/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.foxtrot.handler.*;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FoxtrotHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Radovan Sninsky - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.foxtrot")
@NonNullByDefault
public class FoxtrotHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_PLC, THING_TYPE_VARIABLE, THING_TYPE_SWITCH, THING_TYPE_BLIND, THING_TYPE_DIMMER)
            .collect(Collectors.toSet());

    /**
     * Called when new Thing is added, checks if ThingType is supported.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PLC.equals(thingTypeUID)) {
            return new FoxtrotBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_VARIABLE.equals(thingTypeUID)) {
            return new VariableHandler(thing);
        } else if (THING_TYPE_SWITCH.equals(thingTypeUID)) {
            return new SwitchHandler(thing);
        } else if (THING_TYPE_BLIND.equals(thingTypeUID)) {
            return new BlindHandler(thing);
        } else if (THING_TYPE_DIMMER.equals(thingTypeUID)) {
            return new DimmerHandler(thing);
        }
        return null;
    }
}
