/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.synopanalyzer.internal.discovery.SynopAnalyzerDiscoveryService;
import org.openhab.binding.synopanalyzer.internal.handler.SynopAnalyzerHandler;
import org.openhab.binding.synopanalyzer.internal.synop.StationDB;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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
    private final LocationProvider locationProvider;
    private final Gson gson = new Gson();
    private @Nullable StationDB stationDB;
    private @Nullable ServiceRegistration<?> serviceReg;

    @Activate
    public SynopAnalyzerHandlerFactory(@Reference LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
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

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/db/stations.json");
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);) {

            StationDB stations = gson.fromJson(reader, StationDB.class);
            registerDiscoveryService(stations);
            this.stationDB = stations;
            logger.debug("Discovery service for Synop Stations registered.");
        } catch (IOException e) {
            logger.warn("Unable to read synop stations database");
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        unregisterDiscoveryService();
        super.deactivate(componentContext);
    }

    private void registerDiscoveryService(StationDB stations) {
        SynopAnalyzerDiscoveryService discoveryService = new SynopAnalyzerDiscoveryService(stations, locationProvider);

        serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<>());
    }

    private void unregisterDiscoveryService() {
        if (serviceReg != null) {
            serviceReg.unregister();
            serviceReg = null;
        }
    }
}
