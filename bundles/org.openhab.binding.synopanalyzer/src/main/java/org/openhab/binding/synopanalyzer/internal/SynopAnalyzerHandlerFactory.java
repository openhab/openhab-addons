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
package org.openhab.binding.synopanalyzer.internal;

import static org.openhab.binding.synopanalyzer.internal.SynopAnalyzerBindingConstants.THING_SYNOP;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.synopanalyser.internal.discovery.SynopAnalyzerDiscoveryService;
import org.openhab.binding.synopanalyser.internal.synop.StationDB;
import org.openhab.binding.synopanalyzer.internal.handler.SynopAnalyzerHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SynopAnalyzerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.synopanalyzer")
@NonNullByDefault
public class SynopAnalyzerHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SynopAnalyzerHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_SYNOP);
    private final LocationProvider locationProvider;
    private final Gson gson;
    private @NonNullByDefault({}) StationDB stationDB;
    private TimeZoneProvider timeZoneProvider;
    private final Map<ThingTypeUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public SynopAnalyzerHandlerFactory(@Reference LocationProvider locationProvider,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.locationProvider = locationProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.gson = new Gson();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return thingTypeUID.equals(THING_SYNOP)
                ? new SynopAnalyzerHandler(thing, locationProvider, timeZoneProvider, stationDB)
                : null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/db/stations.json");
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            stationDB = gson.fromJson(reader, StationDB.class);
            reader.close();
            registerDiscoveryService();
            logger.debug("Discovery service for Synop Stations registered.");
        } catch (IOException e) {
            logger.warn("Unable to read synop stations database");
            stationDB = new StationDB();
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        unregisterDiscoveryService();
        super.deactivate(componentContext);
    }

    private void registerDiscoveryService() {
        SynopAnalyzerDiscoveryService discoveryService = new SynopAnalyzerDiscoveryService(stationDB, locationProvider);
        discoveryServiceRegs.put(THING_SYNOP,
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private void unregisterDiscoveryService() {
        if (discoveryServiceRegs.containsKey(THING_SYNOP)) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(THING_SYNOP);
            serviceReg.unregister();
            discoveryServiceRegs.remove(THING_SYNOP);
        }
    }
}
