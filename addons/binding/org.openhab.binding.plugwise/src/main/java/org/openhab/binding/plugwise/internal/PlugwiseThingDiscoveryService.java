/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.PlugwiseBindingConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.plugwise.PlugwiseBindingConstants;
import org.openhab.binding.plugwise.handler.PlugwiseStickHandler;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.listener.PlugwiseStickStatusListener;
import org.openhab.binding.plugwise.internal.protocol.AnnounceAwakeRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.RoleCallRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.RoleCallResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Discovers Plugwise devices by periodically reading the Circle+ node/MAC table with {@link RoleCallRequestMessage}s.
 * Sleeping end devices are discovered when they announce being awake with a {@link AnnounceAwakeRequestMessage}. To
 * reduce network traffic {@link InformationRequestMessage}s are only sent to undiscovered devices.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseThingDiscoveryService extends AbstractDiscoveryService
        implements ExtendedDiscoveryService, PlugwiseMessageListener, PlugwiseStickStatusListener {

    private static class CurrentRoleCall {
        private boolean isRoleCalling;
        private int currentNodeID;
        private int attempts;
        private long lastRequestMillis;
    }

    private static class DiscoveredNode {

        private MACAddress macAddress;
        private DeviceType deviceType = DeviceType.UNKNOWN;
        private Map<String, String> properties = new HashMap<>();
        private int attempts;
        private long lastRequestMillis;

        public DiscoveredNode(MACAddress macAddress) {
            this.macAddress = macAddress;
        }

        public boolean isDataComplete() {
            return macAddress != null && deviceType != DeviceType.UNKNOWN && !properties.isEmpty();
        }

    }

    private static final Set<ThingTypeUID> DISCOVERED_THING_TYPES_UIDS = Sets.difference(SUPPORTED_THING_TYPES_UIDS,
            Sets.newHashSet(THING_TYPE_STICK));

    private static final int MIN_NODE_ID = 0;
    private static final int MAX_NODE_ID = 63;
    private static final int DISCOVERY_INTERVAL = 180;

    private static final int WATCH_INTERVAL = 1;

    private static final int MESSAGE_TIMEOUT = 15;
    private static final int MESSAGE_RETRY_ATTEMPTS = 5;

    private final Logger logger = LoggerFactory.getLogger(PlugwiseThingDiscoveryService.class);

    private PlugwiseStickHandler stickHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    private ScheduledFuture<?> discoveryJob;
    private ScheduledFuture<?> watchJob;
    private CurrentRoleCall currentRoleCall = new CurrentRoleCall();

    private Map<MACAddress, DiscoveredNode> discoveredNodes = new ConcurrentHashMap<>();

    public PlugwiseThingDiscoveryService(PlugwiseStickHandler stickHandler) throws IllegalArgumentException {
        super(DISCOVERED_THING_TYPES_UIDS, 1, true);
        this.stickHandler = stickHandler;
        this.stickHandler.addStickStatusListener(this);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Aborting nodes discovery");
        super.abortScan();
        currentRoleCall.isRoleCalling = false;
        stopDiscoveryWatchJob();
    }

    public void activate() {
        super.activate(new HashMap<>());
    }

    private DiscoveryResult createDiscoveryResult(DiscoveredNode node) {
        String mac = node.macAddress.toString();
        ThingUID thingUID = new ThingUID(PlugwiseUtils.getThingTypeUID(node.deviceType), mac);

        return DiscoveryResultBuilder.create(thingUID).withBridge(stickHandler.getThing().getUID())
                .withLabel("Plugwise " + node.deviceType.toString())
                .withProperty(PlugwiseBindingConstants.CONFIG_PROPERTY_MAC_ADDRESS, mac)
                .withProperties(new HashMap<>(node.properties))
                .withRepresentationProperty(PlugwiseBindingConstants.PROPERTY_MAC_ADDRESS).build();
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        stickHandler.removeMessageListener(this);
        stickHandler.removeStickStatusListener(this);
    }

    private void discoverNewNodeDetails(MACAddress macAddress) {
        if (!isAlreadyDiscovered(macAddress)) {
            logger.debug("Discovered new node ({})", macAddress);
            discoveredNodes.put(macAddress, new DiscoveredNode(macAddress));
            updateInformation(macAddress);
        } else {
            logger.debug("Already discovered node ({})", macAddress);
        }
    }

    protected void discoverNodes() {
        if (getStickStatus() != ThingStatus.ONLINE) {
            logger.debug("Discovery with role call not possible (Stick status is {})", getStickStatus());
        } else if (getCirclePlusMAC() == null) {
            logger.debug("Discovery with role call not possible (Circle+ MAC address is null)");
        } else if (currentRoleCall.isRoleCalling) {
            logger.debug("Discovery with role call not possible (already role calling)");
        } else {
            stickHandler.addMessageListener(this);
            discoveredNodes.clear();
            currentRoleCall.isRoleCalling = true;
            currentRoleCall.currentNodeID = Integer.MIN_VALUE;

            discoverNewNodeDetails(getCirclePlusMAC());

            logger.debug("Discovering nodes with role call on Circle+ ({})", getCirclePlusMAC());
            roleCall(MIN_NODE_ID);
            startDiscoveryWatchJob();
        }
    }

    private MACAddress getCirclePlusMAC() {
        return stickHandler != null ? stickHandler.getCirclePlusMAC() : null;
    }

    private ThingStatus getStickStatus() {
        return stickHandler != null ? stickHandler.getThing().getStatus() : ThingStatus.UNKNOWN;
    }

    private void handleAnnounceAwakeRequest(AnnounceAwakeRequestMessage message) {
        discoverNewNodeDetails(message.getMACAddress());
    }

    private void handleInformationResponse(InformationResponseMessage message) {
        MACAddress mac = message.getMACAddress();
        DiscoveredNode node = discoveredNodes.get(mac);
        if (node != null) {
            node.deviceType = message.getDeviceType();
            PlugwiseUtils.updateProperties(node.properties, message);
            if (node.isDataComplete()) {
                thingDiscovered(createDiscoveryResult(node));
                discoveredNodes.remove(mac);
                logger.debug("Finished discovery of {} ({})", node.deviceType, mac);
            }
        } else {
            logger.debug("Received information response for already discovered node ({})", mac);
        }
    }

    @Override
    public void handleReponseMessage(Message message) {
        switch (message.getType()) {
            case ANNOUNCE_AWAKE_REQUEST:
                handleAnnounceAwakeRequest((AnnounceAwakeRequestMessage) message);
                break;
            case DEVICE_INFORMATION_RESPONSE:
                handleInformationResponse((InformationResponseMessage) message);
                break;
            case DEVICE_ROLE_CALL_RESPONSE:
                handleRoleCallResponse((RoleCallResponseMessage) message);
                break;
            default:
                logger.trace("Received unhandled {} message from {}", message.getType(), message.getMACAddress());
                break;
        }
    }

    private void handleRoleCallResponse(RoleCallResponseMessage message) {
        logger.debug("Node with ID {} has MAC address: {}", message.getNodeID(), message.getNodeMAC());

        if (message.getNodeID() <= MAX_NODE_ID && (message.getNodeMAC() != null)) {
            discoverNewNodeDetails(message.getNodeMAC());
            // Check if there is any other on the network
            int nextNodeID = message.getNodeID() + 1;
            if (nextNodeID <= MAX_NODE_ID) {
                roleCall(nextNodeID);
            } else {
                currentRoleCall.isRoleCalling = false;
            }
        } else {
            currentRoleCall.isRoleCalling = false;
        }

        if (!currentRoleCall.isRoleCalling) {
            logger.debug("Finished discovering devices with role call on Circle+ ({})", getCirclePlusMAC());
        }
    }

    private boolean isAlreadyDiscovered(MACAddress macAddress) {
        for (ThingTypeUID thingTypeUID : DISCOVERED_THING_TYPES_UIDS) {
            ThingUID thingUID = new ThingUID(thingTypeUID, macAddress.toString());
            if (discoveryServiceCallback == null) {
                logger.debug("Assuming Node ({}) has not yet been discovered (callback null)", macAddress);
                return false;
            } else if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
                logger.debug("Node ({}) has existing discovery result: {}", macAddress, thingUID);
                return true;
            } else if (discoveryServiceCallback.getExistingThing(thingUID) != null) {
                logger.debug("Node ({}) has existing thing: {}", macAddress, thingUID);
                return true;
            }
        }
        logger.debug("Node ({}) has not yet been discovered", macAddress);
        return false;
    }

    /**
     * Role calling is basically asking the Circle+ to return all the devices known to it. Up to 64 devices
     * are supported in a Plugwise network, and role calling is done by sequentially sending
     * {@link RoleCallRequestMessage} for all possible IDs in the network (0 <= ID <= 63)
     *
     * @param nodeID of the device to role call
     */
    private void roleCall(int nodeID) {
        if (MIN_NODE_ID <= nodeID && nodeID <= MAX_NODE_ID) {
            sendMessage(new RoleCallRequestMessage(getCirclePlusMAC(), nodeID));
            if (nodeID != currentRoleCall.currentNodeID) {
                currentRoleCall.attempts = 0;
            } else {
                currentRoleCall.attempts++;
            }
            currentRoleCall.currentNodeID = nodeID;
            currentRoleCall.lastRequestMillis = System.currentTimeMillis();
        } else {
            logger.warn("Invalid node ID for role call: {}", nodeID);
        }
    }

    private void sendMessage(Message message) {
        stickHandler.sendMessage(message, PlugwiseMessagePriority.UPDATE_AND_DISCOVERY);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Plugwise device background discovery");

        Runnable discoveryRunnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("Discover nodes (background discovery)");
                discoverNodes();
            }
        };

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(discoveryRunnable, 0, DISCOVERY_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private void startDiscoveryWatchJob() {
        logger.debug("Starting Plugwise discovery watch job");

        Runnable watchRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentRoleCall.isRoleCalling) {
                    if ((System.currentTimeMillis() - currentRoleCall.lastRequestMillis) > (MESSAGE_TIMEOUT * 1000)
                            && currentRoleCall.attempts < MESSAGE_RETRY_ATTEMPTS) {
                        logger.debug("Resending timed out role call message for node with ID {} on Circle+ ({})",
                                currentRoleCall.currentNodeID, getCirclePlusMAC());
                        roleCall(currentRoleCall.currentNodeID);
                    } else if (currentRoleCall.attempts >= MESSAGE_RETRY_ATTEMPTS) {
                        logger.debug("Giving up on role call for node with ID {} on Circle+ ({})",
                                currentRoleCall.currentNodeID, getCirclePlusMAC());
                        currentRoleCall.isRoleCalling = false;
                    }
                }

                Iterator<Entry<MACAddress, DiscoveredNode>> it = discoveredNodes.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<MACAddress, DiscoveredNode> entry = it.next();
                    DiscoveredNode node = entry.getValue();
                    if ((System.currentTimeMillis() - node.lastRequestMillis) > (MESSAGE_TIMEOUT * 1000)
                            && node.attempts < MESSAGE_RETRY_ATTEMPTS) {
                        logger.debug("Resending timed out information request message to node ({})", node.macAddress);
                        updateInformation(node.macAddress);
                        node.attempts++;
                    } else if (node.attempts >= MESSAGE_RETRY_ATTEMPTS) {
                        logger.debug("Giving up on information request for node ({})", node.macAddress);
                        it.remove();
                    }
                }

                if (!currentRoleCall.isRoleCalling && discoveredNodes.isEmpty()) {
                    logger.debug("Discovery no longer needs to be watched");
                    stopDiscoveryWatchJob();
                }
            }
        };

        if (watchJob == null || watchJob.isCancelled()) {
            watchJob = scheduler.scheduleWithFixedDelay(watchRunnable, WATCH_INTERVAL, WATCH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Discover nodes (manual discovery)");
        discoverNodes();
    }

    @Override
    public void stickStatusChanged(ThingStatus status) {
        if (status.equals(ThingStatus.ONLINE)) {
            logger.debug("Discover nodes (Stick online)");
            discoverNodes();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Plugwise device background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
        stopDiscoveryWatchJob();
    }

    private void stopDiscoveryWatchJob() {
        logger.debug("Stopping Plugwise discovery watch job");
        if (watchJob != null && !watchJob.isCancelled()) {
            watchJob.cancel(true);
            watchJob = null;
        }
    }

    private void updateInformation(MACAddress macAddress) {
        sendMessage(new InformationRequestMessage(macAddress));
    }
}
