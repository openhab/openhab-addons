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
package org.openhab.binding.evcc.internal.discovery;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.discovery.mapper.BatteryDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.EvccDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.LoadpointDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.PvDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.SiteDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.StatisticsDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.VehicleDiscoveryMapper;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link EvccDiscoveryService} is responsible for scanning the API response for things
 * 
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = EvccDiscoveryService.class, configurationPid = "discovery.evcc")
public class EvccDiscoveryService extends AbstractThingHandlerDiscoveryService<EvccBridgeHandler> {

    private static final int TIMEOUT = 5;
    private static final int SCAN_INTERVAL_IN_SECONDS = 5; // We can scan every 5 seconds since we are using the cached
                                                           // response

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<EvccDiscoveryMapper> mappers = List.of(new LoadpointDiscoveryMapper(),
            new VehicleDiscoveryMapper(), new BatteryDiscoveryMapper(), new SiteDiscoveryMapper(),
            new PvDiscoveryMapper(), new StatisticsDiscoveryMapper());

    private @Nullable ScheduledFuture<?> evccDiscoveryJob;

    public EvccDiscoveryService() {
        super(EvccBridgeHandler.class, SUPPORTED_THING_TYPES, TIMEOUT, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting evcc Discover");
        JsonObject state = thingHandler.getCachedEvccState();
        if (!state.isEmpty()) {
            for (EvccDiscoveryMapper mapper : mappers) {
                mapper.discover(state, thingHandler).forEach(thing -> {
                    logger.debug("Thing discovered {}", thing);
                    thingDiscovered(thing);
                });
            }
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start evcc background discovery");
        Optional.ofNullable(evccDiscoveryJob).ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                startNewJob();
            }
        }, this::startNewJob);
    }

    private void startNewJob() {
        evccDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, SCAN_INTERVAL_IN_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop evcc device background discovery");
        Optional.ofNullable(evccDiscoveryJob)
                .ifPresent(backgroundScan -> backgroundScan.cancel(isBackgroundDiscoveryEnabled()));
        evccDiscoveryJob = null;
    }
}
