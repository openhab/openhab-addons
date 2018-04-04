/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.BRIDGE_OFFLINE;
import static org.openhab.binding.plugwise.PlugwiseBindingConstants.CHANNEL_LAST_SEEN;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.plugwise.PlugwiseBindingConstants;
import org.openhab.binding.plugwise.internal.PlugwiseDeviceTask;
import org.openhab.binding.plugwise.internal.PlugwiseMessagePriority;
import org.openhab.binding.plugwise.internal.PlugwiseUtils;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.InformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.PingRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractPlugwiseThingHandler} handles common Plugwise device channel updates and commands.
 *
 * @author Wouter Born - Initial contribution
 */
public abstract class AbstractPlugwiseThingHandler extends BaseThingHandler implements PlugwiseMessageListener {

    private static final Duration DEFAULT_UPDATE_INTERVAL = Duration.ofMinutes(1);
    private static final Duration MESSAGE_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_UNANSWERED_PINGS = 2;

    private final PlugwiseDeviceTask onlineStateUpdateTask = new PlugwiseDeviceTask("Online state update", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return MESSAGE_TIMEOUT;
        }

        @Override
        public void runTask() {
            updateOnlineState();
        }

        @Override
        public boolean shouldBeScheduled() {
            return shouldOnlineTaskBeScheduled();
        }

        @Override
        public void start() {
            unansweredPings = 0;
            super.start();
        }
    };

    private final Logger logger = LoggerFactory.getLogger(AbstractPlugwiseThingHandler.class);

    private PlugwiseStickHandler stickHandler;
    private LocalDateTime lastSeen = LocalDateTime.MIN;
    private LocalDateTime lastConfigurationUpdateSend;
    private int unansweredPings;

    public AbstractPlugwiseThingHandler(Thing thing) {
        super(thing);
    }

    protected void addMessageListener() {
        if (stickHandler != null) {
            stickHandler.addMessageListener(this, getMACAddress());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus(bridgeStatusInfo.getStatus());
    }

    @Override
    public void dispose() {
        removeMessageListener();
        onlineStateUpdateTask.stop();
    }

    protected Duration durationSinceLastSeen() {
        return Duration.between(lastSeen, LocalDateTime.now());
    }

    protected Duration getChannelUpdateInterval(String channelId) {
        Configuration configuration = thing.getChannel(channelId).getConfiguration();
        BigDecimal interval = (BigDecimal) configuration.get(PlugwiseBindingConstants.CONFIG_PROPERTY_UPDATE_INTERVAL);
        return interval != null ? Duration.ofSeconds(interval.intValue()) : DEFAULT_UPDATE_INTERVAL;
    }

    protected DeviceType getDeviceType() {
        return PlugwiseUtils.getDeviceType(thing.getThingTypeUID());
    }

    protected abstract MACAddress getMACAddress();

    protected ThingStatusDetail getThingStatusDetail() {
        return isConfigurationPending() ? ThingStatusDetail.CONFIGURATION_PENDING : ThingStatusDetail.NONE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command '{}' for {} ({}) channel '{}'", command, getDeviceType(), getMACAddress(),
                channelUID.getId());
    }

    @Override
    public void initialize() {
        updateBridgeStatus(getBridge() != null ? getBridge().getStatus() : OFFLINE);
        updateTask(onlineStateUpdateTask);

        // Add the message listener after dispose/initialize due to configuration update
        if (isInitialized()) {
            addMessageListener();
        }

        // Send configuration update commands after configuration update
        if (thing.getStatus() == ONLINE) {
            sendConfigurationUpdateCommands();
        }
    }

    protected boolean isConfigurationPending() {
        return false;
    }

    protected void ping() {
        sendMessage(new PingRequestMessage(getMACAddress()));
    }

    protected boolean recentlySendConfigurationUpdate() {
        return lastConfigurationUpdateSend != null
                && LocalDateTime.now().minus(Duration.ofMillis(500)).isBefore(lastConfigurationUpdateSend);
    }

    protected void removeMessageListener() {
        if (stickHandler != null) {
            stickHandler.removeMessageListener(this);
        }
    }

    protected abstract boolean shouldOnlineTaskBeScheduled();

    protected void sendCommandMessage(Message message) {
        if (stickHandler != null) {
            stickHandler.sendMessage(message, PlugwiseMessagePriority.COMMAND);
        }
    }

    protected void sendConfigurationUpdateCommands() {
        lastConfigurationUpdateSend = LocalDateTime.now();
        if (getThingStatusDetail() != thing.getStatusInfo().getStatusDetail()) {
            updateStatus(thing.getStatus(), getThingStatusDetail());
        }
    }

    protected void sendFastUpdateMessage(Message message) {
        if (stickHandler != null) {
            stickHandler.sendMessage(message, PlugwiseMessagePriority.FAST_UPDATE);
        }
    }

    protected void sendMessage(Message message) {
        if (stickHandler != null) {
            stickHandler.sendMessage(message, PlugwiseMessagePriority.UPDATE_AND_DISCOVERY);
        }
    }

    protected void stopTasks(List<PlugwiseDeviceTask> tasks) {
        for (PlugwiseDeviceTask task : tasks) {
            task.stop();
        }
    }

    /**
     * Updates the thing state based on that of the Stick
     */
    protected void updateBridgeStatus(ThingStatus bridgeStatus) {
        if (bridgeStatus == ONLINE && thing.getStatus() != ONLINE) {
            stickHandler = (PlugwiseStickHandler) getBridge().getHandler();
            addMessageListener();
            updateStatus(OFFLINE, getThingStatusDetail());
        } else if (bridgeStatus == OFFLINE) {
            removeMessageListener();
            updateStatus(OFFLINE, BRIDGE_OFFLINE);
        } else if (bridgeStatus == UNKNOWN) {
            removeMessageListener();
            updateStatus(UNKNOWN);
        }
    }

    protected void updateInformation() {
        sendMessage(new InformationRequestMessage(getMACAddress()));
    }

    protected void updateLastSeen() {
        unansweredPings = 0;
        lastSeen = LocalDateTime.now();
        if (isLinked(CHANNEL_LAST_SEEN)) {
            updateState(CHANNEL_LAST_SEEN, PlugwiseUtils.newDateTimeType(lastSeen));
        }
        if (thing.getStatus() == OFFLINE) {
            updateStatus(ONLINE, getThingStatusDetail());
        }
    }

    protected void updateOnlineState() {
        ThingStatus status = thing.getStatus();
        if (status == ONLINE && unansweredPings < MAX_UNANSWERED_PINGS
                && MESSAGE_TIMEOUT.minus(durationSinceLastSeen()).isNegative()) {
            ping();
            unansweredPings++;
        } else if (status == ONLINE && unansweredPings >= MAX_UNANSWERED_PINGS) {
            updateStatus(OFFLINE, getThingStatusDetail());
            unansweredPings = 0;
        } else if (status == OFFLINE) {
            ping();
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
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        ThingStatus oldStatus = thing.getStatus();
        super.updateStatus(status, statusDetail, description);

        updateTask(onlineStateUpdateTask);
        if (oldStatus != ONLINE && status == ONLINE && isConfigurationPending()) {
            sendConfigurationUpdateCommands();
        }
    }

    protected void updateStatusOnDetailChange() {
        if (thing.getStatusInfo().getStatusDetail() != getThingStatusDetail()) {
            updateStatus(thing.getStatus(), getThingStatusDetail());
        }
    }

    protected void updateTask(PlugwiseDeviceTask task) {
        if (task.shouldBeScheduled()) {
            if (!task.isScheduled() || task.getConfiguredInterval() != task.getInterval()) {
                if (task.isScheduled()) {
                    task.stop();
                }
                task.update(getDeviceType(), getMACAddress());
                task.start();
            }
        } else if (!task.shouldBeScheduled() && task.isScheduled()) {
            task.stop();
        }
    }

    protected void updateTasks(List<PlugwiseDeviceTask> tasks) {
        for (PlugwiseDeviceTask task : tasks) {
            updateTask(task);
        }
    }

}
