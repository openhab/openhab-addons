/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bigassfan.discovery;

import static org.openhab.binding.bigassfan.BigAssFanBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BigAssFanDiscoveryService} class implements a service
 * for discovering the Big Ass Fans.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BigAssFanDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(BigAssFanDiscoveryService.class);

    private static final boolean BACKGROUND_DISCOVERY_ENABLED = true;
    private final long BACKGROUND_DISCOVERY_DELAY = 8L;

    private DiscoveryServiceCallback callback;

    // Our own thread pool for the long-running listener job
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> listenerJob;

    DiscoveryListener discoveryListener;

    private boolean terminate;

    private Runnable listenerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                listen();
            } catch (Exception e) {
                logger.warn("Discovery listener got unexpected exception: {}", e.getMessage());
            }
        }
    };

    // Frequency (in seconds) with which we poll for new fans
    private final long POLL_FREQ = 600L;
    private final long POLL_DELAY = 12L;
    private ScheduledFuture<?> pollJob;

    public BigAssFanDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, BACKGROUND_DISCOVERY_ENABLED);
        listenerJob = null;
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
        logger.trace("BigAssFan discovery service ACTIVATED");
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        logger.trace("BigAssFan discovery service DEACTIVATED");
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

    private void startListenerJob() {
        if (listenerJob == null) {
            terminate = false;
            logger.debug("Starting discovery listener job in {} seconds", BACKGROUND_DISCOVERY_DELAY);
            listenerJob = scheduledExecutorService.schedule(listenerRunnable, BACKGROUND_DISCOVERY_DELAY,
                    TimeUnit.SECONDS);
        }
    }

    private void cancelListenerJob() {
        if (listenerJob != null) {
            logger.debug("Canceling discovery listener job");
            listenerJob.cancel(true);
            terminate = true;
            listenerJob = null;
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

        try {
            discoveryListener = new DiscoveryListener();
        } catch (SocketException se) {
            logger.warn("Got Socket exception creating multicast socket: {}", se.getMessage());
            return;
        } catch (IOException ioe) {
            logger.warn("Got IO exception creating multicast socket: {}", ioe.getMessage());
            return;
        }

        logger.debug("Waiting for discovery messages");
        while (!terminate) {
            try {
                // Wait for a discovery message
                BigAssFanDevice device = discoveryListener.waitForMessage();
                processMessage(device);
            } catch (IOException ioe) {
                logger.warn("Got IO exception waiting for message: {}", ioe.getMessage());
                break;
            } catch (Exception e) {
                logger.warn("Got exception waiting for message: {}", e.getMessage());
                break;
            }
        }
        discoveryListener.shutdown();
        logger.debug("DiscoveryListener job is exiting");
    }

    private void processMessage(BigAssFanDevice device) {
        if (device == null) {
            return;
        }
        // XXX Uncomment to test discovery
        // device.setDiscoveryMessage("(Master Bedroom Wall Controller;DEVICE;ID;20:F8:5E:DB:98:37;SWITCH,SENSEME)");
        // device.setDiscoveryMessage("(Master Bedroom Fan;DEVICE;ID;20:F8:5E:DA:A5:6A;FAN,LSERIES)");
        // device.setIpAddress("192.168.1.99");
        // device.setDiscoveryMessage("(Porch Wall Controller;DEVICE;ID;20:F8:5E:DB:98:A7;SWITCH,SENSEME)");
        // device.setDiscoveryMessage("(Porch Fan;DEVICE;ID;20:F8:5E:DA:A5:AA;FAN,LSERIES)");
        // device.setIpAddress("192.168.1.98");

        Pattern pattern = Pattern.compile("[(](.*);DEVICE;ID;(.*);(.*)[)]");
        Matcher matcher = pattern.matcher(device.getDiscoveryMessage());
        if (matcher.find()) {
            logger.debug("Match: grp1={}, grp2={}, grp(3)={}", matcher.group(1), matcher.group(2), matcher.group(3));

            // Extract needed information from the discovery message
            device.setLabel(matcher.group(1));
            device.setMacAddress(matcher.group(2));
            String[] modelParts = matcher.group(3).split(",");
            if (modelParts.length != 2) {
                logger.info("Unable to extract device type from discovery message");
                return;
            }
            device.setType(modelParts[0]);
            device.setModel(modelParts[1]);

            deviceDiscovered(device);
        }
    }

    private synchronized void deviceDiscovered(BigAssFanDevice device) {
        logger.info("Device discovered: {}", device);

        if (device.isSwitch()) {
            logger.debug("Switches (controllers) are not handled currently");
            return;
        } else if (device.isFan()) {
            logger.debug("Add fan with IP={}, MAC={}", device.getIpAddress(), device.getMacAddress());
        } else {
            logger.debug("Discovered unknown device type {} at IP={}", device.getModel(), device.getIpAddress());
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

        try {
            ThingUID uid = new ThingUID(THING_TYPE_FAN, serialNumber);
            if (uid != null) {
                // If there's not a thing and it's not in the inbox, create the discovery result
                if (callback != null && callback.getExistingDiscoveryResult(uid) == null
                        && callback.getExistingThing(uid) == null) {

                    logger.info("Creating discovery result for UID={}, IP={}", uid, device.getIpAddress());
                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                            .withLabel(device.getLabel()).build();

                    thingDiscovered(result);
                } else {
                    logger.debug("Thing or inbox entry already exists for UID={}, IP={}", uid, device.getIpAddress());
                }
            }
        } catch (Exception e) {
            logger.warn("Exception creating discovery result: {}", e.getMessage());
        }
    }

    private void schedulePollJob() {
        logger.debug("Scheduling poll job to run every {} seconds starting in {} sec", POLL_FREQ, POLL_DELAY);
        cancelPollJob();
        pollJob = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    discoveryListener.pollForDevices();
                } catch (Exception e) {
                    logger.debug("Poll job got unexpected exception: {}", e.getMessage());
                }
            }
        }, POLL_DELAY, POLL_FREQ, TimeUnit.SECONDS);
    }

    private void cancelPollJob() {
        if (pollJob != null) {
            logger.debug("Canceling poll job");
            pollJob.cancel(true);
            pollJob = null;
        }
    }
}
