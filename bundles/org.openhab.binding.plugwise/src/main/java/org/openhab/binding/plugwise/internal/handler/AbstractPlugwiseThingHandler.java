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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.CHANNEL_LAST_SEEN;
import static org.openhab.core.thing.ThingStatus.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.PlugwiseBindingConstants;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractPlugwiseThingHandler} handles common Plugwise device channel updates and commands.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
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

    private LocalDateTime lastSeen = LocalDateTime.MIN;
    private @Nullable PlugwiseStickHandler stickHandler;
    private @Nullable LocalDateTime lastConfigurationUpdateSend;
    private int unansweredPings;

    protected AbstractPlugwiseThingHandler(Thing thing) {
        super(thing);
    }

    protected void addMessageListener() {
        if (stickHandler != null) {
            stickHandler.addMessageListener(this, getMACAddress());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus();
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
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            return DEFAULT_UPDATE_INTERVAL;
        }
        BigDecimal interval = (BigDecimal) channel.getConfiguration()
                .get(PlugwiseBindingConstants.CONFIG_PROPERTY_UPDATE_INTERVAL);
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
        updateBridgeStatus();
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
        LocalDateTime lastConfigurationUpdateSend = this.lastConfigurationUpdateSend;
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
    protected void updateBridgeStatus() {
        Bridge bridge = getBridge();
        ThingStatus bridgeStatus = bridge != null ? bridge.getStatus() : null;
        if (bridge == null) {
            removeMessageListener();
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridgeStatus == ONLINE && thing.getStatus() != ONLINE) {
            stickHandler = (PlugwiseStickHandler) bridge.getHandler();
            addMessageListener();
            updateStatus(OFFLINE, getThingStatusDetail());
        } else if (bridgeStatus == OFFLINE) {
            removeMessageListener();
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
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
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
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
            if (!task.isScheduled() || !task.getConfiguredInterval().equals(task.getInterval())) {
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
