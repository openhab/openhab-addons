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
package org.openhab.binding.flicbutton.handler;

import static org.openhab.binding.flicbutton.FlicButtonBindingConstants.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.BatteryStatusListener;
import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.DisconnectReason;

/**
 * The {@link FlicButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonHandler extends ChildThingHandler<FlicDaemonBridgeHandler> {

    private Logger logger = LoggerFactory.getLogger(FlicButtonHandler.class);
    private ScheduledFuture<?> delayedDisconnectTask;
    private DisconnectReason latestDisconnectReason;
    private ButtonConnectionChannel eventConnection;
    private Bdaddr bdaddr;
    BatteryStatusListener batteryConnection;

    public FlicButtonHandler(Thing thing) {
        super(thing);
    }

    public Bdaddr getBdaddr() {
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
            initializeThing();
        }
    }

    public void initializeThing() {
        try {
            FlicButtonBatteryLevelListener batteryListener = new FlicButtonBatteryLevelListener(this);
            BatteryStatusListener batteryConnection = new BatteryStatusListener(getBdaddr(), batteryListener);
            bridgeHandler.getFlicClient().addBatteryStatusListener(batteryConnection);
            this.batteryConnection = batteryConnection;

            FlicButtonEventListener eventListener = new FlicButtonEventListener(this);
            synchronized (eventListener) {
                ButtonConnectionChannel eventConnection = new ButtonConnectionChannel(getBdaddr(), eventListener);
                bridgeHandler.getFlicClient().addConnectionChannel(eventConnection);
                this.eventConnection = eventConnection;
                eventListener.wait(5000);
                // Listener calls initializeStatus() before notifying so that ThingStatus is set at this point
            }
        } catch (IOException | InterruptedException e) {
            logger.info("Connection setup for Flic Button {} failed.", this.getThing(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection setup failed");
        }
    }

    @Override
    public void dispose() {
        cancelDelayedDisconnectTask();

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

    void connectionStatusChanged(ConnectionStatus connectionStatus, DisconnectReason disconnectReason) {
        latestDisconnectReason = disconnectReason;
        if (connectionStatus == ConnectionStatus.Disconnected) {
            // Status change to offline have to be scheduled to improve stability, see issue #2
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
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Button connected.");
    }

    protected void setOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Disconnect Reason: " + Objects.toString(latestDisconnectReason));
    }

    // Cleanup delayedDisconnect on status change to online
    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        if (status == ThingStatus.ONLINE) {
            cancelDelayedDisconnectTask();
        }
        super.updateStatus(status, statusDetail, description);
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                "Button was removed/detached from flicd (e.g. by simpleclient).");
    }

    void fireTriggerEvent(String event) {
        String channelID = event == CommonTriggerEvents.PRESSED || event == CommonTriggerEvents.RELEASED
                ? CHANNEL_ID_RAWBUTTON_EVENTS
                : CHANNEL_ID_BUTTON_EVENTS;
        updateStatus(ThingStatus.ONLINE);
        triggerChannel(channelID, event);
    }
}
