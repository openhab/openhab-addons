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
package org.openhab.binding.frenchgovtenergydata.internal;

import static org.openhab.binding.frenchgovtenergydata.internal.FrenchGovtEnergyDataBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.frenchgovtenergydata.internal.handler.BaseTariffHandler;
import org.openhab.binding.frenchgovtenergydata.internal.handler.HpHcTariffHandler;
import org.openhab.binding.frenchgovtenergydata.internal.handler.TempoTariffHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FrenchGovtEnergyDataHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.frenchgovtenergydata", service = ThingHandlerFactory.class)
public class FrenchGovtEnergyDataHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BASE, THING_TYPE_HPHC,
            THING_TYPE_TEMPO);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BASE.equals(thingTypeUID)) {
            return new BaseTariffHandler(thing);
        } else if (THING_TYPE_HPHC.equals(thingTypeUID)) {
            return new HpHcTariffHandler(thing);
        } else if (THING_TYPE_TEMPO.equals(thingTypeUID)) {
            return new TempoTariffHandler(thing);
        }

        return null;
    }
}
