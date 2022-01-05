/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.DISCOVERY_KEY;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openhab.core.types.State;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
@Component(service = EchonetMessengerService.class, immediate = true, configurationPid = "networking.echonetlite")
public class EchonetMessenger implements EchonetMessengerService {

    private final Logger logger = LoggerFactory.getLogger(EchonetMessenger.class);
    private final ArrayBlockingQueue<Message> requests = new ArrayBlockingQueue<>(1024);
    private final Map<InstanceKey, EchonetItem> devicesByKey = new HashMap<>();
    private final EchonetMessageBuilder messageBuilder = new EchonetMessageBuilder();
    private final Thread networkingThread = new Thread(this::poll);
    private final EchonetMessage echonetMessage = new EchonetMessage();
    private final Clock clock = Clock.systemUTC();
    private EchonetChannel echonetChannel;

    protected void activate(final ComponentContext componentContext) {
        logger.info("Activating");
        try {
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void deactivate(final ComponentContext componentContext) {
        dispose();
    }

    public void start() throws IOException {
        logger.info("Binding echonet channel");
        echonetChannel = new EchonetChannel();
        logger.info("Starting networking thread");

        networkingThread.setName("Echonet Networking");
        networkingThread.setDaemon(true);
        networkingThread.start();
    }

    @Override
    public void newDevice(final InstanceKey instanceKey, final EchonetDeviceListener echonetDeviceListener) {
        requests.add(new NewDeviceMessage(instanceKey, echonetDeviceListener));
    }

    private void newDeviceInternal(final NewDeviceMessage message) {

        final EchonetItem echonetItem = devicesByKey.get(message.instanceKey);
        if (null != echonetItem) {
            if (echonetItem instanceof EchonetDevice) {
                logger.debug("Update item: {} already discovered", message.instanceKey);
                EchonetDevice device = (EchonetDevice) echonetItem;
                device.setListener(message.echonetDeviceListener);
            } else {
                logger.debug("Item: {} already discovered, but was not a device", message.instanceKey);
            }
        } else {
            logger.debug("New Device: {}", message.instanceKey);
            devicesByKey.put(message.instanceKey,
                    new EchonetDevice(message.instanceKey, message.echonetDeviceListener));
        }
    }

    @Override
    public void refreshDevice(final InstanceKey instanceKey, final String channelId) {
        requests.add(new RefreshMessage(instanceKey, channelId));
    }

    private void refreshDeviceInternal(final RefreshMessage refreshMessage, long nowMs) {
        final EchonetItem item = devicesByKey.get(refreshMessage.instanceKey);
        item.refresh(refreshMessage.channelId);
    }

    @Override
    public void removeDevice(final InstanceKey instanceKey) {
        requests.add(new RemoveDevice(instanceKey));
    }

    private void removeDeviceInternal(final RemoveDevice removeDevice) {
        final EchonetItem remove = devicesByKey.remove(removeDevice.instanceKey);

        logger.info("Removing device: {}, {}", removeDevice.instanceKey, remove);
        if (null != remove) {
            remove.removed();
        }
    }

    @Override
    public void updateDevice(final InstanceKey instanceKey, final String id, final State command) {
        requests.add(new UpdateDevice(instanceKey, id, command));
    }

    public void updateDeviceInternal(UpdateDevice updateDevice) {
        final EchonetItem echonetItem = devicesByKey.get(updateDevice.instanceKey);

        if (null == echonetItem) {
            logger.warn("Device not found for update: {}", updateDevice);
            return;
        }

        echonetItem.update(updateDevice.channelId, updateDevice.state);
    }

    @Override
    public void startDiscovery(EchonetDiscoveryListener echonetDiscoveryListener) {
        requests.offer(new StartDiscoveryMessage(echonetDiscoveryListener));
    }

    public void startDiscoveryInternal(StartDiscoveryMessage startDiscovery) {
        devicesByKey.put(startDiscovery.instanceKey, new EchonetProfileNode(startDiscovery.instanceKey,
                this::onDiscoveredInstanceKey, startDiscovery.echonetDiscoveryListener));
    }

    @Override
    public void stopDiscovery() {
        requests.offer(new StopDiscoveryMessage());
    }

    private void stopDiscoveryInternal(StopDiscoveryMessage stopDiscovery) {
        devicesByKey.remove(stopDiscovery.instanceKey);
    }

    private void onDiscoveredInstanceKey(EchonetDevice device) {
        if (null == devicesByKey.putIfAbsent(device.instanceKey(), device)) {
            logger.debug("New device discovered: {}", device.instanceKey);
        }
    }

    private void pollDevices(long nowMs) {
        for (EchonetItem echonetItem : devicesByKey.values()) {
            if (echonetItem.buildUpdateMessage(messageBuilder, echonetChannel::nextTid, nowMs)) {
                try {
                    echonetChannel.sendMessage(messageBuilder);
                } catch (IOException e) {
                    logger.error("Failed to send echonet message", e);
                }
            }

            echonetItem.refreshAll(nowMs);

            if (echonetItem.buildPollMessage(messageBuilder, echonetChannel::nextTid, nowMs)) {
                try {
                    echonetChannel.sendMessage(messageBuilder);
                } catch (IOException e) {
                    logger.error("Failed to send echonet message", e);
                }
            }
        }
    }

    private void pollRequests(long nowMs) {
        Message message;
        while (null != (message = requests.poll())) {
            logger.info("Received request: {}", message);
            if (message instanceof NewDeviceMessage) {
                newDeviceInternal((NewDeviceMessage) message);
            } else if (message instanceof RefreshMessage) {
                refreshDeviceInternal((RefreshMessage) message, nowMs);
            } else if (message instanceof RemoveDevice) {
                removeDeviceInternal((RemoveDevice) message);
            } else if (message instanceof UpdateDevice) {
                updateDeviceInternal((UpdateDevice) message);
            } else if (message instanceof StartDiscoveryMessage) {
                startDiscoveryInternal((StartDiscoveryMessage) message);
            } else if (message instanceof StopDiscoveryMessage) {
                stopDiscoveryInternal((StopDiscoveryMessage) message);
            }
        }
    }

    private void pollNetwork(long nowMs) {
        try {
            echonetChannel.pollMessages(echonetMessage, this::onMessage, 250);
        } catch (IOException e) {
            logger.error("Failed to poll for messages", e);
        }
    }

    private void onMessage(final EchonetMessage echonetMessage) {
        final EchonetClass echonetClass = echonetMessage.sourceClass();
        if (null == echonetClass) {
            return;
        }

        final InstanceKey instanceKey = new InstanceKey((InetSocketAddress) echonetMessage.sourceAddress(),
                echonetClass, echonetMessage.instance());
        final Esv esv = echonetMessage.esv();

        EchonetItem echonetItem = devicesByKey.get(instanceKey);
        if (null == echonetItem) {
            echonetItem = devicesByKey.get(DISCOVERY_KEY);
        }

        logger.debug("Message {} for: {}", esv, echonetItem);
        if (null != echonetItem) {
            while (echonetMessage.moveNext()) {
                final int epc = echonetMessage.currentEpc();
                final int pdc = echonetMessage.currentPdc();
                ByteBuffer edt = echonetMessage.currentEdt();
                echonetItem.applyResponse(instanceKey, esv, epc, pdc, edt);
            }
        }
    }

    private void poll() {
        while (!Thread.currentThread().isInterrupted()) {
            final long nowMs = clock.millis();
            pollRequests(nowMs);
            pollDevices(nowMs);
            pollNetwork(nowMs);
        }
    }

    public void dispose() {
        if (networkingThread.isAlive()) {
            networkingThread.interrupt();
            try {
                networkingThread.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                logger.warn("Interrupted while closing", e);
            }
        }
        if (null != echonetChannel) {
            echonetChannel.close();
        }
    }

    private abstract static class Message {
        final InstanceKey instanceKey;

        public Message(InstanceKey instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    private static final class NewDeviceMessage extends Message {
        final EchonetDeviceListener echonetDeviceListener;

        public NewDeviceMessage(final InstanceKey instanceKey, final EchonetDeviceListener echonetDeviceListener) {
            super(instanceKey);
            this.echonetDeviceListener = echonetDeviceListener;
        }
    }

    private static class RefreshMessage extends Message {
        private String channelId;

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

        public StartDiscoveryMessage(EchonetDiscoveryListener echonetDiscoveryListener) {
            super(DISCOVERY_KEY);
            this.echonetDiscoveryListener = echonetDiscoveryListener;
        }
    }

    private static class StopDiscoveryMessage extends Message {
        public StopDiscoveryMessage() {
            super(DISCOVERY_KEY);
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

        public String toString() {
            return "UpdateDevice{" + "instanceKey=" + instanceKey + ", channelId='" + channelId + '\'' + ", state="
                    + state + "} " + super.toString();
        }
    }
}
