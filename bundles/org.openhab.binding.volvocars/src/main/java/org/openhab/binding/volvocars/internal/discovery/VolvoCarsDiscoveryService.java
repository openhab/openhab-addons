/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volvocars.internal.discovery;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvocars.internal.api.VolvoCarsApi;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.openhab.binding.volvocars.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers the vehicles connected to a Volvo ID account.
 *
 * @author Michael Weger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VolvoCarsDiscoveryService.class)
@NonNullByDefault
public class VolvoCarsDiscoveryService extends AbstractThingHandlerDiscoveryService<AccountHandler> {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 30;
    private static final int BACKGROUND_DISCOVERY_INITIAL_DELAY_SECONDS = 30;
    private static final int BACKGROUND_DISCOVERY_INTERVAL_MINUTES = 60;

    private final Logger logger = LoggerFactory.getLogger(VolvoCarsDiscoveryService.class);
    private @Nullable ScheduledFuture<?> backgroundJob;

    public VolvoCarsDiscoveryService() {
        super(AccountHandler.class, Set.of(THING_TYPE_VEHICLE), DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startScan() {
        VolvoCarsApi api = thingHandler.getApi();
        if (api == null || thingHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Account {} is not online, skipping discovery", thingHandler.getThing().getUID());
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        try {
            for (String vin : api.getVehicles()) {
                String label = "Volvo";
                try {
                    Vehicle vehicle = api.getVehicle(vin);
                    String model = vehicle.getModel();
                    if (model != null) {
                        label += " " + model;
                    }
                    if (vehicle.modelYear > 0) {
                        label += " (" + vehicle.modelYear + ")";
                    }
                } catch (VolvoCarsException e) {
                    logger.debug("Unable to read vehicle details: {}", e.getMessage());
                }
                thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_VEHICLE, bridgeUID, vin))
                        .withBridge(bridgeUID).withLabel(label).withProperty(PROPERTY_VIN, vin)
                        .withRepresentationProperty(PROPERTY_VIN).build());
            }
        } catch (VolvoCarsException e) {
            logger.debug("Vehicle discovery failed: {}", e.getMessage());
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> job = backgroundJob;
        if (job == null || job.isCancelled()) {
            backgroundJob = scheduler.scheduleWithFixedDelay(this::startScan,
                    BACKGROUND_DISCOVERY_INITIAL_DELAY_SECONDS, BACKGROUND_DISCOVERY_INTERVAL_MINUTES * 60L,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> job = backgroundJob;
        if (job != null) {
            job.cancel(true);
            backgroundJob = null;
        }
    }
}
