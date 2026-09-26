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

package org.openhab.binding.tedee.internal.handler;

import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeClient;
import org.openhab.binding.tedee.internal.api.TedeeLock;
import org.openhab.binding.tedee.internal.configuration.TedeeLockConfiguration;
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
 * 
 * Handler for a Tedee lock.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeLockHandler extends BaseThingHandler {
    private TedeeLockConfiguration cfg = new TedeeLockConfiguration();
    private @Nullable ScheduledFuture<?> job;
    private final Logger logger = LoggerFactory.getLogger(TedeeLockHandler.class);

    public TedeeLockHandler(Thing t) {
        super(t);
    }

    @Override
    public void initialize() {
        cfg = getConfigAs(TedeeLockConfiguration.class);
        if (cfg.deviceId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID required");
            return;
        }

        int n = Math.max(2, cfg.pollInterval);

        logger.info("Starting polling for Tedee lock {} every {} seconds", cfg.deviceId, n);

        job = scheduler.scheduleWithFixedDelay(this::refresh, 0, n, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID c, Command x) {
        if (x instanceof RefreshType) {
            logger.debug("Refresh requested for Tedee lock {}", cfg.deviceId);
            scheduler.execute(this::refresh);
            return;
        }

        logger.debug("Command '{}' received on channel '{}' for Tedee lock {}", x, c.getId(), cfg.deviceId);

        TedeeTransportProvider provider = transportProvider();
        if (provider == null || !provider.ready()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport offline");
            return;
        }

        TedeeClient client = provider.getClient();
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport API not initialized");
            return;
        }

        if (TedeeBindingConstants.C_LOCK.equals(c.getId()) && x instanceof OnOffType) {
            o(client, x);
        } else if (TedeeBindingConstants.C_ACTION.equals(c.getId()) && x instanceof StringType) {
            send(client, x.toString().toUpperCase(Locale.ROOT));
        }
    }

    private void o(TedeeClient client, Command x) {
        send(client, x == OnOffType.ON ? "LOCK" : "UNLOCK");
    }

    private void send(TedeeClient client, String action) {
    scheduler.execute(() -> {
        try {
            logger.debug("Sending Tedee action '{}' to lock {}", action, cfg.deviceId);

            switch (action) {
                case "LOCK" -> client.lock(cfg.deviceId);
                case "UNLOCK" -> client.unlockOrPull(cfg.deviceId);
                case "UNLOCK_NO_PULL" -> client.unlockWithoutPull(cfg.deviceId);
                case "PULL" -> client.pull(cfg.deviceId);
                default -> {
                    logger.warn("Unsupported Tedee action '{}'", action);
                    return;
                }
            }

            logger.debug("Tedee action '{}' accepted for lock {}", action, cfg.deviceId);

            scheduler.schedule(this::refresh, 1, TimeUnit.SECONDS);
            scheduler.schedule(this::refresh, 3, TimeUnit.SECONDS);
        } catch (TedeeApiException e) {
            logger.warn("Tedee action '{}' failed for lock {}: {}", action, cfg.deviceId, e.getMessage());

            updateStatus(ThingStatus.OFFLINE,
                    e.getStatus() == 401 || e.getStatus() == 403 ? ThingStatusDetail.CONFIGURATION_ERROR
                            : ThingStatusDetail.COMMUNICATION_ERROR,
                    e.getMessage());
        }
    });
}

    private void refresh() {
        logger.info("Polling Tedee lock {}", cfg.deviceId);

        TedeeTransportProvider provider = transportProvider();
        if (provider == null || !provider.ready()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport offline");
            return;
        }

        TedeeClient client = provider.getClient();
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport API not initialized");
            return;
        }

        try {
            TedeeLock l = client.getLock(cfg.deviceId);
            logger.debug(
                    "Tedee lock {} status: state={}, doorState={}, battery={}%, charging={}, jammed={}, connected={}, firmware='{}'",
                    cfg.deviceId, l.state, l.doorState, l.batteryLevel, l.isCharging, l.jammed, l.isConnected,
                    l.version);

            if (l.state == 6) {
                updateState(TedeeBindingConstants.C_LOCK, OnOffType.ON);
            } else if (l.state == 2) {
                updateState(TedeeBindingConstants.C_LOCK, OnOffType.OFF);
            }

            updateState(TedeeBindingConstants.C_STATE, new StringType(state(l.state)));
            updateState(TedeeBindingConstants.C_DOOR, new StringType(door(l.doorState)));
            updateState(TedeeBindingConstants.C_BATTERY, new DecimalType(l.batteryLevel));
            updateState(TedeeBindingConstants.C_CHARGING, OnOffType.from(l.isCharging != 0));
            updateState(TedeeBindingConstants.C_JAMMED, OnOffType.from(l.jammed != 0));
            updateState(TedeeBindingConstants.C_CONNECTED, OnOffType.from(l.isConnected != 0));
            updateState(TedeeBindingConstants.C_AUTO_LOCK_ENABLED,
                    OnOffType.from(l.deviceSettings.autoLockEnabled != 0));

            updateState(TedeeBindingConstants.C_AUTO_LOCK_DELAY, new DecimalType(l.deviceSettings.autoLockDelay));

            updateState(TedeeBindingConstants.C_AUTO_LOCK_IMPLICIT_ENABLED,
                    OnOffType.from(l.deviceSettings.autoLockImplicitEnabled != 0));

            updateState(TedeeBindingConstants.C_AUTO_LOCK_IMPLICIT_DELAY,
                    new DecimalType(l.deviceSettings.autoLockImplicitDelay));

            updateState(TedeeBindingConstants.C_PULL_SPRING_ENABLED,
                    OnOffType.from(l.deviceSettings.pullSpringEnabled != 0));

            updateState(TedeeBindingConstants.C_PULL_SPRING_DURATION,
                    new DecimalType(l.deviceSettings.pullSpringDuration));

            updateState(TedeeBindingConstants.C_AUTO_PULL_SPRING_ENABLED,
                    OnOffType.from(l.deviceSettings.autoPullSpringEnabled != 0));

            updateState(TedeeBindingConstants.C_POSTPONED_LOCK_ENABLED,
                    OnOffType.from(l.deviceSettings.postponedLockEnabled != 0));

            updateState(TedeeBindingConstants.C_POSTPONED_LOCK_DELAY,
                    new DecimalType(l.deviceSettings.postponedLockDelay));

            updateState(TedeeBindingConstants.C_BUTTON_LOCK_ENABLED,
                    OnOffType.from(l.deviceSettings.buttonLockEnabled != 0));

            updateState(TedeeBindingConstants.C_BUTTON_UNLOCK_ENABLED,
                    OnOffType.from(l.deviceSettings.buttonUnlockEnabled != 0));

            updateProperty("name", l.name);
            updateProperty("serialNumber", l.serialNumber);
            updateProperty("deviceType", "" + l.type);
            updateProperty("firmwareVersion", l.version);

            updateStatus(ThingStatus.ONLINE);
        } catch (TedeeApiException e) {
            logger.warn("Polling failed for Tedee lock {}: {}", cfg.deviceId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Unexpected error while polling Tedee lock {}", cfg.deviceId, e);
        }
    }

    void handleWebhook(int state, int doorState, int jammed) {
        logger.debug("Applying Tedee webhook to lock {}: state={}, doorState={}, jammed={}", cfg.deviceId, state,
                doorState, jammed);

        if (state == 6) {
            updateState(TedeeBindingConstants.C_LOCK, OnOffType.ON);
        } else if (state == 2) {
            updateState(TedeeBindingConstants.C_LOCK, OnOffType.OFF);
        }

        updateState(TedeeBindingConstants.C_STATE, new StringType(state(state)));
        updateState(TedeeBindingConstants.C_DOOR, new StringType(door(doorState)));
        updateState(TedeeBindingConstants.C_JAMMED, OnOffType.from(jammed != 0));

        updateStatus(ThingStatus.ONLINE);
    }

    void handleConnectionWebhook(int isConnected) {
        logger.debug("Applying Tedee connection webhook to lock {}: isConnected={}", cfg.deviceId, isConnected);

        updateState(TedeeBindingConstants.C_CONNECTED, OnOffType.from(isConnected != 0));
        updateStatus(ThingStatus.ONLINE);
    }

    void handleBatteryWebhook(int batteryLevel) {
        logger.debug("Applying Tedee battery webhook to lock {}: batteryLevel={}", cfg.deviceId, batteryLevel);

        updateState(TedeeBindingConstants.C_BATTERY, new DecimalType(batteryLevel));
        updateStatus(ThingStatus.ONLINE);
    }

    void handleChargingWebhook(boolean charging) {
        logger.debug("Applying Tedee charging webhook to lock {}: charging={}", cfg.deviceId, charging);

        updateState(TedeeBindingConstants.C_CHARGING, OnOffType.from(charging));
        updateStatus(ThingStatus.ONLINE);
    }

    void handleSettingsWebhook() {
        logger.debug("Scheduling Tedee lock refresh after settings webhook for lock {}", cfg.deviceId);
        scheduler.execute(this::refresh);
    }

    private @Nullable TedeeTransportProvider transportProvider() {
        Bridge b = getBridge();

        if (b == null) {
            return null;
        }

        return b.getHandler() instanceof TedeeTransportProvider provider ? provider : null;
    }

    private static String state(int s) {
        return switch (s) {
            case 0 -> "UNCALIBRATED";
            case 1 -> "CALIBRATION";
            case 2 -> "OPEN";
            case 3 -> "PARTIALLY_OPEN";
            case 4 -> "OPENING";
            case 5 -> "CLOSING";
            case 6 -> "CLOSED";
            case 7 -> "PULL_SPRING";
            case 8 -> "PULLING";
            case 9 -> "UNKNOWN";
            case 255 -> "UNPULLING";
            default -> "UNKNOWN";
        };
    }

    private static String door(int s) {
        return switch (s) {
            case 0 -> "NOT_PAIRED";
            case 1 -> "CLOSED";
            case 2 -> "OPEN";
            default -> "UNKNOWN";
        };
    }

    public void dispose() {
        ScheduledFuture<?> currentJob = job;
        if (currentJob != null) {
            currentJob.cancel(true);
        }
        super.dispose();
    }
}
