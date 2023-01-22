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
package org.openhab.binding.synopanalyzer.internal;

import static org.openhab.binding.synopanalyzer.internal.SynopAnalyzerBindingConstants.THING_SYNOP;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.synopanalyzer.internal.handler.SynopAnalyzerHandler;
import org.openhab.binding.synopanalyzer.internal.stationdb.Station;
import org.openhab.binding.synopanalyzer.internal.stationdb.StationDbService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SynopAnalyzerHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.synopanalyzer")
@NonNullByDefault
public class SynopAnalyzerHandlerFactory extends BaseThingHandlerFactory {
    private final LocationProvider locationProvider;
    private final List<Station> stationDB;

    @Activate
    public SynopAnalyzerHandlerFactory(@Reference StationDbService stationDBService,
            @Reference LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
        this.stationDB = stationDBService.getStations();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_SYNOP.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        return supportsThingType(thing.getThingTypeUID()) ? new SynopAnalyzerHandler(thing, locationProvider, stationDB)
                : null;
    }
}
