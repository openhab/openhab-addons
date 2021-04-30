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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.updateopenhab.scripts.BaseUpdater;
import org.openhab.binding.updateopenhab.scripts.DebianUpdater;
import org.openhab.binding.updateopenhab.scripts.MacUpdater;
import org.openhab.binding.updateopenhab.scripts.WindowsUpdater;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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

    /**
     * Constructor
     */
    public UpdateHandler(Thing thing) {
        super(thing);
        targetVersion = TargetVersion.STABLE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BaseUpdater updater = this.updater;
        switch (channelUID.getId()) {

            case BindingConstants.CHANNEL_UPDATE_COMMAND:
                updateState(channelUID, OnOffType.OFF);
                if (OnOffType.ON.equals(command) && (updater != null)) {
                    scheduler.submit(updater);
                }
                break;

            case BindingConstants.CHANNEL_ACTUAL_OH_VERSION:
                if (command instanceof RefreshType) {
                    updateState(channelUID, StringType.valueOf(OpenHAB.getVersion()));
                }
                break;

            case BindingConstants.CHANNEL_LATEST_OH_VERSION:
                if (command instanceof RefreshType) {
                    if (updater == null) {
                        updateState(channelUID, UnDefType.UNDEF);
                    } else {
                        updateState(channelUID, StringType.valueOf(updater.getLatestVersion()));
                    }
                }
                break;

            case BindingConstants.CHANNEL_UPDATE_AVAILABLE:
                if (command instanceof RefreshType) {
                    if (TargetVersion.SNAPSHOT.equals(targetVersion) || (updater == null)) {
                        updateState(channelUID, UnDefType.UNDEF);
                    } else {
                        updateState(channelUID,
                                OnOffType.from(OpenHAB.getVersion().equals(updater.getLatestVersion())));
                    }
                }
        }
    }

    @Override
    public void initialize() {
        Configuration config = getConfigAs(Configuration.class);
        try {
            targetVersion = TargetVersion.valueOf(config.targetVersion);
        } catch (IllegalArgumentException | NullPointerException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        OperatingSystem os = OperatingSystem.getOperatingSystemVersion();
        updateProperty(BindingConstants.PROPERTY_OPERATING_SYSTEM, os.toString());
        switch (os) {
            case MAC:
                updater = new MacUpdater(targetVersion);
                break;
            case UNIX:
                updater = new DebianUpdater(targetVersion);
                break;
            case WINDOWS:
                updater = new WindowsUpdater(targetVersion);
                break;
            default:
                updater = null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
