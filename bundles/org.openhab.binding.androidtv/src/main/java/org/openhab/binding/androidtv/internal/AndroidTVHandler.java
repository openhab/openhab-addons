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
    private final AndroidTVTranslationProvider translationProvider;
    private final ThingTypeUID thingTypeUID;
    private final String thingID;

    private String currentThingStatus = "";
    private boolean currentThingFailed = false;

    public AndroidTVHandler(Thing thing, AndroidTVDynamicCommandDescriptionProvider commandDescriptionProvider,
            AndroidTVTranslationProvider translationProvider, ThingTypeUID thingTypeUID) {
        super(thing);
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.translationProvider = translationProvider;
        this.thingTypeUID = thingTypeUID;
        this.thingID = this.getThing().getUID().getId();
    }

    public void setThingProperty(String property, String value) {
        thing.setProperty(property, value);
    }

    public AndroidTVTranslationProvider getTranslationProvider() {
        return translationProvider;
    }

    public String getThingID() {
        return this.thingID;
    }

    public void updateChannelState(String channel, State state) {
        updateState(channel, state);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void updateCDP(String channelName, Map<String, String> cdpMap) {
        logger.trace("{} - Updating CDP for {}", this.thingID, channelName);
        List<CommandOption> commandOptions = new ArrayList<CommandOption>();
        cdpMap.forEach((key, value) -> commandOptions.add(new CommandOption(key, value)));
        logger.trace("{} - CDP List: {}", this.thingID, commandOptions);
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
        String currentThingStatus = this.currentThingStatus;
        boolean currentThingFailed = this.currentThingFailed;

        String statusMessage = "";
        boolean failed = false;

        GoogleTVConnectionManager googletvConnectionManager = this.googletvConnectionManager;
        ShieldTVConnectionManager shieldtvConnectionManager = this.shieldtvConnectionManager;

        if (googletvConnectionManager != null) {
            if (!googletvConnectionManager.getLoggedIn()) {
                failed = true;
            }
            statusMessage = "GoogleTV: " + googletvConnectionManager.getStatusMessage();
        }

        if (THING_TYPE_SHIELDTV.equals(thingTypeUID)) {
            if (shieldtvConnectionManager != null) {
                if (!shieldtvConnectionManager.getLoggedIn()) {
                    failed = true;
                }
                statusMessage = statusMessage + " | ShieldTV: " + shieldtvConnectionManager.getStatusMessage();
            }
        }

        if (!currentThingStatus.equals(statusMessage) || (currentThingFailed != failed)) {
            if (failed) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, statusMessage);
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }

        this.currentThingStatus = statusMessage;
        this.currentThingFailed = failed;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.protocols-starting");

        GoogleTVConfiguration googletvConfig = getConfigAs(GoogleTVConfiguration.class);
        String ipAddress = googletvConfig.ipAddress;
        boolean gtvEnabled = googletvConfig.gtvEnabled;

        if (ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.googletv-address-not-specified");
            return;
        }

        if (THING_TYPE_GOOGLETV.equals(thingTypeUID) || gtvEnabled) {
            googletvConnectionManager = new GoogleTVConnectionManager(this, googletvConfig);
        }

        if (THING_TYPE_SHIELDTV.equals(thingTypeUID)) {
            ShieldTVConfiguration shieldtvConfig = getConfigAs(ShieldTVConfiguration.class);
            ipAddress = shieldtvConfig.ipAddress;

            if (ipAddress.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.shieldtv-address-not-specified");
                return;
            }

            shieldtvConnectionManager = new ShieldTVConnectionManager(this, shieldtvConfig);
        }

        monitorThingStatusJob = scheduler.schedule(this::monitorThingStatus, THING_STATUS_FREQUENCY,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("{} - Command received at handler: {} {}", this.thingID, channelUID.getId(), command);

        if (command.toString().equals("REFRESH")) {
            // REFRESH causes issues on some channels. Block for now until implemented.
            return;
        }

        GoogleTVConnectionManager googletvConnectionManager = this.googletvConnectionManager;
        ShieldTVConnectionManager shieldtvConnectionManager = this.shieldtvConnectionManager;

        if (CHANNEL_DEBUG.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().equals("GOOGLETV_HALT") && (googletvConnectionManager != null)) {
                    googletvConnectionManager.dispose();
                    googletvConnectionManager = null;
                } else if (command.toString().equals("GOOGLETV_START")) {
                    GoogleTVConfiguration googletvConfig = getConfigAs(GoogleTVConfiguration.class);
                    googletvConnectionManager = new GoogleTVConnectionManager(this, googletvConfig);
                } else if (command.toString().equals("GOOGLETV_SHIM") && (googletvConnectionManager == null)) {
                    GoogleTVConfiguration googletvConfig = getConfigAs(GoogleTVConfiguration.class);
                    googletvConfig.shim = true;
                    googletvConnectionManager = new GoogleTVConnectionManager(this, googletvConfig);
                } else if (command.toString().equals("SHIELDTV_HALT") && (shieldtvConnectionManager != null)) {
                    shieldtvConnectionManager.dispose();
                    shieldtvConnectionManager = null;
                } else if (command.toString().equals("SHIELDTV_START")) {
                    ShieldTVConfiguration shieldtvConfig = getConfigAs(ShieldTVConfiguration.class);
                    shieldtvConnectionManager = new ShieldTVConnectionManager(this, shieldtvConfig);
                } else if (command.toString().equals("SHIELDTV_SHIM") && (shieldtvConnectionManager == null)) {
                    ShieldTVConfiguration shieldtvConfig = getConfigAs(ShieldTVConfiguration.class);
                    shieldtvConfig.shim = true;
                    shieldtvConnectionManager = new ShieldTVConnectionManager(this, shieldtvConfig);
                } else if (command.toString().startsWith("GOOGLETV") && (googletvConnectionManager != null)) {
                    googletvConnectionManager.handleCommand(channelUID, command);
                } else if (command.toString().startsWith("SHIELDTV") && (shieldtvConnectionManager != null)) {
                    shieldtvConnectionManager.handleCommand(channelUID, command);
                }
            }
            return;
        }

        if (THING_TYPE_SHIELDTV.equals(thingTypeUID) && (shieldtvConnectionManager != null)) {
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
            } else if (googletvConnectionManager == null) {
                shieldtvConnectionManager.handleCommand(channelUID, command);
                return;
            }
        }

        if (googletvConnectionManager != null) {
            googletvConnectionManager.handleCommand(channelUID, command);
            return;
        }

        logger.warn("{} - Commands All Failed.  Please report this as a bug. {} {}", thingID, channelUID.getId(),
                command);
    }

    @Override
    public void dispose() {
        synchronized (monitorThingStatusJobLock) {
            ScheduledFuture<?> monitorThingStatusJob = this.monitorThingStatusJob;
            if (monitorThingStatusJob != null) {
                monitorThingStatusJob.cancel(true);
            }
        }

        GoogleTVConnectionManager googletvConnectionManager = this.googletvConnectionManager;
        ShieldTVConnectionManager shieldtvConnectionManager = this.shieldtvConnectionManager;

        if (shieldtvConnectionManager != null) {
            shieldtvConnectionManager.dispose();
        }

        if (googletvConnectionManager != null) {
            googletvConnectionManager.dispose();
        }
    }
}
