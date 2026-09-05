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
package org.openhab.binding.millheat.internal.discovery;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.ScanListener;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers the homes, rooms and heaters on a Mill account, including houses that another account
 * has shared with it.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Cloud API identifiers, shared houses, and bridge-triggered first scan
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MillheatDiscoveryService.class)
@NonNullByDefault
public class MillheatDiscoveryService extends AbstractThingHandlerDiscoveryService<MillheatAccountHandler> {
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(MillheatDiscoveryService.class);
    private @Nullable ScheduledFuture<?> discoveryJob;

    public MillheatDiscoveryService() {
        super(MillheatAccountHandler.class, Set.of(MillheatBindingConstants.THING_TYPE_HEATER,
                MillheatBindingConstants.THING_TYPE_ROOM, MillheatBindingConstants.THING_TYPE_HOME),
                DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.setDiscoveryService(null);
    }

    @Override
    protected void startBackgroundDiscovery() {
        // No immediate first run: that would race the sign-in and scan an empty model. The
        // bridge calls scanNow() instead, as soon as it has one.
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, REFRESH_INTERVAL_MINUTES,
                REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        final ScheduledFuture<?> localJob = discoveryJob;
        if (localJob != null && !localJob.isCancelled()) {
            localJob.cancel(true);
        }
        discoveryJob = null;
    }

    /** Runs a scan through the normal lifecycle, so timestamps and result expiry stay consistent. */
    public void scanNow() {
        startScan((ScanListener) null);
    }

    @Override
    protected synchronized void startScan() {
        try {
            final ThingUID accountUID = thingHandler.getThing().getUID();
            logger.debug("Start scan for Mill devices on account {}", accountUID);
            thingHandler.updateModelFromServerWithRetry(false);
            final MillheatModel model = thingHandler.getModel();

            for (final Home home : model.getHomes()) {
                final ThingUID homeUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HOME, accountUID,
                        home.getId());
                thingDiscovered(DiscoveryResultBuilder.create(homeUID).withBridge(accountUID).withLabel(home.getName())
                        .withProperty("homeId", home.getId()).withRepresentationProperty("homeId").build());

                for (final Room room : home.getRooms()) {
                    final ThingUID roomUID = new ThingUID(MillheatBindingConstants.THING_TYPE_ROOM, accountUID,
                            room.getId());
                    thingDiscovered(
                            DiscoveryResultBuilder.create(roomUID).withBridge(accountUID).withLabel(room.getName())
                                    .withProperty("roomId", room.getId()).withRepresentationProperty("roomId").build());
                    for (final Heater heater : room.getHeaters()) {
                        thingDiscovered(heaterResult(accountUID, heater));
                    }
                }
                for (final Heater heater : home.getIndependentHeaters()) {
                    thingDiscovered(heaterResult(accountUID, heater));
                }
            }
        } finally {
            removeOlderResults(getTimestampOfLastScan(), null, thingHandler.getThing().getUID());
        }
    }

    private DiscoveryResult heaterResult(final ThingUID accountUID, final Heater heater) {
        final ThingUID heaterUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HEATER, accountUID, heater.getId());
        final DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(heaterUID).withBridge(accountUID)
                .withLabel(heater.getName()).withProperty("heaterId", heater.getId());
        final String macAddress = heater.getMacAddress();
        if (macAddress != null && !macAddress.isBlank()) {
            return builder.withProperty("macAddress", macAddress).withRepresentationProperty("macAddress").build();
        }
        return builder.withRepresentationProperty("heaterId").build();
    }
}
