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
package org.openhab.binding.thekeys.internal.smartlock;

import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_BATTERY_LEVEL;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_LAST_SYNC;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_LOCK;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_LOW_BATTERY;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_POSITION;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_STATUS;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CHANNEL_SYNC_IN_PROGRESS;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.PROPERTY_VERSION;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.thekeys.internal.api.GatewayService;
import org.openhab.binding.thekeys.internal.api.TheKeysError;
import org.openhab.binding.thekeys.internal.api.model.LockerDTO;
import org.openhab.binding.thekeys.internal.api.model.LockerStatusDTO;
import org.openhab.binding.thekeys.internal.gateway.TheKeysGatewayHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
 * The {@link TheKeysSmartlockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysSmartlockHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TheKeysSmartlockHandler.class);

    private static final int BATTERY_MIN_LEVEL_MV = 6200;
    private static final int BATTERY_MAX_LEVEL_MV = 8000;
    private static final int BATTERY_SECURITY_MV = 200;

    private @Nullable TheKeysSmartlockConfiguration config;
    private @Nullable TheKeysGatewayHandler gateway;
    /**
     * Identifier of the last event from the gateway
     */
    private int lastLog = -1;

    public TheKeysSmartlockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TheKeysSmartlockConfiguration.class);
        gateway = (TheKeysGatewayHandler) getBridge().getHandler();
        lastLog = -1;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                "@text/message.status.wait-data-from-gateway");

        checkIfBridgeOffline();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.submit(this::fetchAndUpdateLockStatus);
        } else if (CHANNEL_LOCK.equals(channelUID.getId())) {
            scheduler.submit(() -> lock((OnOffType) command));
        }
    }

    /**
     * Lock command
     */
    private void lock(OnOffType open) {
        try {
            updateState(CHANNEL_SYNC_IN_PROGRESS, OnOffType.ON);
            if (open == OnOffType.ON) {
                gatewayApi().open(config.lockId);
            } else {
                gatewayApi().close(config.lockId);
            }
        } catch (Exception e) {
            logger.debug("Fail to execute {} request", open == OnOffType.OFF ? "open" : "close", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void checkIfBridgeOffline() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Fetch the lock state from the gateway
     *
     * @return true if succeeded
     */
    private boolean fetchAndUpdateLockStatus() {
        try {
            updateState(CHANNEL_SYNC_IN_PROGRESS, OnOffType.ON);
            LockerStatusDTO lockStatus = gatewayApi().getLockStatus(config.lockId);
            if ("ko".equals(lockStatus.getStatus())) {
                throw new TheKeysError(
                        "Request failed with code " + lockStatus.getCode() + ". " + lockStatus.getCause());
            }

            updateState(CHANNEL_STATUS, new StringType(lockStatus.getStatus()));
            updateState(CHANNEL_LOCK, OnOffType.from(!lockStatus.isClosed()));
            updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(getBatteryLevel(lockStatus.getBattery())));
            updateState(CHANNEL_LOW_BATTERY, OnOffType.from(isLowBattery(lockStatus.getBattery())));
            updateState(CHANNEL_POSITION, new DecimalType(lockStatus.getPosition()));
            updateState(CHANNEL_RSSI, new DecimalType(lockStatus.getRssi()));
            updateState(CHANNEL_LAST_SYNC, new DateTimeType(ZonedDateTime.now()));

            updateProperty(PROPERTY_VERSION, String.valueOf(lockStatus.getVersion()));

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            updateState(CHANNEL_SYNC_IN_PROGRESS, OnOffType.OFF);
            return true;
        } catch (Exception e) {
            logger.debug("Fail to fetch data of the lock", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        }
    }

    public void onLockNotFoundOnGateway() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/message.status.lock-no-found");
    }

    /**
     * Called from the gateway when data fetched
     *
     * @param data The data from the gateway
     */
    public void updateLockState(LockerDTO data) {
        int lastLogUpdated = data.getLastLog();

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (lastLogUpdated > lastLog && fetchAndUpdateLockStatus()) {
            lastLog = lastLogUpdated;
        }
    }

    /**
     * Convert battery level to percentage
     *
     * @param batteryMv Battery level in mV
     * @return The percentage between 0 and 100
     */
    private int getBatteryLevel(int batteryMv) {
        double level = (double) (batteryMv - BATTERY_SECURITY_MV - BATTERY_MIN_LEVEL_MV)
                / (BATTERY_MAX_LEVEL_MV - BATTERY_MIN_LEVEL_MV);
        int batteryPercent = (int) Math.floor(level * 100);
        return Math.max(0, Math.min(100, batteryPercent));
    }

    /**
     * Determine if the battery is low based on the batteryLevel
     *
     * @param batteryLevel Battery level in mV
     * @return true if the battery is under 20%
     */
    private boolean isLowBattery(int batteryLevel) {
        return getBatteryLevel(batteryLevel) <= 20;
    }

    public int getLockIdentifier() {
        return Objects.requireNonNull(config).lockId;
    }

    private GatewayService gatewayApi() {
        return Objects.requireNonNull(Objects.requireNonNull(gateway).getApi());
    }

    @Override
    public void dispose() {
        super.dispose();
        lastLog = -1;
    }
}
