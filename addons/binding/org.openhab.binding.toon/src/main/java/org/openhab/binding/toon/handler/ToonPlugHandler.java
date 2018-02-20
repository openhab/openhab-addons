/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.handler;

import static org.openhab.binding.toon.ToonBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.toon.internal.api.DeviceConfig;
import org.openhab.binding.toon.internal.api.ToonConnectionException;
import org.openhab.binding.toon.internal.api.ToonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ToonPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonPlugHandler extends AbstractToonHandler {
    private Logger logger = LoggerFactory.getLogger(ToonPlugHandler.class);

    public ToonPlugHandler(Thing thing) {
        super(thing);
    }

    private void updateDevice(DeviceConfig info) {
        if (info == null) {
            return;
        }

        if (info.getIsConnected() != null) {
            if (info.getIsConnected() != 0) {
                if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                if (!ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }

        if (info.getCurrentState() != null) {
            updateChannel(CHANNEL_SWITCH_BINARY, info.getCurrentState() == 0 ? OnOffType.OFF : OnOffType.ON);
        }
        if (info.getCurrentUsage() != null) {
            BigDecimal d = new BigDecimal(info.getCurrentUsage()).setScale(1, BigDecimal.ROUND_HALF_UP);
            updateChannel(CHANNEL_POWER_CONSUMPTION, new DecimalType(d));
        }

    }

    @Override
    protected void updateChannels(ToonState state) {
        logger.debug("Updating channels");

        // bridge has collect new data samples
        String UUID = getThing().getProperties().get(PROPERTY_DEV_UUID);

        // process results
        if (state.getDeviceStatusInfo() != null && state.getDeviceStatusInfo().getDevice() != null) {
            for (DeviceConfig config : state.getDeviceStatusInfo().getDevice()) {
                if (UUID.equals(config.getDevUUID())) {
                    updateDevice(config);
                    break;
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} for {}", command, channelUID.getAsString());
        try {
            if (command == RefreshType.REFRESH) {
                getToonBridgeHandler().requestRefresh();
                return;
            }

            if (CHANNEL_SWITCH_BINARY.equals(channelUID.getId())) {
                int value = 0;
                if (command instanceof OnOffType) {
                    value = command == OnOffType.ON ? 1 : 0;
                }
                if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    value = d.intValue();
                }

                getToonBridgeHandler().getApiClient().setPlugState(value,
                        getThing().getProperties().get(PROPERTY_DEV_UUID));
            } else {
                logger.warn("unknown channel:{} / command:{}", channelUID.getAsString(), command);
            }
        } catch (ToonConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

}
