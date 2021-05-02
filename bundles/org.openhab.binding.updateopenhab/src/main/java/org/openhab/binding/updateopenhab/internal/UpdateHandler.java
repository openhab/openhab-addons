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
package org.openhab.binding.updateopenhab.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.updateopenhab.scripts.BaseUpdater;
import org.openhab.binding.updateopenhab.scripts.DebianUpdater;
import org.openhab.binding.updateopenhab.scripts.MacUpdater;
import org.openhab.binding.updateopenhab.scripts.WindowsUpdater;
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
 * The {@link UpdateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class UpdateHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

    private TargetVersion targetVersion;
    private @Nullable BaseUpdater updater;
    private @Nullable ScheduledFuture<?> refreshTask;

    /**
     * Constructor
     */
    public UpdateHandler(Thing thing) {
        super(thing);
        targetVersion = TargetVersion.STABLE;
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
        Configuration config = getConfigAs(Configuration.class);
        try {
            targetVersion = TargetVersion.valueOf(config.targetVersion);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        OperatingSystem os = OperatingSystem.getOperatingSystemVersion();
        updateProperty(BindingConstants.PROPERTY_OPERATING_SYSTEM, os.toString());
        switch (os) {
            case MAC:
                updater = new MacUpdater(targetVersion, config.password);
                break;
            case UNIX:
                updater = new DebianUpdater(targetVersion, config.password);
                break;
            case WINDOWS:
                updater = new WindowsUpdater(targetVersion, config.password);
                break;
            default:
                updater = null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
        }

        updateStatus(ThingStatus.ONLINE);

        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask == null || refreshTask.isCancelled()) {
            this.refreshTask = scheduler.scheduleWithFixedDelay(() -> {
                updateChanels();
            }, 0, 60, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
    }

    private void updateChanels() {
        updateState(BindingConstants.CHANNEL_UPDATE_COMMAND, OnOffType.OFF);
        updateState(BindingConstants.CHANNEL_ACTUAL_OH_VERSION, BaseUpdater.getRunningVersionState());

        BaseUpdater updater = this.updater;
        if (updater != null) {
            updateState(BindingConstants.CHANNEL_LATEST_OH_VERSION, updater.getLatestVersionState());
            updateState(BindingConstants.CHANNEL_UPDATE_AVAILABLE, updater.getUpdateAvailableState());
        } else {
            updateState(BindingConstants.CHANNEL_LATEST_OH_VERSION, UnDefType.UNDEF);
            updateState(BindingConstants.CHANNEL_UPDATE_AVAILABLE, UnDefType.UNDEF);
        }
    }
}
