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
package org.openhab.binding.loqed.internal;

import static org.openhab.binding.loqed.internal.LoqedBindingConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.loqed.internal.api.BoltState;
import org.openhab.binding.loqed.internal.api.LoqedApiException;
import org.openhab.binding.loqed.internal.api.LoqedAuthenticationException;
import org.openhab.binding.loqed.internal.api.LoqedConfigurationException;
import org.openhab.binding.loqed.internal.api.LoqedLockData;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a single LOQED lock.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLockHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LoqedLockHandler.class);
    private final Object lifecycleLock = new Object();
    private String lockId = "";
    private String keySecret = "";
    private int localKeyId = -1;
    private @Nullable ScheduledFuture<?> initializationJob;
    private long lifecycleGeneration;

    public LoqedLockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        long generation;
        synchronized (lifecycleLock) {
            lifecycleGeneration++;
            cancelInitializationJob();
            generation = lifecycleGeneration;
        }
        LoqedConfiguration config = getConfigAs(LoqedConfiguration.class);
        String configuredLockId = config.lockId;
        String configuredKeySecret = config.keySecret;
        int configuredLocalKeyId = config.localKeyId;
        synchronized (lifecycleLock) {
            if (generation != lifecycleGeneration) {
                return;
            }
            lockId = configuredLockId;
            keySecret = configuredKeySecret;
            localKeyId = configuredLocalKeyId;
        }
        if (configuredLockId.isBlank()) {
            updateInitializationStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status.loqed.lock.id-empty");
            return;
        }
        synchronized (lifecycleLock) {
            if (generation == lifecycleGeneration) {
                updateStatus(ThingStatus.UNKNOWN);
                initializationJob = scheduler.schedule(
                        () -> initializeConnection(generation, configuredKeySecret, configuredLocalKeyId), 0,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void initializeConnection(long generation, String configuredKeySecret, int configuredLocalKeyId) {
        try {
            LoqedBridge bridgeHandler = getBridgeHandler();
            if (bridgeHandler == null) {
                updateInitializationStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "@text/thing-status.loqed.lock.bridge-unavailable");
            } else if (bridgeHandler.requiresLocalCredentials()
                    && (configuredKeySecret.isBlank() || configuredLocalKeyId < 0 || configuredLocalKeyId > 250)) {
                updateInitializationStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.lock.local-credentials-invalid");
            } else {
                List<LoqedLockData> refreshedLocks = bridgeHandler.refreshAndGetLocks();
                synchronized (lifecycleLock) {
                    if (generation == lifecycleGeneration) {
                        updateFromBridge(refreshedLocks);
                    }
                }
            }
        } finally {
            synchronized (lifecycleLock) {
                if (generation == lifecycleGeneration) {
                    initializationJob = null;
                }
            }
        }
    }

    private void updateInitializationStatus(long generation, ThingStatus status, ThingStatusDetail statusDetail,
            String description) {
        synchronized (lifecycleLock) {
            if (generation == lifecycleGeneration) {
                updateStatus(status, statusDetail, description);
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (lifecycleLock) {
            lifecycleGeneration++;
            cancelInitializationJob();
        }
    }

    private void cancelInitializationJob() {
        ScheduledFuture<?> job = initializationJob;
        if (job != null) {
            job.cancel(true);
            initializationJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            LoqedBridge bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                scheduler.execute(bridgeHandler::refresh);
            }
            return;
        }

        @Nullable
        BoltState boltState = null;
        if (CHANNEL_LOCK.equals(channelUID.getId()) && command instanceof OnOffType onOffCommand) {
            boltState = onOffCommand == OnOffType.ON ? BoltState.NIGHT_LOCK : BoltState.DAY_LOCK;
        } else if (CHANNEL_BOLT_STATE.equals(channelUID.getId()) && command instanceof StringType stringCommand) {
            boltState = BoltState.fromApiValue(stringCommand.toString()).filter(state -> state != BoltState.UNKNOWN)
                    .orElse(null);
        }

        if (boltState != null) {
            long generation;
            String commandLockId;
            String commandKeySecret;
            int commandLocalKeyId;
            @Nullable
            LoqedBridge bridgeHandler;
            synchronized (lifecycleLock) {
                generation = lifecycleGeneration;
                commandLockId = lockId;
                commandKeySecret = keySecret;
                commandLocalKeyId = localKeyId;
                bridgeHandler = getBridgeHandler();
            }
            BoltState requestedState = boltState;
            scheduler.execute(() -> setBoltState(generation, bridgeHandler, commandLockId, commandKeySecret,
                    commandLocalKeyId, requestedState));
        }
    }

    private void setBoltState(long generation, @Nullable LoqedBridge bridgeHandler, String commandLockId,
            String commandKeySecret, int commandLocalKeyId, BoltState boltState) {
        if (!isCurrentGeneration(generation)) {
            return;
        }
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "@text/thing-status.loqed.lock.bridge-unavailable");
            return;
        }
        try {
            long stateUpdateSequence = bridgeHandler.getStateUpdateSequence(boltState);
            bridgeHandler.setBoltState(commandLockId, commandKeySecret, commandLocalKeyId, boltState);
            if (isCurrentGeneration(generation)) {
                scheduler.schedule(() -> refreshAfterCommand(generation, bridgeHandler, boltState, stateUpdateSequence),
                        10, TimeUnit.SECONDS);
            }
        } catch (LoqedAuthenticationException | LoqedConfigurationException e) {
            logger.debug("Could not set LOQED lock {} to {}", commandLockId, boltState, e);
            if (isCurrentGeneration(generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.lock.command-configuration-error");
            }
        } catch (LoqedApiException e) {
            logger.debug("Could not set LOQED lock {} to {}", commandLockId, boltState, e);
            if (isCurrentGeneration(generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing-status.loqed.lock.command-communication-error");
            }
        }
    }

    private void refreshAfterCommand(long generation, LoqedBridge bridgeHandler, BoltState boltState,
            long stateUpdateSequence) {
        if (isCurrentGeneration(generation) && (!bridgeHandler.usesPushUpdates()
                || bridgeHandler.getStateUpdateSequence(boltState) == stateUpdateSequence)) {
            logger.debug("No LOQED state update received after command, refreshing lock status");
            bridgeHandler.refresh();
        }
    }

    private boolean isCurrentGeneration(long generation) {
        synchronized (lifecycleLock) {
            return generation == lifecycleGeneration;
        }
    }

    public void updateFromBridge(List<LoqedLockData> locks) {
        locks.stream().filter(lock -> lockId.equals(lock.id)).findFirst().ifPresentOrElse(this::updateChannels, () -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-status.loqed.lock.unavailable");
            getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        });
    }

    private void updateChannels(LoqedLockData lock) {
        if (!lock.online) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-status.loqed.lock.disconnected");
            updateState(CHANNEL_BOLT_STATE, UnDefType.UNDEF);
            updateState(CHANNEL_LOCK, UnDefType.UNDEF);
            updateState(CHANNEL_BATTERY_LEVEL, UnDefType.UNDEF);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_BOLT_STATE, new StringType(lock.boltState.name()));
        updateState(CHANNEL_LOCK, lock.boltState == BoltState.UNKNOWN ? UnDefType.UNDEF
                : OnOffType.from(lock.boltState == BoltState.NIGHT_LOCK));
        if (lock.batteryPercentage >= 0) {
            updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(lock.batteryPercentage));
        } else {
            updateState(CHANNEL_BATTERY_LEVEL, UnDefType.UNDEF);
        }
        updateState(CHANNEL_BATTERY_TYPE, new StringType(lock.batteryType));
        updateOptionalState(CHANNEL_PARTY_MODE, lock.partyMode);
        updateOptionalState(CHANNEL_GUEST_ACCESS, lock.guestAccessMode);
        updateOptionalState(CHANNEL_TWIST_ASSIST, lock.twistAssist);
        updateOptionalState(CHANNEL_TOUCH_TO_CONNECT, lock.touchToConnect);
    }

    private void updateOptionalState(String channelId, @Nullable Boolean value) {
        updateState(channelId, value == null ? UnDefType.UNDEF : OnOffType.from(value));
    }

    private @Nullable LoqedBridge getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof LoqedBridge handler ? handler : null;
    }
}
