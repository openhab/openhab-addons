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
package org.openhab.binding.kermi.internal;

import static org.openhab.binding.kermi.internal.KermiBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.handler.KermiBridgeHandler;
import org.openhab.binding.kermi.internal.handler.KermiDrinkingWaterHeatingThingHandler;
import org.openhab.binding.kermi.internal.handler.KermiHeatpumpManagerThingHandler;
import org.openhab.binding.kermi.internal.handler.KermiHeatpumpThingHandler;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link KermiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author marco@descher.at - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.kermi", service = ThingHandlerFactory.class)
public class KermiHandlerFactory extends BaseThingHandlerFactory {

    private KermiHttpUtil httpUtil;
    private KermiSiteInfo kermiSiteInfo;

    public KermiHandlerFactory() {
        httpUtil = new KermiHttpUtil();
        kermiSiteInfo = new KermiSiteInfo();
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>() {

        private static final long serialVersionUID = 1L;
        {
            add(THING_TYPE_BRIDGE);
            add(THING_TYPE_HEATPUMP_MANAGER);
            add(THING_TYPE_HEATPUMP);
            add(THING_TYPE_DRINKINGWATER_HEATING);
            add(THING_TYPE_ROOM_HEATING);
        }
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new KermiBridgeHandler((Bridge) thing, httpUtil, kermiSiteInfo);
        } else if (thingTypeUID.equals(THING_TYPE_HEATPUMP_MANAGER)) {
            return new KermiHeatpumpManagerThingHandler(thing, httpUtil, kermiSiteInfo);
        } else if (thingTypeUID.equals(THING_TYPE_DRINKINGWATER_HEATING)) {
            return new KermiDrinkingWaterHeatingThingHandler(thing, httpUtil, kermiSiteInfo);
        } else if (thingTypeUID.equals(THING_TYPE_HEATPUMP)) {
            return new KermiHeatpumpThingHandler(thing, httpUtil, kermiSiteInfo);
        }

        return null;
    }
}
