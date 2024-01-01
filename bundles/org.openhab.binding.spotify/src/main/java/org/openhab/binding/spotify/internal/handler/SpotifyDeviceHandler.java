/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal.handler;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.spotify.internal.api.SpotifyApi;
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyDeviceHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Code cleanup, moved channel state to this class, generic stability.
 */
@NonNullByDefault
public class SpotifyDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SpotifyDeviceHandler.class);
    private @NonNullByDefault({}) SpotifyHandleCommands commandHandler;
    private @NonNullByDefault({}) SpotifyApi spotifyApi;
    private String deviceName = "";
    private String deviceId = "";

    private boolean active;

    /**
     * Constructor.
     *
     * @param thing Thing representing this device.
     */
    public SpotifyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (commandHandler != null && !deviceId.isEmpty()) {
                commandHandler.handleCommand(channelUID, command, active, deviceId);
            }
        } catch (SpotifyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        final SpotifyBridgeHandler bridgeHandler = (SpotifyBridgeHandler) getBridge().getHandler();
        spotifyApi = bridgeHandler.getSpotifyApi();

        if (spotifyApi == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                    "Missing configuration from the Spotify Bridge (UID:%s). Fix configuration or report if this problem remains.",
                    getBridge().getBridgeUID()));
            return;
        }
        deviceName = (String) getConfig().get(PROPERTY_SPOTIFY_DEVICE_NAME);
        if (deviceName == null || deviceName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The deviceName property is not set or empty. If you have an older thing please recreate this thing.");
            deviceName = "";
        } else {
            commandHandler = new SpotifyHandleCommands(spotifyApi);
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Spotify Bridge Offline");
            logger.debug("SpotifyDevice {}: SpotifyBridge is not online: {}", getThing().getThingTypeUID(),
                    bridgeStatusInfo.getStatus());
        }
    }

    /**
     * Updates the status if the given device matches with this handler.
     *
     * @param device device with status information
     * @param playing true if the current active device is playing
     * @return returns true if given device matches with this handler
     */
    public boolean updateDeviceStatus(Device device, boolean playing) {
        if (deviceName.equals(device.getName())) {
            deviceId = device.getId() == null ? "" : device.getId();
            logger.debug("Updating status of Thing: {} Device [ {} {}, {} ]", thing.getUID(), deviceId,
                    device.getName(), device.getType());
            final boolean online = setOnlineStatus(device.isRestricted());
            updateChannelState(CHANNEL_DEVICEID, new StringType(deviceId));
            updateChannelState(CHANNEL_DEVICENAME, new StringType(device.getName()));
            updateChannelState(CHANNEL_DEVICETYPE, new StringType(device.getType()));
            updateChannelState(CHANNEL_DEVICEVOLUME,
                    device.getVolumePercent() == null ? UnDefType.UNDEF : new PercentType(device.getVolumePercent()));
            active = device.isActive();
            updateChannelState(CHANNEL_DEVICEACTIVE, OnOffType.from(active));
            updateChannelState(CHANNEL_DEVICEPLAYER,
                    online && active && playing ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the device as showing status is gone and reset all device status to default.
     */
    public void setStatusGone() {
        if (getThing().getStatus() != ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.GONE) {
            logger.debug("Device is gone: {}", thing.getUID());
            getThing().setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                    "Device not available on Spotify"));
            updateChannelState(CHANNEL_DEVICERESTRICTED, OnOffType.ON);
            updateChannelState(CHANNEL_DEVICEACTIVE, OnOffType.OFF);
            updateChannelState(CHANNEL_DEVICEPLAYER, PlayPauseType.PAUSE);
        }
    }

    /**
     * Sets the device online status. If the device is restricted it will be set offline.
     *
     * @param restricted true if device is restricted (no access)
     * @return true if device is online
     */
    private boolean setOnlineStatus(boolean restricted) {
        updateChannelState(CHANNEL_DEVICERESTRICTED, OnOffType.from(restricted));
        final boolean statusUnknown = thing.getStatus() == ThingStatus.UNKNOWN;

        if (restricted) {
            // Only change status if device is currently online
            if (thing.getStatus() == ThingStatus.ONLINE || statusUnknown) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "Restricted. No Web API commands will be accepted by this device.");
            }
            return false;
        } else if (statusUnknown || thing.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        return true;
    }

    /**
     * Convenience method to update the channel state but only if the channel is linked.
     *
     * @param channelId id of the channel to update
     * @param state State to set on the channel
     */
    private void updateChannelState(String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }
}
