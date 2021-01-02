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
package org.openhab.binding.bondhome.internal.handler;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.api.BPUPListener;
import org.openhab.binding.bondhome.internal.api.BPUPUpdate;
import org.openhab.binding.bondhome.internal.api.BondDeviceState;
import org.openhab.binding.bondhome.internal.api.BondHttpApi;
import org.openhab.binding.bondhome.internal.api.BondSysVersion;
import org.openhab.binding.bondhome.internal.config.BondBridgeConfiguration;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
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
    private @Nullable ScheduledFuture<?> listenerJob;
    private final BPUPListener udpListener;
    private final BondHttpApi api;

    private @NonNullByDefault({}) BondBridgeConfiguration config;

    private final Set<BondDeviceHandler> handlers = Collections.synchronizedSet(new HashSet<>());

    public BondBridgeHandler(Bridge bridge) {
        super(bridge);
        udpListener = new BPUPListener(this);
        api = new BondHttpApi(this);
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

        // Example for background initialization:
        scheduler.execute(() -> {
            initializeThing();
        });
    }

    private void initializeThing() {
        BondBridgeConfiguration localConfig = config = getConfigAs(BondBridgeConfiguration.class);
        if (localConfig.bondIpAddress == null) {
            try {
                logger.trace("IP address of Bond {} is unknown", localConfig.bondId);
                String lookupAddress = localConfig.bondId + ".local";
                logger.trace("Attempting to get IP address for Bond Bridge {}", lookupAddress);
                InetAddress ia = InetAddress.getByName(lookupAddress);
                String ip = ia.getHostAddress();
                Configuration c = editConfiguration();
                c.put(CONFIG_IP_ADDRESS, ip);
                updateConfiguration(c);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unable to get an IP Address for Bond Bridge");
                return;
            }
        } else {
            try {
                InetAddress.getByName(localConfig.bondIpAddress);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "IP Address or host name for Bond Bridge is not valid");
            }
        }

        // Ask the bridge it's current status and update the properties with the info
        // This will also set the thing status to online/offline based on whether it
        // succeeds in getting the properties from the bridge.
        updateBridgeProperties();

        // Finished
    }

    @Override
    public void dispose() {
        // The listener should already have been stopped when the last child was
        // disposed,
        // but we'll call the stop here for good measure.
        stopUDPListenerJob();
    }

    private synchronized void startUDPListenerJob() {
        logger.debug("Scheduled listener job to start in 30 seconds");
        listenerJob = bondScheduler.schedule(udpListener, 30, TimeUnit.SECONDS);
    }

    private synchronized void stopUDPListenerJob() {
        logger.trace("Stopping UDP listener job");
        ScheduledFuture<?> lJob = this.listenerJob;
        if (lJob != null) {
            lJob.cancel(true);
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
                if (handlers.isEmpty()) {
                    // Start the BPUP update service after the first child device is added
                    startUDPListenerJob();
                }
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
        BondDeviceState updateState = pushUpdate.deviceState;
        String topic = pushUpdate.topic;
        String deviceId = null;
        if (topic != null) {
            deviceId = topic.split("/")[1];
        }
        // We can't use getThingByUID because we don't know the type of device and thus
        // don't know the full uid (that is we cannot tell a fan from a fireplace, etc,
        // from the contents of the update)
        if (deviceId != null) {
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
            logger.warn("Can not read device Id from push update.");
        }
    }

    /**
     * Returns the Id of the bridge associated with the handler
     */
    public String getBridgeId() {
        return config.bondId;
    }

    /**
     * Returns the Ip Address of the bridge associated with the handler as a string
     */
    public @Nullable String getBridgeIpAddress() {
        return config.bondIpAddress;
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
    public void setBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
        updateBridgeProperties();
    }

    private void updateBridgeProperties() {
        BondSysVersion myVersion = null;
        try {
            myVersion = api.getBridgeVersion();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to access Bond local API through bridge");
        }
        if (myVersion != null) {
            // Update all the thing properties based on the result
            Map<String, String> thingProperties = new HashMap<String, String>();
            thingProperties.put(PROPERTY_VENDOR, myVersion.make);
            thingProperties.put(PROPERTY_MODEL_ID, myVersion.model);
            thingProperties.put(PROPERTY_SERIAL_NUMBER, myVersion.bondid);
            thingProperties.put(PROPERTY_FIRMWARE_VERSION, myVersion.firmwareVersion);
            updateProperties(thingProperties);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable get Bond bridge version via API");
        }
    }
}
