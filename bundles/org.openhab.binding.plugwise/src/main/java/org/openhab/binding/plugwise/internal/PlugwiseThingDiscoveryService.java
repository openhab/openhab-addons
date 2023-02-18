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
package org.openhab.binding.plugwise.internal;

import static java.util.stream.Collectors.*;
import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.handler.PlugwiseStickHandler;
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
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Plugwise devices by periodically reading the Circle+ node/MAC table with {@link RoleCallRequestMessage}s.
 * Sleeping end devices are discovered when they announce being awake with an {@link AnnounceAwakeRequestMessage}. To
 * reduce network traffic {@link InformationRequestMessage}s are only sent to undiscovered devices.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class PlugwiseThingDiscoveryService extends AbstractDiscoveryService
        implements PlugwiseMessageListener, PlugwiseStickStatusListener, ThingHandlerService {

    private static class CurrentRoleCall {
        private boolean isRoleCalling;
        private int currentNodeID;
        private int attempts;
        private Instant lastRequest = Instant.MIN;
    }

    private static class DiscoveredNode {
        private final MACAddress macAddress;
        private final Map<String, String> properties = new HashMap<>();
        private DeviceType deviceType = DeviceType.UNKNOWN;
        private int attempts;
        private Instant lastRequest = Instant.MIN;

        public DiscoveredNode(MACAddress macAddress) {
            this.macAddress = macAddress;
        }

        public boolean isDataComplete() {
            return deviceType != DeviceType.UNKNOWN && !properties.isEmpty();
        }
    }

    private static final Set<ThingTypeUID> DISCOVERED_THING_TYPES_UIDS = SUPPORTED_THING_TYPES_UIDS.stream()
            .filter(thingTypeUID -> !thingTypeUID.equals(THING_TYPE_STICK))
            .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

    private static final int MIN_NODE_ID = 0;
    private static final int MAX_NODE_ID = 63;

    private static final int MESSAGE_RETRY_ATTEMPTS = 5;

    private static final Duration DISCOVERY_INTERVAL = Duration.ofMinutes(3);
    private static final Duration MESSAGE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration WATCH_INTERVAL = Duration.ofSeconds(1);

    private final Logger logger = LoggerFactory.getLogger(PlugwiseThingDiscoveryService.class);

    private @NonNullByDefault({}) PlugwiseStickHandler stickHandler;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable ScheduledFuture<?> watchJob;
    private CurrentRoleCall currentRoleCall = new CurrentRoleCall();

    private final Map<MACAddress, DiscoveredNode> discoveredNodes = new ConcurrentHashMap<>();

    public PlugwiseThingDiscoveryService() throws IllegalArgumentException {
        super(DISCOVERED_THING_TYPES_UIDS, 1, true);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Aborting nodes discovery");
        super.abortScan();
        currentRoleCall.isRoleCalling = false;
        stopDiscoveryWatchJob();
    }

    @Override
    public void activate() {
        super.activate(new HashMap<>());
    }

    private void createDiscoveryResult(DiscoveredNode node) {
        String mac = node.macAddress.toString();
        ThingUID bridgeUID = stickHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = PlugwiseUtils.getThingTypeUID(node.deviceType);
        if (thingTypeUID != null) {
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, mac);

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel("Plugwise " + node.deviceType.toString())
                    .withProperty(PlugwiseBindingConstants.CONFIG_PROPERTY_MAC_ADDRESS, mac)
                    .withProperties(new HashMap<>(node.properties))
                    .withRepresentationProperty(PlugwiseBindingConstants.PROPERTY_MAC_ADDRESS).build());
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        stickHandler.removeMessageListener(this);
        stickHandler.removeStickStatusListener(this);
    }

    private void discoverNewNodeDetails(MACAddress macAddress) {
        if (!isAlreadyDiscovered(macAddress)) {
            logger.debug("Discovered new node ({})", macAddress);
            DiscoveredNode node = new DiscoveredNode(macAddress);
            discoveredNodes.put(macAddress, node);
            updateInformation(macAddress);
            node.lastRequest = Instant.now();
        } else {
            logger.debug("Already discovered node ({})", macAddress);
        }
    }

    protected void discoverNodes() {
        MACAddress circlePlusMAC = getCirclePlusMAC();
        if (getStickStatus() != ThingStatus.ONLINE) {
            logger.debug("Discovery with role call not possible (Stick status is {})", getStickStatus());
        } else if (circlePlusMAC == null) {
            logger.debug("Discovery with role call not possible (Circle+ MAC address is null)");
        } else if (currentRoleCall.isRoleCalling) {
            logger.debug("Discovery with role call not possible (already role calling)");
        } else {
            stickHandler.addMessageListener(this);
            discoveredNodes.clear();
            currentRoleCall.isRoleCalling = true;
            currentRoleCall.currentNodeID = Integer.MIN_VALUE;

            discoverNewNodeDetails(circlePlusMAC);

            logger.debug("Discovering nodes with role call on Circle+ ({})", circlePlusMAC);
            roleCall(MIN_NODE_ID);
            startDiscoveryWatchJob();
        }
    }

    private @Nullable MACAddress getCirclePlusMAC() {
        return stickHandler.getCirclePlusMAC();
    }

    private ThingStatus getStickStatus() {
        return stickHandler.getThing().getStatus();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return stickHandler;
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
                createDiscoveryResult(node);
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
        Thing thing = stickHandler.getThingByMAC(macAddress);
        if (thing != null) {
            logger.debug("Node ({}) has existing thing: {}", macAddress, thing.getUID());
        }
        return thing != null;
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
            currentRoleCall.lastRequest = Instant.now();
        } else {
            logger.warn("Invalid node ID for role call: {}", nodeID);
        }
    }

    private void sendMessage(Message message) {
        stickHandler.sendMessage(message, PlugwiseMessagePriority.UPDATE_AND_DISCOVERY);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof PlugwiseStickHandler) {
            stickHandler = (PlugwiseStickHandler) handler;
            stickHandler.addStickStatusListener(this);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Plugwise device background discovery");

        Runnable discoveryRunnable = () -> {
            logger.debug("Discover nodes (background discovery)");
            discoverNodes();
        };

        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(discoveryRunnable, 0, DISCOVERY_INTERVAL.toMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private void startDiscoveryWatchJob() {
        logger.debug("Starting Plugwise discovery watch job");

        Runnable watchRunnable = () -> {
            if (currentRoleCall.isRoleCalling) {
                Duration durationSinceLastRequest = Duration.between(currentRoleCall.lastRequest, Instant.now());
                if (durationSinceLastRequest.compareTo(MESSAGE_TIMEOUT) > 0
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
                Duration durationSinceLastRequest = Duration.between(node.lastRequest, Instant.now());
                if (durationSinceLastRequest.compareTo(MESSAGE_TIMEOUT) > 0 && node.attempts < MESSAGE_RETRY_ATTEMPTS) {
                    logger.debug("Resending timed out information request message to node ({})", node.macAddress);
                    updateInformation(node.macAddress);
                    node.lastRequest = Instant.now();
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
        };

        ScheduledFuture<?> localWatchJob = watchJob;
        if (localWatchJob == null || localWatchJob.isCancelled()) {
            watchJob = scheduler.scheduleWithFixedDelay(watchRunnable, WATCH_INTERVAL.toMillis(),
                    WATCH_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
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
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null && !localDiscoveryJob.isCancelled()) {
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }
        stopDiscoveryWatchJob();
    }

    private void stopDiscoveryWatchJob() {
        logger.debug("Stopping Plugwise discovery watch job");
        ScheduledFuture<?> localWatchJob = watchJob;
        if (localWatchJob != null && !localWatchJob.isCancelled()) {
            localWatchJob.cancel(true);
            watchJob = null;
        }
    }

    private void updateInformation(MACAddress macAddress) {
        sendMessage(new InformationRequestMessage(macAddress));
    }
}
