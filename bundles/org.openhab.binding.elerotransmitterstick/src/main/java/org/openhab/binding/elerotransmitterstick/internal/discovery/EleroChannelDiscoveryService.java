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
package org.openhab.binding.elerotransmitterstick.internal.discovery;

import static org.openhab.binding.elerotransmitterstick.internal.EleroTransmitterStickBindingConstants.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.elerotransmitterstick.internal.handler.EleroTransmitterStickHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EleroChannelDiscoveryService} is responsible for discovery of elero channels from an Elero Transmitter
 * Stick.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroChannelDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private final Logger logger = LoggerFactory.getLogger(EleroChannelDiscoveryService.class);

    private EleroTransmitterStickHandler bridge;
    private ScheduledFuture<?> sensorDiscoveryJob;

    /**
     * Creates the discovery service for the given handler and converter.
     */
    public EleroChannelDiscoveryService(EleroTransmitterStickHandler stickHandler) {
        super(Set.of(THING_TYPE_ELERO_CHANNEL), DISCOVER_TIMEOUT_SECONDS, true);

        bridge = stickHandler;
    }

    @Override
    protected void startScan() {
        discoverSensors();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Elero Channel background discovery");
        if (sensorDiscoveryJob == null || sensorDiscoveryJob.isCancelled()) {
            sensorDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                discoverSensors();
            }, 0, 2, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Elero Channel background discovery");
        if (sensorDiscoveryJob != null && !sensorDiscoveryJob.isCancelled()) {
            sensorDiscoveryJob.cancel(true);
            sensorDiscoveryJob = null;
        }
    }

    private void discoverSensors() {
        if (bridge.getStick() == null) {
            logger.debug("Stick not opened, scanning skipped.");
            return;
        }

        ArrayList<Integer> channelIds = bridge.getStick().getKnownIds();
        if (channelIds.isEmpty()) {
            logger.debug("Could not obtain known channels from the stick, scanning skipped.");
            return;
        }

        for (Integer id : channelIds) {
            ThingUID sensorThing = new ThingUID(THING_TYPE_ELERO_CHANNEL, bridge.getThing().getUID(),
                    String.valueOf(id));

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(sensorThing).withLabel("Channel " + id)
                    .withRepresentationProperty("id").withBridge(bridge.getThing().getUID())
                    .withProperty(PROPERTY_CHANNEL_ID, id).build();
            thingDiscovered(discoveryResult);
        }
    }
}
