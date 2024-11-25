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
package org.openhab.binding.echonetlite.internal;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler for echonet lite devices. By default, all messages (inbound and outbound) happen on port 3610, so
 * we can only have a single listener for echonet lite messages. Hence, using a bridge model to handle communications
 * and discovery.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetLiteBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EchonetLiteBridgeHandler.class);
    private final ArrayBlockingQueue<Message> requests = new ArrayBlockingQueue<>(1024);
    private final Map<InstanceKey, EchonetObject> devicesByKey = new HashMap<>();
    private final EchonetMessageBuilder messageBuilder = new EchonetMessageBuilder();
    private final Thread networkingThread = new Thread(this::poll,
            "OH-binding-" + EchonetLiteBindingConstants.BINDING_ID);
    private final EchonetMessage echonetMessage = new EchonetMessage();
    private final MonotonicClock clock = new MonotonicClock();

    @Nullable
    private EchonetChannel echonetChannel;

    @Nullable
    private InstanceKey managementControllerKey;

    @Nullable
    private InstanceKey discoveryKey;

    public EchonetLiteBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    private void start(final InstanceKey managementControllerKey, InstanceKey discoveryKey) throws IOException {
        this.managementControllerKey = managementControllerKey;
        this.discoveryKey = discoveryKey;

        logger.debug("Binding echonet channel");
        echonetChannel = new EchonetChannel(discoveryKey.address);
        logger.debug("Starting networking thread");
        networkingThread.setDaemon(true);
        networkingThread.start();
    }

    public void newDevice(InstanceKey instanceKey, long pollIntervalMs, long retryTimeoutMs,
            final EchonetDeviceListener echonetDeviceListener) {
        requests.add(new NewDeviceMessage(instanceKey, pollIntervalMs, retryTimeoutMs, echonetDeviceListener));
    }

    private void newDeviceInternal(final NewDeviceMessage message) {
        final EchonetObject echonetObject = devicesByKey.get(message.instanceKey);
        if (null != echonetObject) {
            if (echonetObject instanceof EchonetDevice device) {
                logger.debug("Update item: {} already discovered", message.instanceKey);
                device.setTimeouts(message.pollIntervalMs, message.retryTimeoutMs);
                device.setListener(message.echonetDeviceListener);
            } else {
                logger.debug("Item: {} already discovered, but was not a device", message.instanceKey);
            }
        } else {
            logger.debug("New Device: {}", message.instanceKey);
            final EchonetDevice device = new EchonetDevice(message.instanceKey, message.echonetDeviceListener);
            device.setTimeouts(message.pollIntervalMs, message.retryTimeoutMs);
            devicesByKey.put(message.instanceKey, device);
        }
    }

    public void refreshDevice(final InstanceKey instanceKey, final String channelId) {
        requests.add(new RefreshMessage(instanceKey, channelId));
    }

    private void refreshDeviceInternal(final RefreshMessage refreshMessage) {
        final EchonetObject item = devicesByKey.get(refreshMessage.instanceKey);
        if (null != item) {
            item.refresh(refreshMessage.channelId);
        }
    }

    public void removeDevice(final InstanceKey instanceKey) {
        requests.add(new RemoveDevice(instanceKey));
    }

    private void removeDeviceInternal(final RemoveDevice removeDevice) {
        final EchonetObject remove = devicesByKey.remove(removeDevice.instanceKey);

        logger.debug("Removing device: {}, {}", removeDevice.instanceKey, remove);
        if (null != remove) {
            remove.removed();
        }
    }

    public void updateDevice(final InstanceKey instanceKey, final String id, final State command) {
        requests.add(new UpdateDevice(instanceKey, id, command));
    }

    public void updateDeviceInternal(UpdateDevice updateDevice) {
        final EchonetObject echonetObject = devicesByKey.get(updateDevice.instanceKey);

        if (null == echonetObject) {
            logger.warn("Device not found for update: {}", updateDevice);
            return;
        }

        echonetObject.update(updateDevice.channelId, updateDevice.state);
    }

    public void startDiscovery(EchonetDiscoveryListener echonetDiscoveryListener) {
        requests.offer(new StartDiscoveryMessage(echonetDiscoveryListener, requireNonNull(discoveryKey)));
    }

    public void startDiscoveryInternal(StartDiscoveryMessage startDiscovery) {
        devicesByKey.put(startDiscovery.instanceKey, new EchonetProfileNode(startDiscovery.instanceKey,
                this::onDiscoveredInstanceKey, startDiscovery.echonetDiscoveryListener));
    }

    public void stopDiscovery() {
        requests.offer(new StopDiscoveryMessage(requireNonNull(discoveryKey)));
    }

    private void stopDiscoveryInternal(StopDiscoveryMessage stopDiscovery) {
        devicesByKey.remove(stopDiscovery.instanceKey);
    }

    private void onDiscoveredInstanceKey(EchonetDevice device) {
        if (null == devicesByKey.putIfAbsent(device.instanceKey(), device)) {
            logger.debug("New device discovered: {}", device.instanceKey);
        }
    }

    private void pollDevices(long nowMs, EchonetChannel echonetChannel) {
        for (EchonetObject echonetObject : devicesByKey.values()) {
            if (echonetObject.buildUpdateMessage(messageBuilder, echonetChannel::nextTid, nowMs,
                    requireNonNull(managementControllerKey))) {
                try {
                    echonetChannel.sendMessage(messageBuilder);
                } catch (IOException e) {
                    logger.warn("Failed to send echonet message", e);
                }
            }

            echonetObject.refreshAll(nowMs);

            if (echonetObject.buildPollMessage(messageBuilder, echonetChannel::nextTid, nowMs,
                    requireNonNull(managementControllerKey))) {
                try {
                    echonetChannel.sendMessage(messageBuilder);
                } catch (IOException e) {
                    logger.warn("Failed to send echonet message", e);
                }
            } else {
                echonetObject.checkTimeouts();
            }
        }
    }

    private void pollRequests() {
        Message message;
        while (null != (message = requestsPoll())) {
            logger.debug("Received request: {}", message);
            if (message instanceof NewDeviceMessage deviceMessage) {
                newDeviceInternal(deviceMessage);
            } else if (message instanceof RefreshMessage refreshMessage) {
                refreshDeviceInternal(refreshMessage);
            } else if (message instanceof RemoveDevice device) {
                removeDeviceInternal(device);
            } else if (message instanceof UpdateDevice device) {
                updateDeviceInternal(device);
            } else if (message instanceof StartDiscoveryMessage discoveryMessage) {
                startDiscoveryInternal(discoveryMessage);
            } else if (message instanceof StopDiscoveryMessage discoveryMessage) {
                stopDiscoveryInternal(discoveryMessage);
            }
        }
    }

    private @Nullable Message requestsPoll() {
        return requests.poll();
    }

    private void pollNetwork(EchonetChannel echonetChannel) {
        try {
            echonetChannel.pollMessages(echonetMessage, this::onMessage,
                    EchonetLiteBindingConstants.NETWORK_WAIT_TIMEOUT);
        } catch (IOException e) {
            logger.warn("Failed to poll for messages", e);
        }
    }

    private void onMessage(final EchonetMessage echonetMessage, final SocketAddress sourceAddress) {
        final EchonetClass echonetClass = echonetMessage.sourceClass();
        if (null == echonetClass) {
            logger.warn("Unable to find echonetClass for message: {}, from: {}", echonetMessage.toDebug(),
                    sourceAddress);
            return;
        }

        final InstanceKey instanceKey = new InstanceKey((InetSocketAddress) sourceAddress, echonetClass,
                echonetMessage.instance());
        final Esv esv = echonetMessage.esv();

        EchonetObject echonetObject = devicesByKey.get(instanceKey);
        if (null == echonetObject) {
            echonetObject = devicesByKey.get(discoveryKey);
        }

        logger.debug("Message {} for: {}", esv, echonetObject);
        if (null != echonetObject) {
            echonetObject.applyHeader(esv, echonetMessage.tid(), clock.timeMs());
            while (echonetMessage.moveNext()) {
                final int epc = echonetMessage.currentEpc();
                final int pdc = echonetMessage.currentPdc();
                ByteBuffer edt = echonetMessage.currentEdt();
                echonetObject.applyProperty(instanceKey, esv, epc, pdc, edt);
            }
        }
    }

    private void poll() {
        try {
            doPoll();
            updateStatus(ThingStatus.ONLINE);

            while (!Thread.currentThread().isInterrupted()) {
                doPoll();
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void doPoll() {
        final long nowMs = clock.timeMs();
        pollRequests();
        pollDevices(nowMs, requireNonNull(echonetChannel));
        pollNetwork(requireNonNull(echonetChannel));
    }

    @Override
    public void initialize() {
        final EchonetBridgeConfig bridgeConfig = getConfigAs(EchonetBridgeConfig.class);

        final InstanceKey managementControllerKey = new InstanceKey(new InetSocketAddress(bridgeConfig.port),
                EchonetClass.MANAGEMENT_CONTROLLER, (byte) 0x01);
        final InstanceKey discoveryKey = new InstanceKey(
                new InetSocketAddress(requireNonNull(bridgeConfig.multicastAddress), bridgeConfig.port),
                EchonetClass.NODE_PROFILE, (byte) 0x01);

        updateStatus(ThingStatus.UNKNOWN);

        try {
            start(managementControllerKey, discoveryKey);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start networking thread", e);
        }
    }

    @Override
    public void dispose() {
        if (networkingThread.isAlive()) {
            networkingThread.interrupt();
            try {
                networkingThread.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                logger.debug("Interrupted while closing", e);
            }
        }

        @Nullable
        final EchonetChannel echonetChannel = this.echonetChannel;
        if (null != echonetChannel) {
            echonetChannel.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EchonetDiscoveryService.class);
    }

    private abstract static class Message {
        final InstanceKey instanceKey;

        public Message(InstanceKey instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    private static final class NewDeviceMessage extends Message {
        final long pollIntervalMs;
        final long retryTimeoutMs;
        final EchonetDeviceListener echonetDeviceListener;

        public NewDeviceMessage(final InstanceKey instanceKey, long pollIntervalMs, long retryTimeoutMs,
                final EchonetDeviceListener echonetDeviceListener) {
            super(instanceKey);
            this.pollIntervalMs = pollIntervalMs;
            this.retryTimeoutMs = retryTimeoutMs;
            this.echonetDeviceListener = echonetDeviceListener;
        }

        @Override
        public String toString() {
            return "NewDeviceMessage{" + "instanceKey=" + instanceKey + ", pollIntervalMs=" + pollIntervalMs
                    + ", retryTimeoutMs=" + retryTimeoutMs + "} " + super.toString();
        }
    }

    private static class RefreshMessage extends Message {
        private final String channelId;

        public RefreshMessage(InstanceKey instanceKey, String channelId) {
            super(instanceKey);
            this.channelId = channelId;
        }
    }

    private static class RemoveDevice extends Message {
        public RemoveDevice(final InstanceKey instanceKey) {
            super(instanceKey);
        }
    }

    private static class StartDiscoveryMessage extends Message {
        private final EchonetDiscoveryListener echonetDiscoveryListener;

        public StartDiscoveryMessage(EchonetDiscoveryListener echonetDiscoveryListener, InstanceKey discoveryKey) {
            super(discoveryKey);
            this.echonetDiscoveryListener = echonetDiscoveryListener;
        }
    }

    private static class StopDiscoveryMessage extends Message {
        public StopDiscoveryMessage(InstanceKey discoveryKey) {
            super(discoveryKey);
        }
    }

    private static class UpdateDevice extends Message {
        private final String channelId;
        private final State state;

        public UpdateDevice(final InstanceKey instanceKey, final String channelId, final State state) {
            super(instanceKey);
            this.channelId = channelId;
            this.state = state;
        }

        @Override
        public String toString() {
            return "UpdateDevice{" + "instanceKey=" + instanceKey + ", channelId='" + channelId + '\'' + ", state="
                    + state + "} " + super.toString();
        }
    }
}
