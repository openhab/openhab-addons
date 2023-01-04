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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.CONFIG_PROPERTY_MAC_ADDRESS;
import static org.openhab.binding.plugwise.internal.protocol.field.DeviceType.STICK;
import static org.openhab.core.thing.ThingStatus.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.PlugwiseCommunicationHandler;
import org.openhab.binding.plugwise.internal.PlugwiseDeviceTask;
import org.openhab.binding.plugwise.internal.PlugwiseInitializationException;
import org.openhab.binding.plugwise.internal.PlugwiseMessagePriority;
import org.openhab.binding.plugwise.internal.PlugwiseThingDiscoveryService;
import org.openhab.binding.plugwise.internal.PlugwiseUtils;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.listener.PlugwiseStickStatusListener;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage.ExtensionCode;
import org.openhab.binding.plugwise.internal.protocol.InformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.NetworkStatusRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.NetworkStatusResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The {@link PlugwiseStickHandler} handles channel updates and commands for a Plugwise Stick device.
 * </p>
 * <p>
 * The Stick is an USB Zigbee controller that communicates with the Circle+. It is a {@link Bridge} to the devices on a
 * Plugwise Zigbee mesh network.
 * </p>
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class PlugwiseStickHandler extends BaseBridgeHandler implements PlugwiseMessageListener {

    private final PlugwiseDeviceTask onlineStateUpdateTask = new PlugwiseDeviceTask("Online state update", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return Duration.ofSeconds(20);
        }

        @Override
        public void runTask() {
            initialize();
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == OFFLINE;
        }
    };

    private final Logger logger = LoggerFactory.getLogger(PlugwiseStickHandler.class);
    private final PlugwiseCommunicationHandler communicationHandler;
    private final List<PlugwiseStickStatusListener> statusListeners = new CopyOnWriteArrayList<>();

    private PlugwiseStickConfig configuration = new PlugwiseStickConfig();

    private @Nullable MACAddress circlePlusMAC;
    private @Nullable MACAddress stickMAC;

    public PlugwiseStickHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        communicationHandler = new PlugwiseCommunicationHandler(bridge.getUID(), () -> configuration,
                serialPortManager);
    }

    public void addMessageListener(PlugwiseMessageListener listener) {
        communicationHandler.addMessageListener(listener);
    }

    public void addMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        communicationHandler.addMessageListener(listener, macAddress);
    }

    public void addStickStatusListener(PlugwiseStickStatusListener listener) {
        statusListeners.add(listener);
        listener.stickStatusChanged(thing.getStatus());
    }

    @Override
    public void dispose() {
        communicationHandler.stop();
        communicationHandler.removeMessageListener(this);
        onlineStateUpdateTask.stop();
    }

    public @Nullable MACAddress getCirclePlusMAC() {
        return circlePlusMAC;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(PlugwiseThingDiscoveryService.class);
    }

    public @Nullable MACAddress getStickMAC() {
        return stickMAC;
    }

    public @Nullable Thing getThingByMAC(MACAddress macAddress) {
        for (Thing thing : getThing().getThings()) {
            String thingMAC = (String) thing.getConfiguration().get(CONFIG_PROPERTY_MAC_ADDRESS);
            if (thingMAC != null && macAddress.equals(new MACAddress(thingMAC))) {
                return thing;
            }
        }

        return null;
    }

    private void handleAcknowledgement(AcknowledgementMessage acknowledge) {
        if (acknowledge.isExtended() && acknowledge.getExtensionCode() == ExtensionCode.CIRCLE_PLUS) {
            circlePlusMAC = acknowledge.getMACAddress();
            logger.debug("Received extended acknowledgement, Circle+ MAC: {}", circlePlusMAC);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command, channelUID: {}, command: {}", channelUID, command);
    }

    private void handleDeviceInformationResponse(InformationResponseMessage message) {
        if (message.getDeviceType() == STICK) {
            updateProperties(message);
        }
    }

    private void handleNetworkStatusResponse(NetworkStatusResponseMessage message) {
        stickMAC = message.getMACAddress();
        if (message.isOnline()) {
            circlePlusMAC = message.getCirclePlusMAC();
            logger.debug("The network is online: circlePlusMAC={}, stickMAC={}", circlePlusMAC, stickMAC);
            updateStatus(ONLINE);
            sendMessage(new InformationRequestMessage(stickMAC));
        } else {
            logger.debug("The network is offline: circlePlusMAC={}, stickMAC={}", circlePlusMAC, stickMAC);
            updateStatus(OFFLINE);
        }
    }

    @Override
    public void handleReponseMessage(Message message) {
        switch (message.getType()) {
            case ACKNOWLEDGEMENT_V1:
            case ACKNOWLEDGEMENT_V2:
                handleAcknowledgement((AcknowledgementMessage) message);
                break;
            case DEVICE_INFORMATION_RESPONSE:
                handleDeviceInformationResponse((InformationResponseMessage) message);
                break;
            case NETWORK_STATUS_RESPONSE:
                handleNetworkStatusResponse((NetworkStatusResponseMessage) message);
                break;
            default:
                logger.trace("Received unhandled {} message from {}", message.getType(), message.getMACAddress());
                break;
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PlugwiseStickConfig.class);
        communicationHandler.addMessageListener(this);

        try {
            communicationHandler.start();
            sendMessage(new NetworkStatusRequestMessage());
        } catch (PlugwiseInitializationException e) {
            communicationHandler.stop();
            communicationHandler.removeMessageListener(this);
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void removeMessageListener(PlugwiseMessageListener listener) {
        communicationHandler.removeMessageListener(listener);
    }

    public void removeMessageListener(PlugwiseMessageListener listener, MACAddress macAddress) {
        communicationHandler.addMessageListener(listener, macAddress);
    }

    public void removeStickStatusListener(PlugwiseStickStatusListener listener) {
        statusListeners.remove(listener);
    }

    private void sendMessage(Message message) {
        sendMessage(message, PlugwiseMessagePriority.UPDATE_AND_DISCOVERY);
    }

    public void sendMessage(Message message, PlugwiseMessagePriority priority) {
        try {
            communicationHandler.sendMessage(message, priority);
        } catch (IOException e) {
            communicationHandler.stop();
            communicationHandler.removeMessageListener(this);
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void updateProperties(InformationResponseMessage message) {
        Map<String, String> properties = editProperties();
        boolean update = PlugwiseUtils.updateProperties(properties, message);

        if (update) {
            updateProperties(properties);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String comment) {
        ThingStatus oldStatus = thing.getStatus();
        super.updateStatus(status, detail, comment);
        ThingStatus newStatus = thing.getStatus();

        if (!oldStatus.equals(newStatus)) {
            logger.debug("Updating listeners with status {}", status);
            for (PlugwiseStickStatusListener listener : statusListeners) {
                listener.stickStatusChanged(status);
            }
            updateTask(onlineStateUpdateTask);
        }
    }

    protected void updateTask(PlugwiseDeviceTask task) {
        if (task.shouldBeScheduled()) {
            if (!task.isScheduled() || !task.getConfiguredInterval().equals(task.getInterval())) {
                if (task.isScheduled()) {
                    task.stop();
                }
                task.update(DeviceType.STICK, getStickMAC());
                task.start();
            }
        } else if (!task.shouldBeScheduled() && task.isScheduled()) {
            task.stop();
        }
    }
}
