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
package org.openhab.binding.flicbutton.internal.handler;

import static org.openhab.binding.flicbutton.internal.FlicButtonBindingConstants.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.BatteryStatusListener;
import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.DisconnectReason;

/**
 * The {@link FlicButtonHandler} is responsible for initializing the online status of Flic Buttons
 * and trigger channel events when they're used.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class FlicButtonHandler extends ChildThingHandler<FlicDaemonBridgeHandler> {

    private Logger logger = LoggerFactory.getLogger(FlicButtonHandler.class);
    private @Nullable ScheduledFuture<?> delayedDisconnectTask;
    private @Nullable Future<?> initializationTask;
    private @Nullable DisconnectReason latestDisconnectReason;
    private @Nullable ButtonConnectionChannel eventConnection;
    private @Nullable Bdaddr bdaddr;
    private @Nullable BatteryStatusListener batteryConnection;

    public FlicButtonHandler(Thing thing) {
        super(thing);
    }

    public @Nullable Bdaddr getBdaddr() {
        return bdaddr;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Pure sensor -> no commands have to be handled
    }

    @Override
    public void initialize() {
        super.initialize();
        bdaddr = new Bdaddr((String) this.getThing().getConfiguration().get(CONFIG_ADDRESS));
        if (bridgeValid) {
            initializationTask = scheduler.submit(this::initializeThing);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE && bridgeValid) {
            dispose();
            initializationTask = scheduler.submit(this::initializeThing);
        }
    }

    private void initializeThing() {
        try {
            initializeBatteryListener();
            initializeEventListener();
            // EventListener calls initializeStatus() before releasing so that ThingStatus should be set at this point
            if (this.getThing().getStatus().equals(ThingStatus.INITIALIZING)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Got no response by eventListener");
            }
        } catch (IOException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection setup failed: {}" + e.getMessage());
        }
    }

    private void initializeBatteryListener() throws IOException {
        FlicButtonBatteryLevelListener batteryListener = new FlicButtonBatteryLevelListener(this);
        BatteryStatusListener batteryConnection = new BatteryStatusListener(getBdaddr(), batteryListener);
        bridgeHandler.getFlicClient().addBatteryStatusListener(batteryConnection);
        this.batteryConnection = batteryConnection;
    }

    public void initializeEventListener() throws IOException, InterruptedException {
        FlicButtonEventListener eventListener = new FlicButtonEventListener(this);
        ButtonConnectionChannel eventConnection = new ButtonConnectionChannel(getBdaddr(), eventListener);
        bridgeHandler.getFlicClient().addConnectionChannel(eventConnection);
        this.eventConnection = eventConnection;
        eventListener.getChannelResponseSemaphore().tryAcquire(5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        cancelDelayedDisconnectTask();
        cancelInitializationTask();
        try {
            if (eventConnection != null) {
                bridgeHandler.getFlicClient().removeConnectionChannel(eventConnection);
            }
            if (batteryConnection != null) {
                bridgeHandler.getFlicClient().removeBatteryStatusListener(this.batteryConnection);
            }
        } catch (IOException e) {
            logger.warn("Button channel could not be properly removed", e);
        }

        super.dispose();
    }

    void initializeStatus(ConnectionStatus connectionStatus) {
        if (connectionStatus == ConnectionStatus.Disconnected) {
            setOffline();
        } else {
            setOnline();
        }
    }

    void connectionStatusChanged(ConnectionStatus connectionStatus, @Nullable DisconnectReason disconnectReason) {
        latestDisconnectReason = disconnectReason;
        if (connectionStatus == ConnectionStatus.Disconnected) {
            // Status change to offline have to be scheduled to improve stability,
            // see https://github.com/pfink/openhab2-flicbutton/issues/2
            scheduleStatusChangeToOffline();
        } else {
            setOnline();
        }
    }

    private void scheduleStatusChangeToOffline() {
        if (delayedDisconnectTask == null) {
            delayedDisconnectTask = scheduler.schedule(this::setOffline, BUTTON_OFFLINE_GRACE_PERIOD_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    protected void setOnline() {
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    protected void setOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE,
                "Disconnect Reason: " + Objects.toString(latestDisconnectReason));
    }

    // Cleanup delayedDisconnect on status change to online
    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (status == ThingStatus.ONLINE) {
            cancelDelayedDisconnectTask();
        }
        super.updateStatus(status, statusDetail, description);
    }

    private void cancelInitializationTask() {
        if (initializationTask != null) {
            initializationTask.cancel(true);
            initializationTask = null;
        }
    }

    private void cancelDelayedDisconnectTask() {
        if (delayedDisconnectTask != null) {
            delayedDisconnectTask.cancel(false);
            delayedDisconnectTask = null;
        }
    }

    void updateBatteryChannel(int percent) {
        DecimalType batteryLevel = new DecimalType(percent);
        updateState(CHANNEL_ID_BATTERY_LEVEL, batteryLevel);
    }

    void flicButtonRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE,
                "Button was removed/detached from flicd (e.g. by simpleclient).");
    }

    void fireTriggerEvent(String event) {
        String channelID = event.equals(CommonTriggerEvents.PRESSED) || event.equals(CommonTriggerEvents.RELEASED)
                ? CHANNEL_ID_RAWBUTTON_EVENTS
                : CHANNEL_ID_BUTTON_EVENTS;
        updateStatus(ThingStatus.ONLINE);
        triggerChannel(channelID, event);
    }
}
