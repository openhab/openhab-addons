/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.novelanheatpump.internal;

import static org.openhab.binding.novelanheatpump.internal.NovelanHeatpumpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NovelanHeatpumpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.novelanheatpump", service = ThingHandlerFactory.class)
public class NovelanHeatpumpHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HEATPUMP);
    private static final Map<String, NovelanHeatpumpHandler> THING_HANDLERS = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        NovelanHeatpumpHandler thingHandler = null;

        if (THING_TYPE_HEATPUMP.equals(thingTypeUID)) {
            thingHandler = new NovelanHeatpumpHandler(thing);
        }

        if (thingHandler != null) {
            THING_HANDLERS.put(thing.getUID().toString(), thingHandler);
        }

        return thingHandler;
    }

    public static @Nullable NovelanHeatpumpHandler getHandler(String thingUid) {
        return THING_HANDLERS.get(thingUid);
    }
}
