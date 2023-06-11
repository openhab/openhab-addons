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
package org.openhab.binding.millheat.internal.discovery;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class does discovery of discoverable things
 *
 * @author Arne Seime - Initial contribution
 */
public class MillheatDiscoveryService extends AbstractDiscoveryService {
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(MillheatBindingConstants.THING_TYPE_HEATER, MillheatBindingConstants.THING_TYPE_ROOM,
                    MillheatBindingConstants.THING_TYPE_HOME).collect(Collectors.toSet()));
    private final Logger logger = LoggerFactory.getLogger(MillheatDiscoveryService.class);
    private ScheduledFuture<?> discoveryJob;
    private final MillheatAccountHandler accountHandler;

    public MillheatDiscoveryService(final MillheatAccountHandler accountHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void startScan() {
        try {
            final ThingUID accountUID = accountHandler.getThing().getUID();
            logger.debug("Start scan for Millheat devices on account {}", accountUID.toString());
            accountHandler.updateModelFromServerWithRetry(false);
            final MillheatModel model = accountHandler.getModel();
            for (final Home home : model.getHomes()) {
                final ThingUID homeUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HOME, accountUID,
                        String.valueOf(home.getId()));
                final DiscoveryResult discoveryResultHome = DiscoveryResultBuilder.create(homeUID)
                        .withBridge(accountUID).withLabel(home.getName()).withProperty("homeId", home.getId())
                        .withRepresentationProperty("homeId").build();
                thingDiscovered(discoveryResultHome);

                for (final Room room : home.getRooms()) {
                    final ThingUID roomUID = new ThingUID(MillheatBindingConstants.THING_TYPE_ROOM, accountUID,
                            String.valueOf(room.getId()));
                    final DiscoveryResult discoveryResultRoom = DiscoveryResultBuilder.create(roomUID)
                            .withBridge(accountUID).withLabel(room.getName()).withProperty("roomId", room.getId())
                            .withRepresentationProperty("roomId").build();
                    thingDiscovered(discoveryResultRoom);
                    for (final Heater heater : room.getHeaters()) {
                        final ThingUID heaterUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HEATER, accountUID,
                                String.valueOf(heater.getId()));
                        final DiscoveryResult discoveryResultHeater = DiscoveryResultBuilder.create(heaterUID)
                                .withBridge(accountUID).withLabel(heater.getName())
                                .withProperty("heaterId", heater.getId()).withRepresentationProperty("macAddress")
                                .withProperty("macAddress", heater.getMacAddress()).build();
                        thingDiscovered(discoveryResultHeater);
                    }
                }
                for (final Heater heater : home.getIndependentHeaters()) {
                    final ThingUID heaterUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HEATER, accountUID,
                            String.valueOf(heater.getId()));
                    final DiscoveryResult discoveryResultHeater = DiscoveryResultBuilder.create(heaterUID)
                            .withBridge(accountUID).withLabel(heater.getName()).withRepresentationProperty("heaterId")
                            .withProperty("heaterId", heater.getId()).build();
                    thingDiscovered(discoveryResultHeater);
                }
            }
        } finally {
            removeOlderResults(getTimestampOfLastScan(), null, accountHandler.getThing().getUID());
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }
}
