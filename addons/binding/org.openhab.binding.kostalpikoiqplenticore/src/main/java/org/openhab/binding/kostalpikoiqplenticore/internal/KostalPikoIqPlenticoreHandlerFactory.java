/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostalpikoiqplenticore.internal;

import static org.openhab.binding.kostalpikoiqplenticore.internal.KostalPikoIqPlenticoreBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPikoIq100Handler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPikoIq42Handler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPikoIq55Handler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPikoIq70Handler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPikoIq85Handler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus100WithBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus100WithoutBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus42WithBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus42WithoutBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus55WithBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus55WithoutBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus70WithBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus70WithoutBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus85WithBatteryHandler;
import org.openhab.binding.kostalpikoiqplenticore.internal.things.KostalPlenticorePlus85WithoutBatteryHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link KostalPikoIqPlenticoreHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author René - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.kostalpikoiqplenticore", service = ThingHandlerFactory.class)
public class KostalPikoIqPlenticoreHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(PIKOIQ42, PIKOIQ55, PIKOIQ70, PIKOIQ85, PIKOIQ100, PLENTICOREPLUS42WITHBATTERY,
                    PLENTICOREPLUS55WITHBATTERY, PLENTICOREPLUS70WITHBATTERY, PLENTICOREPLUS85WITHBATTERY,
                    PLENTICOREPLUS100WITHBATTERY, PLENTICOREPLUS42WITHOUTBATTERY, PLENTICOREPLUS55WITHOUTBATTERY,
                    PLENTICOREPLUS70WITHOUTBATTERY, PLENTICOREPLUS85WITHOUTBATTERY, PLENTICOREPLUS100WITHOUTBATTERY));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (PIKOIQ42.equals(thingTypeUID)) {
            return new KostalPikoIq42Handler(thing);
        }
        if (PIKOIQ55.equals(thingTypeUID)) {
            return new KostalPikoIq55Handler(thing);
        }
        if (PIKOIQ70.equals(thingTypeUID)) {
            return new KostalPikoIq70Handler(thing);
        }
        if (PIKOIQ85.equals(thingTypeUID)) {
            return new KostalPikoIq85Handler(thing);
        }
        if (PIKOIQ100.equals(thingTypeUID)) {
            return new KostalPikoIq100Handler(thing);
        }
        if (PLENTICOREPLUS42WITHBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus42WithBatteryHandler(thing);
        }
        if (PLENTICOREPLUS55WITHBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus55WithBatteryHandler(thing);
        }
        if (PLENTICOREPLUS70WITHBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus70WithBatteryHandler(thing);
        }
        if (PLENTICOREPLUS85WITHBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus85WithBatteryHandler(thing);
        }
        if (PLENTICOREPLUS100WITHBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus100WithBatteryHandler(thing);
        }
        if (PLENTICOREPLUS42WITHOUTBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus42WithoutBatteryHandler(thing);
        }
        if (PLENTICOREPLUS55WITHOUTBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus55WithoutBatteryHandler(thing);
        }
        if (PLENTICOREPLUS70WITHOUTBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus70WithoutBatteryHandler(thing);
        }
        if (PLENTICOREPLUS85WITHOUTBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus85WithoutBatteryHandler(thing);
        }
        if (PLENTICOREPLUS100WITHOUTBATTERY.equals(thingTypeUID)) {
            return new KostalPlenticorePlus100WithoutBatteryHandler(thing);
        }

        return null;
    }
}
