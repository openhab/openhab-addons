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
package org.openhab.binding.updateopenhab.binding;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.updateopenhab.updaters.BaseUpdater;
import org.openhab.binding.updateopenhab.updaters.OperatingSystem;
import org.openhab.binding.updateopenhab.updaters.TargetVersionType;
import org.openhab.binding.updateopenhab.updaters.UpdaterFactory;
import org.openhab.binding.updateopenhab.updaters.UpdaterStates;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpdaterHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class UpdaterHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(UpdaterHandler.class);

    private TargetVersionType targetVersion;
    private @Nullable BaseUpdater updater;
    private @Nullable ScheduledFuture<?> refreshTask;

    private boolean initialized;

    /**
     * Constructor
     */
    public UpdaterHandler(Thing thing) {
        super(thing);
        targetVersion = TargetVersionType.STABLE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChanels();
            return;
        }
        if (BindingConstants.CHANNEL_UPDATE_COMMAND.equals(channelUID.getId()) && (OnOffType.ON.equals(command))) {
            scheduler.schedule(() -> {
                updateState(channelUID, OnOffType.OFF);
            }, 1, TimeUnit.SECONDS);
            if (updater != null) {
                scheduler.submit(updater);
            }
        }
    }

    @Override
    public void initialize() {
        BaseUpdater updater = this.updater = UpdaterFactory.newUpdater();
        if (updater == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        Configuration config = getConfigAs(Configuration.class);
        try {
            updater.setTargetVersion(config.targetVersion);
        } catch (IllegalArgumentException e) {
            logger.debug("Bad targetVersion {}.", targetVersion);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        try {
            updater.setPassword(config.password);
        } catch (IllegalArgumentException e) {
            logger.debug("Bad password {}.", config.password);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        try {
            updater.setSleepTime(config.sleepTime.toString());
        } catch (IllegalArgumentException e) {
            logger.debug("Bad sleepTime {}.", config.sleepTime);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        updateProperty(BindingConstants.PROPERTY_OPERATING_SYSTEM,
                OperatingSystem.getOperatingSystemVersion().toString());

        updateStatus(ThingStatus.ONLINE);
        initialized = true;

        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask == null || refreshTask.isCancelled()) {
            this.refreshTask = scheduler.scheduleWithFixedDelay(() -> {
                updateChanels();
            }, 5, 3600, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        initialized = false;
        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
    }

    private synchronized void updateChanels() {
        if (!initialized) {
            return;
        }

        updateState(BindingConstants.CHANNEL_UPDATE_COMMAND, OnOffType.OFF);
        updateState(BindingConstants.CHANNEL_ACTUAL_OH_VERSION, UpdaterStates.getActualVersion());

        BaseUpdater updater = this.updater;
        if (updater != null) {
            updateState(BindingConstants.CHANNEL_LATEST_OH_VERSION, UpdaterStates.getRemoteVersion(updater));
            updateState(BindingConstants.CHANNEL_UPDATE_AVAILABLE, UpdaterStates.getRemoteVersionHigher(updater));
        } else {
            updateState(BindingConstants.CHANNEL_LATEST_OH_VERSION, UnDefType.UNDEF);
            updateState(BindingConstants.CHANNEL_UPDATE_AVAILABLE, UnDefType.UNDEF);
        }
    }
}
