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
package org.openhab.binding.androidtv.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.androidtv.internal.protocol.shieldtv.ShieldTVConfiguration;
import org.openhab.binding.androidtv.internal.protocol.shieldtv.ShieldTVConnectionManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShieldTVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Significant portions reused from Lutron binding with permission from Bob A.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ShieldTVHandler.class);

    private @Nullable ShieldTVConnectionManager shieldtvConnectionManager;
    private @Nullable ScheduledFuture<?> monitorThingStatusJob;
    private final Object monitorThingStatusJobLock = new Object();
    private static final int THING_STATUS_FREQUENCY = 250;

    public ShieldTVHandler(Thing thing) {
        super(thing);
    }

    public void setThingProperty(String property, String value) {
        thing.setProperty(property, value);
    }

    public void updateChannelState(String channel, State state) {
        updateState(channel, state);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    private void monitorThingStatus() {
        synchronized (monitorThingStatusJobLock) {
            checkThingStatus();
            monitorThingStatusJob = scheduler.schedule(this::monitorThingStatus, THING_STATUS_FREQUENCY,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void checkThingStatus() {
        if (shieldtvConnectionManager.getLoggedIn()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            String statusMessage = "ShieldTV: " + shieldtvConnectionManager.getStatusMessage();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, statusMessage);
        }
    }

    @Override
    public void initialize() {
        ShieldTVConfiguration config = getConfigAs(ShieldTVConfiguration.class);

        if (config.ipAddress == null || config.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "shieldtv address not specified");
            return;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Protocols Starting");

        shieldtvConnectionManager = new ShieldTVConnectionManager(this, config);

        monitorThingStatusJob = scheduler.schedule(this::monitorThingStatus, THING_STATUS_FREQUENCY,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command received at handler: {} {}", channelUID.getId().toString(), command.toString());
        shieldtvConnectionManager.handleCommand(channelUID, command);
    }

    @Override
    public void dispose() {
        synchronized (monitorThingStatusJobLock) {

            ScheduledFuture<?> monitorThingStatusJob = this.monitorThingStatusJob;
            if (monitorThingStatusJob != null) {
                monitorThingStatusJob.cancel(true);
            }
        }
        shieldtvConnectionManager.dispose();
    }
}
