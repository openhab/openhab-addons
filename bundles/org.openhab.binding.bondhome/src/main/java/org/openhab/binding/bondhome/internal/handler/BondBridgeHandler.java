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
package org.openhab.binding.bondhome.internal.handler;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.BondException;
import org.openhab.binding.bondhome.internal.api.BPUPListener;
import org.openhab.binding.bondhome.internal.api.BPUPUpdate;
import org.openhab.binding.bondhome.internal.api.BondDeviceState;
import org.openhab.binding.bondhome.internal.api.BondHttpApi;
import org.openhab.binding.bondhome.internal.api.BondSysVersion;
import org.openhab.binding.bondhome.internal.config.BondBridgeConfiguration;
import org.openhab.binding.bondhome.internal.discovery.BondDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BondBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BondBridgeHandler.class);

    // Get a dedicated threadpool for the long-running listener thread.
    // Intent is to not permanently tie up the common scheduler pool.
    private final ScheduledExecutorService bondScheduler = ThreadPoolManager.getScheduledPool("bondBridgeHandler");
    private final BPUPListener udpListener;
    private final BondHttpApi api;

    private BondBridgeConfiguration config = new BondBridgeConfiguration();

    private @Nullable BondDiscoveryService discoveryService;

    private final Set<BondDeviceHandler> handlers = Collections.synchronizedSet(new HashSet<>());

    private @Nullable ScheduledFuture<?> initializer;

    public BondBridgeHandler(Bridge bridge, final HttpClientFactory httpClientFactory) {
        super(bridge);
        udpListener = new BPUPListener(this);
        api = new BondHttpApi(this, httpClientFactory);
        logger.debug("Created a BondBridgeHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Not needed, all commands are handled in the {@link BondDeviceHandler}
    }

    @Override
    public void initialize() {
        config = getConfigAs(BondBridgeConfiguration.class);

        // set the thing status to UNKNOWN temporarily
        updateStatus(ThingStatus.UNKNOWN);

        this.initializer = scheduler.schedule(this::initializeThing, 0L, TimeUnit.MILLISECONDS);
    }

    private void initializeThing() {
        if (config.localToken.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.incorrect-local-token");
            this.initializer = null;
            return;
        }
        if (config.ipAddress.isEmpty()) {
            try {
                String lookupAddress = config.serialNumber + ".local";
                logger.debug("Attempting to get IP address for Bond Bridge {}", lookupAddress);
                InetAddress ia = InetAddress.getByName(lookupAddress);
                String ip = ia.getHostAddress();
                Configuration c = editConfiguration();
                c.put(CONFIG_IP_ADDRESS, ip);
                updateConfiguration(c);
                config = getConfigAs(BondBridgeConfiguration.class);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.unknown-host");
                this.initializer = null;
                return;
            }
        } else {
            try {
                InetAddress.getByName(config.ipAddress);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.invalid-host");
                this.initializer = null;
                return;
            }
        }

        // Ask the bridge its current status and update the properties with the info
        // This will also set the thing status to online/offline based on whether it
        // succeeds in getting the properties from the bridge.
        updateBridgeProperties();
        this.initializer = null;
    }

    @Override
    public void dispose() {
        // The listener should already have been stopped when the last child was
        // disposed,
        // but we'll call the stop here for good measure.
        stopUDPListenerJob();
        ScheduledFuture<?> localInitializer = initializer;
        if (localInitializer != null) {
            localInitializer.cancel(true);
        }
    }

    private synchronized void startUDPListenerJob() {
        if (udpListener.isRunning()) {
            return;
        }
        logger.debug("Started listener job");
        udpListener.start(bondScheduler);
    }

    private synchronized void stopUDPListenerJob() {
        logger.trace("Stopping UDP listener job");
        if (udpListener.isRunning()) {
            udpListener.shutdown();
            logger.debug("UDP listener job stopped");
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof BondDeviceHandler) {
            BondDeviceHandler handler = (BondDeviceHandler) childHandler;
            synchronized (handlers) {
                // Start the BPUP update service after the first child device is added
                startUDPListenerJob();
                if (!handlers.contains(handler)) {
                    handlers.add(handler);
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof BondDeviceHandler) {
            BondDeviceHandler handler = (BondDeviceHandler) childHandler;
            synchronized (handlers) {
                handlers.remove(handler);
                if (handlers.isEmpty()) {
                    // Stop the update service when the last child is removed
                    stopUDPListenerJob();
                }
            }
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    /**
     * Forwards a push update to a device
     *
     * @param the {@link BPUPUpdate object}
     */
    public void forwardUpdateToThing(BPUPUpdate pushUpdate) {
        updateStatus(ThingStatus.ONLINE);

        BondDeviceState updateState = pushUpdate.deviceState;
        String topic = pushUpdate.topic;
        String deviceId = null;
        String topicType = null;
        if (topic != null) {
            String parts[] = topic.split("/");
            deviceId = parts[1];
            topicType = parts[2];
        }
        // We can't use getThingByUID because we don't know the type of device and thus
        // don't know the full uid (that is we cannot tell a fan from a fireplace, etc,
        // from the contents of the update)
        if (deviceId != null) {
            if (topicType != null && "state".equals(topicType)) {
                synchronized (handlers) {
                    for (BondDeviceHandler handler : handlers) {
                        String handlerDeviceId = handler.getDeviceId();
                        if (handlerDeviceId.equalsIgnoreCase(deviceId)) {
                            handler.updateChannelsFromState(updateState);
                            break;
                        }
                    }
                }
            } else {
                logger.trace("could not read topic type from push update or type was not state.");
            }
        } else {
            logger.warn("Can not read device Id from push update.");
        }
    }

    /**
     * Returns the Id of the bridge associated with the handler
     */
    public String getBridgeId() {
        return config.serialNumber;
    }

    /**
     * Returns the Ip Address of the bridge associated with the handler as a string
     */
    public String getBridgeIpAddress() {
        return config.ipAddress;
    }

    /**
     * Returns the local token of the bridge associated with the handler as a string
     */
    public String getBridgeToken() {
        return config.localToken;
    }

    /**
     * Returns the api instance
     */
    public BondHttpApi getBridgeAPI() {
        return this.api;
    }

    /**
     * Set the bridge status offline.
     *
     * Called by the dependents to set the bridge offline when repeated requests
     * fail.
     *
     * NOTE: This does NOT stop the UDP listener job, which will keep pinging the
     * bridge's IP once a minute. The listener job will set the bridge back online
     * if it receives a proper response from the bridge.
     */
    public void setBridgeOffline(ThingStatusDetail detail, String description) {
        updateStatus(ThingStatus.OFFLINE, detail, description);
    }

    /**
     * Set the bridge status back online.
     *
     * Called by the UDP listener when it gets a proper response.
     */
    public void setBridgeOnline(String bridgeAddress) {
        if (!config.isValid()) {
            logger.warn("Configuration error, cannot set the bridghe online without configuration");
            return;
        } else if (!config.ipAddress.equals(bridgeAddress)) {
            logger.debug("IP address of Bond {} has changed to {}", config.serialNumber, bridgeAddress);
            Configuration c = editConfiguration();
            c.put(CONFIG_IP_ADDRESS, bridgeAddress);
            updateConfiguration(c);
            updateBridgeProperties();
            return;
        }
        // don't bother updating on every keepalive packet
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateBridgeProperties();
        }
    }

    private void updateProperty(Map<String, String> thingProperties, String key, @Nullable String value) {
        if (value == null) {
            return;
        }
        thingProperties.put(key, value);
    }

    private void updateBridgeProperties() {
        BondSysVersion myVersion;
        try {
            myVersion = api.getBridgeVersion();
        } catch (BondException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        // Update all the thing properties based on the result
        Map<String, String> thingProperties = editProperties();
        updateProperty(thingProperties, PROPERTY_VENDOR, myVersion.make);
        updateProperty(thingProperties, PROPERTY_MODEL_ID, myVersion.model);
        updateProperty(thingProperties, PROPERTY_SERIAL_NUMBER, myVersion.bondid);
        updateProperty(thingProperties, PROPERTY_FIRMWARE_VERSION, myVersion.firmwareVersion);
        updateProperties(thingProperties);
        updateStatus(ThingStatus.ONLINE);
        BondDiscoveryService localDiscoveryService = discoveryService;
        if (localDiscoveryService != null) {
            localDiscoveryService.discoverNow();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(BondDiscoveryService.class);
    }

    public void setDiscoveryService(BondDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
}
