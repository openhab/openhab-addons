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
package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_LIGHTLEVEL;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * This class extends the BaseThingHandler to support HomeWorks Dimmer modules.
 *
 * @author Andrew Shilliday - Initial contribution
 *
 */
public class HwDimmerHandler extends BaseThingHandler {
    private String address;
    private Integer fadeTime = 1;
    private Integer defaultLevel = 100;

    public HwDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        HwDimmerConfig config = getThing().getConfiguration().as(HwDimmerConfig.class);

        address = config.getAddress();
        if (address == null || address.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Address not set");
            return;
        }

        fadeTime = config.getFadeTime();
        defaultLevel = config.getDefaultLevel();

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        queryLevel();
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number number) {
                int level = number.intValue();
                outputLevel(level);
            } else if (command.equals(OnOffType.ON)) {
                outputLevel(defaultLevel);
            } else if (command.equals(OnOffType.OFF)) {
                outputLevel(0);
            }
        }
    }

    private HwSerialBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        } else if (!(bridge.getHandler() instanceof HwSerialBridgeHandler)) {
            return null;
        } else {
            return (HwSerialBridgeHandler) bridge.getHandler();
        }
    }

    private void queryLevel() {
        HwSerialBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
            return;
        }

        String cmd = String.format("RDL, %s", address);
        bridgeHandler.sendCommand(cmd);
    }

    private void outputLevel(Number level) {
        HwSerialBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
            return;
        }

        String cmd = String.format("FADEDIM, %s, %s, 0, %s", level, fadeTime, address);
        bridgeHandler.sendCommand(cmd);
    }

    public void handleLevelChange(Integer level) {
        updateState(CHANNEL_LIGHTLEVEL, new PercentType(level));
    }
}
