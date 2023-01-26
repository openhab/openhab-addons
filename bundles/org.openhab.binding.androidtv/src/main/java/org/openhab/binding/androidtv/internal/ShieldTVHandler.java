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

    private String hostName = "";
    private String currentApp = "";
    private String deviceId = "";
    private String arch = "";

    public ShieldTVHandler(Thing thing) {
        super(thing);
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        thing.setProperty("Device Name", hostName);
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setDeviceID(String deviceId) {
        this.deviceId = deviceId;
        thing.setProperty("Device ID", deviceId);
    }

    public String getDeviceID() {
        return this.deviceId;
    }

    public void setArch(String arch) {
        this.arch = arch;
        thing.setProperty("Architectures", arch);
    }

    public String getArch() {
        return this.arch;
    }

    public void updateThingStatus(ThingStatus thingStatus) {
        updateStatus(thingStatus);
    }

    public void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        updateStatus(thingStatus, thingStatusDetail);
    }

    public void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String status) {
        updateStatus(thingStatus, thingStatusDetail, status);
    }

    public void updateChannelState(String channel, State state) {
        updateState(channel, state);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public void initialize() {
        ShieldTVConfiguration config = getConfigAs(ShieldTVConfiguration.class);

        if (config.ipAddress == null || config.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "shieldtv address not specified");
            return;
        }

        shieldtvConnectionManager = new ShieldTVConnectionManager(this, config);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Connecting");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command received at handler: {} {}", channelUID.getId().toString(), command.toString());
        shieldtvConnectionManager.handleCommand(channelUID, command);
    }

    @Override
    public void dispose() {
        shieldtvConnectionManager.dispose();
    }
}
