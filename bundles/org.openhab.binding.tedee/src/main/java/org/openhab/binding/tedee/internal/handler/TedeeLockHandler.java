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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
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
 * Handles a Tedee lock Thing, including polling its status, processing commands,
 * and applying updates received through webhooks.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeLockHandler extends BaseThingHandler {
    private volatile TedeeLockConfiguration cfg = new TedeeLockConfiguration();
    private @Nullable ScheduledFuture<?> job;
    private final Logger logger = LoggerFactory.getLogger(TedeeLockHandler.class);
    private final Set<ScheduledFuture<?>> pendingCommands = new HashSet<>();

    private long commandGeneration;
    private boolean disposed;

    /**
     * Creates a handler for a Tedee lock.
     *
     * @param t the Tedee lock Thing
     */
    public TedeeLockHandler(Thing t) {
        super(t);
    }

    /**
     * Initializes the handler, validates the lock configuration, and schedules
     * periodic status polling.
     */
    @Override
    public void initialize() {
        ScheduledFuture<?> currentJob = job;
        if (currentJob != null) {
            currentJob.cancel(true);
            job = null;
        }

        synchronized (pendingCommands) {
            disposed = false;
            commandGeneration++;
            pendingCommands.forEach(future -> future.cancel(false));
            pendingCommands.clear();
        }

        cfg = getConfigAs(TedeeLockConfiguration.class);
        if (cfg.deviceId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device ID required");
            return;
        }

        int n = Math.max(2, cfg.pollInterval);

        logger.info("Starting polling for Tedee lock {} every {} seconds", cfg.deviceId, n);

        job = scheduler.scheduleWithFixedDelay(this::refresh, 0, n, TimeUnit.SECONDS);
    }

    /**
     * Handles commands received on the Tedee lock's channels.
     *
     * A refresh command schedules a status update. Lock and action commands
     * are forwarded to the bridge handler when the transport is available.
     *
     * @param c the channel on which the command was received
     * @param x the command to handle
     */
    @Override
    public void handleCommand(ChannelUID c, Command x) {
        final int deviceId = cfg.deviceId;

        if (x instanceof RefreshType) {
            logger.debug("Refresh requested for Tedee lock {}", deviceId);
            scheduleRefresh(0);
            return;
        }

        logger.debug("Command '{}' received on channel '{}' for Tedee lock {}", x, c.getId(), deviceId);

        TedeeTransportProvider provider = transportProvider();
        if (provider == null || !provider.ready()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport offline");
            return;
        }

        if (TedeeBindingConstants.C_LOCK.equals(c.getId()) && x instanceof OnOffType) {
            queueAction(provider, deviceId, x == OnOffType.ON ? "LOCK" : "UNLOCK");
        } else if (TedeeBindingConstants.C_ACTION.equals(c.getId()) && x instanceof StringType) {
            queueAction(provider, deviceId, x.toString().toUpperCase(Locale.ROOT));
        }
    }

    /**
     * Executes a lock action through the associated bridge's transport provider.
     *
     * @param provider the provider used to communicate with the lock
     * @param deviceId the identifier of the Tedee lock
     * @param action the action to execute
     * @throws TedeeApiException if the API request fails
     */
    void executeAction(TedeeTransportProvider provider, int deviceId, String action) throws TedeeApiException {
        switch (action) {
            case "LOCK" -> provider.lock(deviceId);
            case "UNLOCK" -> provider.unlock(deviceId);
            case "UNLOCK_NO_PULL" -> provider.unlockWithoutPull(deviceId);
            case "UNLOCK_OR_PULL" -> provider.unlockOrPull(deviceId);
            case "PULL" -> provider.pull(deviceId);
            default -> logger.warn("Unsupported Tedee action '{}'", action);
        }
    }

    private void refresh() {
        int deviceId = cfg.deviceId;
        logger.info("Polling Tedee lock {}", deviceId);

        TedeeTransportProvider provider = transportProvider();
        if (provider == null || !provider.ready()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Tedee transport offline");
            return;
        }

        try {
            TedeeLock l = provider.getLock(deviceId);
            logger.debug(
                    "Tedee lock {} status: state={}, doorState={}, battery={}%, charging={}, jammed={}, connected={}, firmware='{}'",
                    deviceId, l.state, l.doorState, l.batteryLevel, l.isCharging, l.jammed, l.isConnected, l.version);

            if (l.state == 6) {
                updateState(TedeeBindingConstants.C_LOCK, OnOffType.ON);
            } else if (l.state == 2) {
                updateState(TedeeBindingConstants.C_LOCK, OnOffType.OFF);
            }

            updateState(TedeeBindingConstants.C_STATE, new StringType(state(l.state)));

            if (l.doorState != null) {
                updateState(TedeeBindingConstants.C_DOOR, new StringType(door(l.doorState.intValue())));
            }
            if (l.batteryLevel != null) {
                updateState(TedeeBindingConstants.C_BATTERY, new DecimalType(l.batteryLevel.intValue()));
            }
            if (l.isCharging != null) {
                updateState(TedeeBindingConstants.C_CHARGING, OnOffType.from(l.isCharging != 0));
            }
            if (l.jammed != null) {
                updateState(TedeeBindingConstants.C_JAMMED, OnOffType.from(l.jammed != 0));
            }
            if (l.isConnected != null) {
                updateState(TedeeBindingConstants.C_CONNECTED, OnOffType.from(l.isConnected != 0));
            }

            TedeeLock.DeviceSettings settings = l.deviceSettings;
            if (settings != null) {
                if (settings.autoLockEnabled != null) {
                    updateState(TedeeBindingConstants.C_AUTO_LOCK_ENABLED,
                            OnOffType.from(settings.autoLockEnabled != 0));
                }
                if (settings.autoLockDelay != null) {
                    updateState(TedeeBindingConstants.C_AUTO_LOCK_DELAY,
                            new DecimalType(settings.autoLockDelay.intValue()));
                }
                if (settings.autoLockImplicitEnabled != null) {
                    updateState(TedeeBindingConstants.C_AUTO_LOCK_IMPLICIT_ENABLED,
                            OnOffType.from(settings.autoLockImplicitEnabled != 0));
                }
                if (settings.autoLockImplicitDelay != null) {
                    updateState(TedeeBindingConstants.C_AUTO_LOCK_IMPLICIT_DELAY,
                            new DecimalType(settings.autoLockImplicitDelay.intValue()));
                }
                if (settings.pullSpringEnabled != null) {
                    updateState(TedeeBindingConstants.C_PULL_SPRING_ENABLED,
                            OnOffType.from(settings.pullSpringEnabled != 0));
                }
                if (settings.pullSpringDuration != null) {
                    updateState(TedeeBindingConstants.C_PULL_SPRING_DURATION,
                            new DecimalType(settings.pullSpringDuration.intValue()));
                }
                if (settings.autoPullSpringEnabled != null) {
                    updateState(TedeeBindingConstants.C_AUTO_PULL_SPRING_ENABLED,
                            OnOffType.from(settings.autoPullSpringEnabled != 0));
                }
                if (settings.postponedLockEnabled != null) {
                    updateState(TedeeBindingConstants.C_POSTPONED_LOCK_ENABLED,
                            OnOffType.from(settings.postponedLockEnabled != 0));
                }
                if (settings.postponedLockDelay != null) {
                    updateState(TedeeBindingConstants.C_POSTPONED_LOCK_DELAY,
                            new DecimalType(settings.postponedLockDelay.intValue()));
                }
                if (settings.buttonLockEnabled != null) {
                    updateState(TedeeBindingConstants.C_BUTTON_LOCK_ENABLED,
                            OnOffType.from(settings.buttonLockEnabled != 0));
                }
                if (settings.buttonUnlockEnabled != null) {
                    updateState(TedeeBindingConstants.C_BUTTON_UNLOCK_ENABLED,
                            OnOffType.from(settings.buttonUnlockEnabled != 0));
                }
            }

            updateProperty("name", l.name);
            updateProperty("serialNumber", l.serialNumber);
            updateProperty("deviceType", "" + l.type);
            updateProperty("firmwareVersion", l.version);

            updateStatus(ThingStatus.ONLINE);
        } catch (TedeeApiException e) {
            logger.warn("Polling failed for Tedee lock {}: {}", deviceId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Unexpected error while polling Tedee lock {}", deviceId, e);
        }
    }

    /**
     * Applies a webhook update containing the lock state, door state, and jammed status.
     *
     * @param state the lock state reported by Tedee
     * @param doorState the door state reported by Tedee
     * @param jammed the jammed status reported by Tedee, where zero means not jammed
     */
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

    /**
     * Applies a webhook update for the lock's bridge connectivity.
     *
     * @param isConnected the connection status reported by Tedee, where zero means disconnected
     */
    void handleConnectionWebhook(int isConnected) {
        logger.debug("Applying Tedee connection webhook to lock {}: isConnected={}", cfg.deviceId, isConnected);

        updateState(TedeeBindingConstants.C_CONNECTED, OnOffType.from(isConnected != 0));
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Applies a webhook update for the lock's battery level.
     *
     * @param batteryLevel the battery level reported by Tedee, as a percentage
     */
    void handleBatteryWebhook(int batteryLevel) {
        logger.debug("Applying Tedee battery webhook to lock {}: batteryLevel={}", cfg.deviceId, batteryLevel);

        updateState(TedeeBindingConstants.C_BATTERY, new DecimalType(batteryLevel));
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Applies a webhook update for the lock's charging status.
     *
     * @param charging whether the lock is charging
     */
    void handleChargingWebhook(boolean charging) {
        logger.debug("Applying Tedee charging webhook to lock {}: charging={}", cfg.deviceId, charging);

        updateState(TedeeBindingConstants.C_CHARGING, OnOffType.from(charging));
        updateStatus(ThingStatus.ONLINE);
    }

    void handleSettingsWebhook() {
        logger.debug("Scheduling Tedee lock refresh after settings webhook for lock {}", cfg.deviceId);
        scheduleRefresh(0);
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
            case 1 -> "UNKNOWN"; // Disconnected
            case 2 -> "OPEN";
            case 3 -> "CLOSED";
            default -> "UNKNOWN";
        };
    }

    /**
     * Queues a lock action while binding the action to the current device configuration.
     *
     * @param provider transport provider to use for the action
     * @param deviceId device ID captured when the command was accepted
     * @param action Tedee action to execute
     */
    private void queueAction(TedeeTransportProvider provider, int deviceId, String action) {
        synchronized (pendingCommands) {
            if (disposed) {
                logger.debug("Ignoring Tedee action '{}' because the lock handler is disposed", action);
                return;
            }

            final long generation = commandGeneration;
            schedulePending(() -> runQueuedAction(provider, deviceId, action, generation), 0);
        }
    }

    private void scheduleRefresh(long delaySeconds) {
        synchronized (pendingCommands) {
            if (!disposed) {
                scheduleRefresh(delaySeconds, commandGeneration);
            }
        }
    }

    private void scheduleRefresh(long delaySeconds, long generation) {
        if (!disposed && generation == commandGeneration) {
            schedulePending(() -> runScheduledRefresh(generation), delaySeconds);
        }
    }

    private void runScheduledRefresh(long generation) {
        try {
            synchronized (pendingCommands) {
                if (disposed || generation != commandGeneration) {
                    return;
                }
            }
            refresh();
        } finally {
            synchronized (pendingCommands) {
                pendingCommands.removeIf(ScheduledFuture::isDone);
            }
        }
    }

    private void schedulePending(Runnable task, long delaySeconds) {
        pendingCommands.removeIf(ScheduledFuture::isDone);
        pendingCommands.add(scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS));
    }

    private void runQueuedAction(TedeeTransportProvider provider, int deviceId, String action, long generation) {
        try {
            synchronized (pendingCommands) {
                if (disposed || generation != commandGeneration) {
                    logger.debug("Ignoring stale Tedee action '{}' for lock {}", action, deviceId);
                    return;
                }
            }

            if (!provider.ready()) {
                logger.debug("Ignoring Tedee action '{}' because the transport is offline", action);
                return;
            }

            synchronized (pendingCommands) {
                if (disposed || generation != commandGeneration) {
                    logger.debug("Ignoring stale Tedee action '{}' for lock {}", action, deviceId);
                    return;
                }
            }

            logger.debug("Sending Tedee action '{}' to lock {}", action, deviceId);

            executeAction(provider, deviceId, action);

            logger.debug("Tedee action '{}' accepted for lock {}", action, deviceId);

            synchronized (pendingCommands) {
                if (!disposed && generation == commandGeneration) {
                    scheduleRefresh(1, generation);
                    scheduleRefresh(3, generation);
                }
            }
        } catch (TedeeApiException e) {
            logger.warn("Tedee action '{}' failed for lock {}: {}", action, deviceId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE,
                    e.getStatus() == 401 || e.getStatus() == 403 ? ThingStatusDetail.CONFIGURATION_ERROR
                            : ThingStatusDetail.COMMUNICATION_ERROR,
                    e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Tedee transport unavailable for lock {}: {}", deviceId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } finally {
            synchronized (pendingCommands) {
                pendingCommands.removeIf(ScheduledFuture::isDone);
            }
        }
    }

    /**
     * Stops scheduled polling and releases the resources associated with this handler.
     */
    @Override
    public void dispose() {
        ScheduledFuture<?> currentJob = job;
        if (currentJob != null) {
            currentJob.cancel(true);
            job = null;
        }

        synchronized (pendingCommands) {
            disposed = true;
            commandGeneration++;
            pendingCommands.forEach(future -> future.cancel(false));
            pendingCommands.clear();
        }

        super.dispose();
    }
}
