/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bigassfan.internal.discovery;

import static org.openhab.binding.bigassfan.internal.BigAssFanBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BigAssFanDiscoveryService} class implements a service
 * for discovering the Big Ass Fans.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.bigassfan")
public class BigAssFanDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(BigAssFanDiscoveryService.class);

    private static final boolean BACKGROUND_DISCOVERY_ENABLED = true;
    private static final long BACKGROUND_DISCOVERY_DELAY = 8L;

    // Our own thread pool for the long-running listener job
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private @Nullable ScheduledFuture<?> listenerJob;
    private @Nullable DiscoveryListener discoveryListener;
    private boolean terminate;
    private final Pattern announcementPattern = Pattern.compile("[(](.*);DEVICE;ID;(.*);(.*)[)]");

    private Runnable listenerRunnable = () -> {
        try {
            listen();
        } catch (RuntimeException e) {
            logger.warn("Discovery listener got unexpected exception: {}", e.getMessage(), e);
        }
    };

    // Frequency (in seconds) with which we poll for new devices
    private static final long POLL_FREQ = 300L;
    private static final long POLL_DELAY = 12L;
    private @Nullable ScheduledFuture<?> pollJob;

    public BigAssFanDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, BACKGROUND_DISCOVERY_ENABLED);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
        logger.trace("BigAssFan discovery service ACTIVATED");
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        logger.trace("BigAssFan discovery service DEACTIVATED");
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");
        startListenerJob();
        schedulePollJob();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping background discovery");
        cancelPollJob();
        cancelListenerJob();
    }

    private synchronized void startListenerJob() {
        if (this.listenerJob == null) {
            logger.debug("Starting discovery listener job in {} seconds", BACKGROUND_DISCOVERY_DELAY);
            terminate = false;
            this.listenerJob = scheduledExecutorService.schedule(listenerRunnable, BACKGROUND_DISCOVERY_DELAY,
                    TimeUnit.SECONDS);
        }
    }

    private void cancelListenerJob() {
        ScheduledFuture<?> localListenerJob = this.listenerJob;
        if (localListenerJob != null) {
            logger.debug("Canceling discovery listener job");
            localListenerJob.cancel(true);
            terminate = true;
            this.listenerJob = null;
        }
    }

    @Override
    public void startScan() {
    }

    @Override
    public void stopScan() {
    }

    private synchronized void listen() {
        logger.info("BigAssFan discovery service is running");
        DiscoveryListener localDiscoveryListener;

        try {
            localDiscoveryListener = new DiscoveryListener();
            discoveryListener = localDiscoveryListener;
        } catch (SocketException se) {
            logger.warn("Got Socket exception creating multicast socket: {}", se.getMessage(), se);
            return;
        } catch (IOException ioe) {
            logger.warn("Got IO exception creating multicast socket: {}", ioe.getMessage(), ioe);
            return;
        }

        logger.debug("Waiting for discovery messages");
        while (!terminate) {
            try {
                // Wait for a discovery message
                processMessage(localDiscoveryListener.waitForMessage());
            } catch (SocketTimeoutException e) {
                // Read on socket timed out; check for termination
                continue;
            } catch (IOException ioe) {
                logger.warn("Got IO exception waiting for message: {}", ioe.getMessage(), ioe);
                break;
            }
        }
        localDiscoveryListener.shutdown();
        logger.debug("DiscoveryListener job is exiting");
    }

    private void processMessage(BigAssFanDevice device) {
        Matcher matcher = announcementPattern.matcher(device.getDiscoveryMessage());
        if (matcher.find()) {
            logger.debug("Match: grp1={}, grp2={}, grp(3)={}", matcher.group(1), matcher.group(2), matcher.group(3));

            // Extract needed information from the discovery message
            device.setLabel(matcher.group(1));
            device.setMacAddress(matcher.group(2));
            String[] modelParts = matcher.group(3).split(",");
            switch (modelParts.length) {
                case 2:
                    // L-Series fans
                    device.setType(modelParts[0]);
                    device.setModel(modelParts[1]);
                    deviceDiscovered(device);
                    break;
                case 3:
                    // H-Series fans
                    device.setType(modelParts[0]);
                    device.setModel(modelParts[2]);
                    deviceDiscovered(device);
                    break;
                default:
                    logger.info("Unable to extract device type from discovery message");
                    break;
            }
        }
    }

    private synchronized void deviceDiscovered(BigAssFanDevice device) {
        logger.debug("Device discovered: {}", device);

        ThingTypeUID thingTypeUid;

        if (device.isSwitch()) {
            logger.debug("Add controller with IP={}, MAC={}, MODEL={}", device.getIpAddress(), device.getMacAddress(),
                    device.getModel());
            thingTypeUid = THING_TYPE_CONTROLLER;
        } else if (device.isFan()) {
            logger.debug("Add fan with IP={}, MAC={}, MODEL={}", device.getIpAddress(), device.getMacAddress(),
                    device.getModel());
            thingTypeUid = THING_TYPE_FAN;
        } else if (device.isLight()) {
            logger.debug("Add light with IP={}, MAC={}, MODEL={}", device.getIpAddress(), device.getMacAddress(),
                    device.getModel());
            thingTypeUid = THING_TYPE_LIGHT;
        } else {
            logger.info("Discovered unknown device type {} at IP={}", device.getModel(), device.getIpAddress());
            return;
        }

        // We got a valid discovery message. Process it as a potential new thing
        String serialNumber = device.getMacAddress().replace(":", "");

        Map<String, Object> properties = new HashMap<>();
        properties.put(THING_PROPERTY_MAC, device.getMacAddress());
        properties.put(THING_PROPERTY_IP, device.getIpAddress());
        properties.put(THING_PROPERTY_LABEL, device.getLabel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
        properties.put(Thing.PROPERTY_VENDOR, "Haiku");

        ThingUID uid = new ThingUID(thingTypeUid, serialNumber);
        logger.debug("Creating discovery result for UID={}, IP={}", uid, device.getIpAddress());
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(THING_PROPERTY_MAC).withLabel(device.getLabel()).build());
    }

    private synchronized void schedulePollJob() {
        cancelPollJob();
        if (this.pollJob == null) {
            logger.debug("Scheduling discovery poll job to run every {} seconds starting in {} sec", POLL_FREQ,
                    POLL_DELAY);
            pollJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    DiscoveryListener localListener = discoveryListener;
                    if (localListener != null) {
                        localListener.pollForDevices();
                    }
                } catch (RuntimeException e) {
                    logger.warn("Poll job got unexpected exception: {}", e.getMessage(), e);
                }
            }, POLL_DELAY, POLL_FREQ, TimeUnit.SECONDS);
        }
    }

    private void cancelPollJob() {
        ScheduledFuture<?> localPollJob = pollJob;
        if (localPollJob != null) {
            logger.debug("Canceling poll job");
            localPollJob.cancel(true);
            this.pollJob = null;
        }
    }
}
