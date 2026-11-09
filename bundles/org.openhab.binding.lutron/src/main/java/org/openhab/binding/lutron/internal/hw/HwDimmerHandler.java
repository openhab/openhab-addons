/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.openhab.binding.lutron.internal.hw.HwConstants.CHANNEL_TAP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends the BaseThingHandler to support HomeWorks Dimmer modules.
 *
 * @author Andrew Shilliday - Initial contribution
 *
 */
@NonNullByDefault
public class HwDimmerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HwDimmerHandler.class);

    private @Nullable String address;
    private Integer fadeTime = 1;
    private Integer defaultLevel = 100;

    public HwDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        HwDimmerConfig config = getConfigAs(HwDimmerConfig.class);

        String configuredAddress = config.getAddress();
        address = configuredAddress == null ? null : HwAddress.normalize(configuredAddress);
        if (configuredAddress == null || configuredAddress.isEmpty()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Address not set");
            return;
        }

        fadeTime = config.getFadeTime();
        defaultLevel = config.getDefaultLevel();

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            // bridgeStatusChanged() brings the Thing online and queries the level when the bridge comes online
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        queryLevel();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && address != null) {
            // The query sent during initialization is lost if the bridge was not connected yet, and the level can
            // have changed while the connection was down.
            queryLevel();
        }
    }

    public @Nullable String getAddress() {
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

    private @Nullable HwBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HwBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    private void queryLevel() {
        if (getBridge() == null) {
            logger.debug("Bridge unavailable while querying Thing: {}", thing.getUID());
            return;
        }
        HwBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, "No bridge associated");
            return;
        }

        String cmd = String.format("RDL, %s", address);
        bridgeHandler.sendCommand(cmd);
    }

    private void outputLevel(Number level) {
        if (getBridge() == null) {
            logger.debug("Bridge unavailable while sending command for Thing: {}", thing.getUID());
            return;
        }
        HwBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, "No bridge associated");
            return;
        }

        String cmd = String.format("FADEDIM, %s, %s, 0, %s", level, fadeTime, address);
        bridgeHandler.sendCommand(cmd);
    }

    /**
     * Passes a tap on the dimmer's switch on to the {@code tap} channel.
     *
     * @param action the matching keypad message name: KBP, KBR, KBH or KBDT
     */
    void handleTap(String action) {
        String event = switch (action) {
            case "KBP" -> CommonTriggerEvents.PRESSED;
            case "KBR" -> CommonTriggerEvents.RELEASED;
            case "KBH" -> CommonTriggerEvents.LONG_PRESSED;
            case "KBDT" -> CommonTriggerEvents.DOUBLE_PRESSED;
            default -> null;
        };
        if (event != null && thing.getChannel(CHANNEL_TAP) != null) {
            triggerChannel(CHANNEL_TAP, event);
        }
    }

    public void handleLevelChange(Integer level) {
        updateState(CHANNEL_LIGHTLEVEL, new PercentType(level));
    }
}
