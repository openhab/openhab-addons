/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.bank.RioBankHandler;
import org.openhab.binding.russound.internal.rio.controller.RioControllerHandler;
import org.openhab.binding.russound.internal.rio.favorites.RioFavoriteHandler;
import org.openhab.binding.russound.internal.rio.preset.RioPresetHandler;
import org.openhab.binding.russound.internal.rio.source.RioSourceHandler;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;
import org.openhab.binding.russound.internal.rio.zone.RioZoneHandler;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link RussoundHandlerFactory} is responsible for creating bridge and thing
 * handlers.
 *
 * @author Tim Roberts
 */
public class RussoundHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(RioConstants.BRIDGE_TYPE_RIO,
            RioConstants.BRIDGE_TYPE_CONTROLLER, RioConstants.BRIDGE_TYPE_SOURCE, RioConstants.BRIDGE_TYPE_ZONE,
            RioConstants.BRIDGE_TYPE_BANK, RioConstants.THING_TYPE_BANK_PRESET, RioConstants.THING_TYPE_ZONE_PRESET,
            RioConstants.THING_TYPE_SYSTEM_FAVORITE, RioConstants.THING_TYPE_ZONE_FAVORITE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(RioConstants.BRIDGE_TYPE_RIO)) {
            return new RioSystemHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RioConstants.BRIDGE_TYPE_CONTROLLER)) {
            return new RioControllerHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RioConstants.BRIDGE_TYPE_SOURCE)) {
            return new RioSourceHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RioConstants.BRIDGE_TYPE_ZONE)) {
            return new RioZoneHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RioConstants.BRIDGE_TYPE_BANK)) {
            return new RioBankHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RioConstants.THING_TYPE_BANK_PRESET)) {
            return new RioPresetHandler(thing);
        } else if (thingTypeUID.equals(RioConstants.THING_TYPE_ZONE_PRESET)) {
            return new RioPresetHandler(thing);
        } else if (thingTypeUID.equals(RioConstants.THING_TYPE_SYSTEM_FAVORITE)) {
            return new RioFavoriteHandler(thing);
        } else if (thingTypeUID.equals(RioConstants.THING_TYPE_ZONE_FAVORITE)) {
            return new RioFavoriteHandler(thing);
        }

        return null;
    }
}
