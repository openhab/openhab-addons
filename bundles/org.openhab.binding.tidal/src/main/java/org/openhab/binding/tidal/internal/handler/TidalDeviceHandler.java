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
package org.openhab.binding.tidal.internal.handler;

import static org.openhab.binding.tidal.internal.TidalBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tidal.internal.api.TidalApi;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TidalDeviceHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TidalDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TidalDeviceHandler.class);
    private @NonNullByDefault({}) TidalHandleCommands commandHandler;
    private @NonNullByDefault({}) TidalApi tidalApi;
    private String deviceName = "";
    private String deviceId = "";

    private boolean active;

    /**
     * Constructor.
     *
     * @param thing Thing representing this device.
     */
    public TidalDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (commandHandler != null && !deviceId.isEmpty()) {
                commandHandler.handleCommand(channelUID, command, active, deviceId);
            }
        } catch (TidalException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        final TidalBridgeHandler bridgeHandler = (TidalBridgeHandler) getBridge().getHandler();
        tidalApi = bridgeHandler.getTidalApi();

        if (tidalApi == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                    "Missing configuration from the Tidal Bridge (UID:%s). Fix configuration or report if this problem remains.",
                    getBridge().getBridgeUID()));
            return;
        }
        deviceName = (String) getConfig().get(PROPERTY_TIDAL_DEVICE_NAME);
        if (deviceName == null || deviceName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The deviceName property is not set or empty. If you have an older thing please recreate this thing.");
            deviceName = "";
        } else {
            commandHandler = new TidalHandleCommands(tidalApi);
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tidal Bridge Offline");
            logger.debug("TidalDevice {}: TidalBridge is not online: {}", getThing().getThingTypeUID(),
                    bridgeStatusInfo.getStatus());
        }
    }

    /**
     * Updates the device as showing status is gone and reset all device status to default.
     */
    public void setStatusGone() {
        if (getThing().getStatus() != ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.GONE) {
            logger.debug("Device is gone: {}", thing.getUID());
            getThing().setStatusInfo(
                    new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device not available on Tidal"));
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
