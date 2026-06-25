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
package org.openhab.binding.autoblind.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.autoblind.internal.AutoBlindBindingConstants;
import org.openhab.binding.autoblind.internal.api.AutoBlindApiClient;
import org.openhab.binding.autoblind.internal.api.dto.PeripheralStatus;
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
 * Handler for an individual AutoBlind motorized shade.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class AutoBlindShadeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AutoBlindShadeHandler.class);
    private int peripheralUid;
    private volatile int lastKnownApiPosition = -1;
    private volatile long lastCommandTime = 0;
    private volatile int lastCommandedOhPosition = -1;

    // Motion state for notification-based tracking
    private volatile boolean inMotion;
    private volatile int pendingTarget = -1;
    private volatile int startPosition = -1;
    private volatile boolean sawMoveSinceCommand;

    public AutoBlindShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Object uidObj = getConfig().get(AutoBlindBindingConstants.CONFIG_PERIPHERAL_UID);
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

        AutoBlindHubHandler hubHandler = (AutoBlindHubHandler) bridge.getHandler();
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
            AutoBlindHubHandler hubHandler = (AutoBlindHubHandler) bridge.getHandler();
            if (hubHandler != null) {
                hubHandler.unregisterShadeHandler(peripheralUid);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            AutoBlindHubHandler hubHandler = getHubHandler();
            if (hubHandler != null) {
                hubHandler.scheduleRefreshPoll(1);
            }
            return;
        }

        if (!AutoBlindBindingConstants.CHANNEL_POSITION.equals(channelUID.getId())) {
            return;
        }

        @Nullable
        AutoBlindApiClient client = getApiClient();
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
                updateState(AutoBlindBindingConstants.CHANNEL_POSITION, new PercentType(ohPosition));
                lastCommandTime = System.currentTimeMillis();
                lastCommandedOhPosition = ohPosition;
                registerCommand(ohPosition);
            } else if (command instanceof PercentType percent) {
                int apiPosition = 100 - percent.intValue();
                logger.debug("Shade {} percent {} -> API position {}", peripheralUid, percent, apiPosition);
                client.controlShade(peripheralUid, apiPosition);
                updateState(AutoBlindBindingConstants.CHANNEL_POSITION, percent);
                lastCommandTime = System.currentTimeMillis();
                lastCommandedOhPosition = percent.intValue();
                registerCommand(percent.intValue());
            } else {
                logger.debug("Shade {} ignoring unhandled command type: {}", peripheralUid,
                        command.getClass().getSimpleName());
                return;
            }
            AutoBlindHubHandler hubHandler = getHubHandler();
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
        if (commanded >= 0 && elapsed < AutoBlindBindingConstants.COMMAND_SUPPRESSION_MS) {
            if (Math.abs(ohPosition - commanded) > AutoBlindBindingConstants.POSITION_TOLERANCE) {
                logger.debug("Shade {} suppressing stale hub position {} (commanded {} {}ms ago)", peripheralUid,
                        ohPosition, commanded, elapsed);
                suppressed = true;
            } else {
                logger.debug("Shade {} hub confirmed position {} (commanded {})", peripheralUid, ohPosition, commanded);
                lastCommandedOhPosition = -1;
            }
        }

        if (inMotion && checkSettlement(ohPosition)) {
            suppressed = false;
        }

        if (!suppressed) {
            updateState(AutoBlindBindingConstants.CHANNEL_POSITION, new PercentType(ohPosition));
        }

        int battery = Math.max(0, Math.min(100, status.batteryVoltage));
        updateState(AutoBlindBindingConstants.CHANNEL_BATTERY_LEVEL, new QuantityType<>(battery, Units.PERCENT));

        boolean lowBattery = battery <= AutoBlindBindingConstants.LOW_BATTERY_THRESHOLD;
        updateState(AutoBlindBindingConstants.CHANNEL_LOW_BATTERY, OnOffType.from(lowBattery));

        logger.debug("Shade {} updated: apiPos={} ohPos={} battery={}%{}", peripheralUid, status.bottomRailPosition,
                ohPosition, battery, suppressed ? " (position suppressed)" : "");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable AutoBlindHubHandler getHubHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (AutoBlindHubHandler) bridge.getHandler();
        }
        return null;
    }

    private @Nullable AutoBlindApiClient getApiClient() {
        AutoBlindHubHandler hubHandler = getHubHandler();
        return hubHandler != null ? hubHandler.getApiClient() : null;
    }

    public void registerCommand(int targetOhPosition) {
        startPosition = lastKnownApiPosition >= 0 ? (100 - lastKnownApiPosition) : -1;
        pendingTarget = targetOhPosition;
        sawMoveSinceCommand = false;
        inMotion = true;
        logger.debug("Shade {} motion started: target={} startPos={}", peripheralUid, targetOhPosition, startPosition);
    }

    public boolean checkSettlement(int ohPosition) {
        if (!inMotion || pendingTarget < 0) {
            return false;
        }
        if (startPosition >= 0
                && Math.abs(ohPosition - startPosition) > AutoBlindBindingConstants.SETTLEMENT_TOLERANCE) {
            sawMoveSinceCommand = true;
        }
        if (Math.abs(ohPosition - pendingTarget) <= AutoBlindBindingConstants.SETTLEMENT_TOLERANCE
                && sawMoveSinceCommand) {
            logger.debug("Shade {} settled: hubPos={} target={}", peripheralUid, ohPosition, pendingTarget);
            inMotion = false;
            pendingTarget = -1;
            clearSuppression();
            return true;
        }
        return false;
    }

    public boolean isInMotion() {
        return inMotion;
    }

    public void clearMotion() {
        inMotion = false;
        pendingTarget = -1;
        sawMoveSinceCommand = false;
    }

    public void clearSuppression() {
        lastCommandedOhPosition = -1;
        lastCommandTime = 0;
    }

    public int getPeripheralUid() {
        return peripheralUid;
    }
}
