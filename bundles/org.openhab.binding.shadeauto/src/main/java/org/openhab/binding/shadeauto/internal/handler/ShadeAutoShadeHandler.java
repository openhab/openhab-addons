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
package org.openhab.binding.shadeauto.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shadeauto.internal.ShadeAutoBindingConstants;
import org.openhab.binding.shadeauto.internal.api.ShadeAutoApiClient;
import org.openhab.binding.shadeauto.internal.api.dto.PeripheralStatus;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an individual ShadeAuto motorized shade.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class ShadeAutoShadeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ShadeAutoShadeHandler.class);
    private int peripheralUid;
    private volatile int lastKnownApiPosition = -1;
    private volatile long lastCommandTime = 0;
    private volatile int lastCommandedOhPosition = -1;

    public ShadeAutoShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Object uidObj = getConfig().get(ShadeAutoBindingConstants.CONFIG_PERIPHERAL_UID);
        if (uidObj == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Peripheral UID not configured");
            return;
        }
        peripheralUid = ((Number) uidObj).intValue();

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        ShadeAutoHubHandler hubHandler = (ShadeAutoHubHandler) bridge.getHandler();
        if (hubHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        hubHandler.registerShadeHandler(peripheralUid, this);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ShadeAutoHubHandler hubHandler = (ShadeAutoHubHandler) bridge.getHandler();
            if (hubHandler != null) {
                hubHandler.unregisterShadeHandler(peripheralUid);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            ShadeAutoHubHandler hubHandler = getHubHandler();
            if (hubHandler != null) {
                hubHandler.scheduleRefreshPoll(1);
            }
            return;
        }

        if (!ShadeAutoBindingConstants.CHANNEL_POSITION.equals(channelUID.getId())) {
            return;
        }

        @Nullable
        ShadeAutoApiClient client = getApiClient();
        if (client == null) {
            logger.debug("Cannot send command — hub not connected");
            return;
        }

        logger.debug("Shade {} handleCommand: channel={} command={} (type={})", peripheralUid, channelUID.getId(),
                command, command.getClass().getSimpleName());

        try {
            if (command instanceof OnOffType onOff) {
                int ohPosition = onOff == OnOffType.ON ? 100 : 0;
                int apiPosition = 100 - ohPosition;
                logger.debug("Shade {} ON/OFF -> API position {}", peripheralUid, apiPosition);
                client.controlShade(peripheralUid, apiPosition);
                updateState(ShadeAutoBindingConstants.CHANNEL_POSITION, new PercentType(ohPosition));
                lastCommandTime = System.currentTimeMillis();
                lastCommandedOhPosition = ohPosition;
            } else if (command instanceof PercentType percent) {
                int apiPosition = 100 - percent.intValue();
                logger.debug("Shade {} percent {} -> API position {}", peripheralUid, percent, apiPosition);
                client.controlShade(peripheralUid, apiPosition);
                updateState(ShadeAutoBindingConstants.CHANNEL_POSITION, percent);
                lastCommandTime = System.currentTimeMillis();
                lastCommandedOhPosition = percent.intValue();
            } else {
                logger.debug("Shade {} ignoring unhandled command type: {}", peripheralUid,
                        command.getClass().getSimpleName());
                return;
            }
            ShadeAutoHubHandler hubHandler = getHubHandler();
            if (hubHandler != null) {
                hubHandler.startTrackingPoll();
            }
        } catch (Exception e) {
            logger.debug("Failed to send command to shade {}: {}", peripheralUid, e.getMessage());
        }
    }

    public void updateFromStatus(PeripheralStatus status) {
        lastKnownApiPosition = status.bottomRailPosition;

        int ohPosition = 100 - status.bottomRailPosition;

        int commanded = lastCommandedOhPosition;
        long elapsed = System.currentTimeMillis() - lastCommandTime;
        boolean suppressed = false;
        if (commanded >= 0 && elapsed < ShadeAutoBindingConstants.COMMAND_SUPPRESSION_MS) {
            if (Math.abs(ohPosition - commanded) > ShadeAutoBindingConstants.POSITION_TOLERANCE) {
                logger.debug("Shade {} suppressing stale hub position {} (commanded {} {}ms ago)", peripheralUid,
                        ohPosition, commanded, elapsed);
                suppressed = true;
            } else {
                logger.debug("Shade {} hub confirmed position {} (commanded {})", peripheralUid, ohPosition, commanded);
                lastCommandedOhPosition = -1;
            }
        }

        if (!suppressed) {
            updateState(ShadeAutoBindingConstants.CHANNEL_POSITION, new PercentType(ohPosition));
        }

        int battery = Math.max(0, Math.min(100, status.batteryVoltage));
        updateState(ShadeAutoBindingConstants.CHANNEL_BATTERY_LEVEL, new QuantityType<>(battery, Units.PERCENT));

        boolean lowBattery = battery <= ShadeAutoBindingConstants.LOW_BATTERY_THRESHOLD;
        updateState(ShadeAutoBindingConstants.CHANNEL_LOW_BATTERY, OnOffType.from(lowBattery));

        logger.debug("Shade {} updated: apiPos={} ohPos={} battery={}%{}", peripheralUid, status.bottomRailPosition,
                ohPosition, battery, suppressed ? " (position suppressed)" : "");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable ShadeAutoHubHandler getHubHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (ShadeAutoHubHandler) bridge.getHandler();
        }
        return null;
    }

    private @Nullable ShadeAutoApiClient getApiClient() {
        ShadeAutoHubHandler hubHandler = getHubHandler();
        return hubHandler != null ? hubHandler.getApiClient() : null;
    }

    public void clearSuppression() {
        lastCommandedOhPosition = -1;
        lastCommandTime = 0;
    }

    public int getPeripheralUid() {
        return peripheralUid;
    }
}
