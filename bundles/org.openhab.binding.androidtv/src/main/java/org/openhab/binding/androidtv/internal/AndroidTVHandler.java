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

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.androidtv.internal.protocol.googletv.GoogleTVConfiguration;
import org.openhab.binding.androidtv.internal.protocol.googletv.GoogleTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.shieldtv.ShieldTVConfiguration;
import org.openhab.binding.androidtv.internal.protocol.shieldtv.ShieldTVConnectionManager;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AndroidTVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Significant portions reused from Lutron binding with permission from Bob A.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class AndroidTVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AndroidTVHandler.class);

    private @Nullable ShieldTVConnectionManager shieldtvConnectionManager;
    private @Nullable GoogleTVConnectionManager googletvConnectionManager;

    private @Nullable ScheduledFuture<?> monitorThingStatusJob;
    private final Object monitorThingStatusJobLock = new Object();
    private static final int THING_STATUS_FREQUENCY = 250;

    private final AndroidTVDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final ThingTypeUID thingTypeUID;

    public AndroidTVHandler(Thing thing, AndroidTVDynamicCommandDescriptionProvider commandDescriptionProvider,
            ThingTypeUID thingTypeUID) {
        super(thing);
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.thingTypeUID = thingTypeUID;
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

    public void updateCDP(String channelName, Map<String, String> cdpMap) {
        logger.trace("Updating CDP for {}", channelName);
        List<CommandOption> commandOptions = new ArrayList<CommandOption>();
        cdpMap.forEach((key, value) -> commandOptions.add(new CommandOption(key, value)));
        logger.trace("CDP List: {}", commandOptions.toString());
        commandDescriptionProvider.setCommandOptions(new ChannelUID(getThing().getUID(), channelName), commandOptions);
    }

    private void monitorThingStatus() {
        synchronized (monitorThingStatusJobLock) {
            checkThingStatus();
            monitorThingStatusJob = scheduler.schedule(this::monitorThingStatus, THING_STATUS_FREQUENCY,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void checkThingStatus() {
        String statusMessage = "";
        boolean failed = false;

        if (googletvConnectionManager != null) {
            if (!googletvConnectionManager.getLoggedIn()) {
                statusMessage = "GoogleTV: " + googletvConnectionManager.getStatusMessage();
                failed = true;
            } else {
                statusMessage = "GoogleTV: ONLINE";
            }
        }

        if (THING_TYPE_SHIELDTV.equals(thingTypeUID)) {
            if (shieldtvConnectionManager != null) {
                if (!shieldtvConnectionManager.getLoggedIn()) {
                    statusMessage = statusMessage + " | ShieldTV: " + shieldtvConnectionManager.getStatusMessage();
                    failed = true;
                } else {
                    statusMessage = statusMessage + " | ShieldTV: ONLINE";
                }
            }
        }

        if (failed) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, statusMessage);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Protocols Starting");

        GoogleTVConfiguration googletvConfig = getConfigAs(GoogleTVConfiguration.class);

        if (googletvConfig.ipAddress == null || googletvConfig.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "googletv address not specified");
            return;
        }

        googletvConnectionManager = new GoogleTVConnectionManager(this, googletvConfig);

        if (THING_TYPE_SHIELDTV.equals(thingTypeUID)) {
            ShieldTVConfiguration shieldtvConfig = getConfigAs(ShieldTVConfiguration.class);

            if (shieldtvConfig.ipAddress == null || shieldtvConfig.ipAddress.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "shieldtv address not specified");
                return;
            }

            shieldtvConnectionManager = new ShieldTVConnectionManager(this, shieldtvConfig);
        }

        monitorThingStatusJob = scheduler.schedule(this::monitorThingStatus, THING_STATUS_FREQUENCY,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command received at handler: {} {}", channelUID.getId().toString(), command.toString());
        if (THING_TYPE_SHIELDTV.equals(thingTypeUID)) {
            if (CHANNEL_PINCODE.equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    if (!shieldtvConnectionManager.getLoggedIn()) {
                        shieldtvConnectionManager.handleCommand(channelUID, command);
                        return;
                    }
                }
            } else if (CHANNEL_APP.equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    shieldtvConnectionManager.handleCommand(channelUID, command);
                    return;
                }

            }
        }

        googletvConnectionManager.handleCommand(channelUID, command);
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
        googletvConnectionManager.dispose();
    }
}
