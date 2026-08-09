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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PowerListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the power management API for Companion.
 *
 * <p>
 * The initial state is fetched with {@code FetchAttentionState} (failures tolerated —
 * newer tvOS versions reply "No request handler"), and live updates are tracked by
 * subscribing to the {@code SystemStatus} and {@code TVSystemStatus} events.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionPower implements Power, CapabilitySource {

    private static final List<SystemStatus> ON_STATES = List.of(SystemStatus.Screensaver, SystemStatus.Awake,
            SystemStatus.Idle);

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionPower.class);

    private final CompanionApi api;
    private final ListenerRegistry<PowerListener> listeners;

    private volatile PowerState powerState = PowerState.Unknown;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     * @param core protocol context (device loop for listener dispatch)
     */
    public CompanionPower(CompanionApi api, Core core) {
        this.api = api;
        this.listeners = new ListenerRegistry<>(core.loop());
    }

    /** Whether power updates are supported (an initial state could be determined). */
    public boolean supportsPowerUpdates() {
        return powerState != PowerState.Unknown;
    }

    /**
     * Initializes the power module: snapshots the current state and subscribes to updates.
     * Blocking on futures internally; run from a dedicated thread.
     *
     * @return future completing when initialization is done
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            // Fetching the initial power state snapshot can fail on newer tvOS versions
            // where FetchAttentionState is no longer handled. This must not prevent us
            // from subscribing to live status updates.
            try {
                SystemStatus systemStatus = CompanionApi.join(api.fetchAttentionState());
                powerState = systemStatusToPowerState(systemStatus);
                LOGGER.debug("Initial power state is {}", powerState);
            } catch (Exception ex) {
                LOGGER.debug("Could not fetch initial SystemStatus ({})", ex.toString());
            }

            // Subscribe regardless, so power state can still be tracked via pushed
            // SystemStatus/TVSystemStatus events even if the initial fetch failed.
            try {
                api.listenTo("SystemStatus", this::handleSystemStatusUpdate);
                CompanionApi.join(api.subscribeEvent("SystemStatus"));

                api.listenTo("TVSystemStatus", this::handleSystemStatusUpdate);
                CompanionApi.join(api.subscribeEvent("TVSystemStatus"));
            } catch (Exception ex) {
                LOGGER.debug("Could not subscribe to SystemStatus updates ({})", ex.toString());
            }
        }, api.blockingExecutor());
    }

    @Override
    public PowerState powerState() {
        return powerState;
    }

    @Override
    public CompletableFuture<PowerState> refreshPowerState() {
        return api.fetchAttentionState().thenApply(systemStatus -> {
            PowerState oldState = powerState;
            powerState = systemStatusToPowerState(systemStatus);
            updatePowerState(oldState, powerState);
            return powerState;
        });
    }

    private void handleSystemStatusUpdate(Map<String, Object> data) {
        try {
            Long stateValue = CompanionProtocol.toLong(data.get("state"));
            if (stateValue == null) {
                throw new IllegalArgumentException("missing state");
            }
            PowerState oldState = powerState;
            powerState = systemStatusToPowerState(SystemStatus.fromValue(stateValue.intValue()));
            updatePowerState(oldState, powerState);
        } catch (Exception e) {
            LOGGER.debug("Got invalid SystemStatus: {}", data, e);
        }
    }

    private static PowerState systemStatusToPowerState(SystemStatus systemStatus) {
        if (systemStatus == SystemStatus.Asleep) {
            return PowerState.Off;
        }
        if (ON_STATES.contains(systemStatus)) {
            return PowerState.On;
        }
        return PowerState.Unknown;
    }

    private void updatePowerState(PowerState oldState, PowerState newState) {
        if (newState != oldState) {
            LOGGER.debug("Power state changed from {} to {}", oldState, newState);
            listeners.fire(listener -> listener.powerstateUpdate(oldState, newState));
        }
    }

    @Override
    public CompletableFuture<Void> turnOn(boolean awaitNewState) {
        if (awaitNewState) {
            return CompletableFuture.failedFuture(new NotSupportedError("not supported by Companion yet"));
        }
        return api.hidCommand(false, HidCommand.Wake);
    }

    @Override
    public CompletableFuture<Void> turnOff(boolean awaitNewState) {
        if (awaitNewState) {
            return CompletableFuture.failedFuture(new NotSupportedError("not supported by Companion yet"));
        }
        return api.hidCommand(false, HidCommand.Sleep);
    }

    @Override
    public void addListener(PowerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PowerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.POWER_STATE, Capability.POWER_REFRESH, Capability.POWER_TURN_ON,
                Capability.POWER_TURN_OFF);
    }
}
