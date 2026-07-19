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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PowerListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass.DeviceInfoMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the power state API for MRP. The power state is derived from
 * {@code logicalDeviceCount} in the device information messages: turning on sends
 * {@code WAKE_DEVICE_MESSAGE}, turning off performs the home-hold + select sequence.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpPower implements Power, CapabilitySource {

    /** Delay between the home hold and the select press. */
    public static final Duration DELAY_BETWEEN_COMMANDS = Duration.ofMillis(100);

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpPower.class);

    private final MrpProtocol protocol;
    private final MrpRemoteControl remote;
    private final ListenerRegistry<PowerListener> listeners;
    private final Map<PowerState, CompletableFuture<Void>> waiters = new ConcurrentHashMap<>();

    private volatile @Nullable ProtocolMessage deviceInfo;

    /**
     * Creates a new power instance.
     *
     * @param protocol protocol used for messages and device info updates
     * @param remote remote control used for the turn-off button sequence
     */
    public MrpPower(MrpProtocol protocol, MrpRemoteControl remote) {
        this.protocol = protocol;
        this.remote = remote;
        this.listeners = new ListenerRegistry<>(protocol.loop());
        protocol.listenTo(ProtocolMessage.Type.DEVICE_INFO_MESSAGE, this::updatePowerState);
        protocol.listenTo(ProtocolMessage.Type.DEVICE_INFO_UPDATE_MESSAGE, this::updatePowerState);
    }

    private PowerState currentPowerState() {
        @Nullable
        ProtocolMessage latest = deviceInfo;
        if (latest == null) {
            latest = protocol.deviceInfo().orElse(null);
        }
        return getPowerState(latest);
    }

    @Override
    public PowerState powerState() {
        return currentPowerState();
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> turnOn(boolean awaitNewState) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-turn-on").start(() -> {
            try {
                protocol.send(MrpMessages.wakeDevice()).join();
                if (awaitNewState) {
                    awaitPowerState(PowerState.On);
                }
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> turnOff(boolean awaitNewState) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-turn-off").start(() -> {
            try {
                remote.home(InputAction.Hold).join();
                Thread.sleep(DELAY_BETWEEN_COMMANDS.toMillis());
                remote.select().join();
                if (awaitNewState) {
                    awaitPowerState(PowerState.Off);
                }
                MrpFutures.completeVoid(result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result.completeExceptionally(e);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    /**
     * Blocks until the given power state has been reached. The device-info update may
     * arrive on another thread between checking the state and registering a waiter, so
     * the waiter is registered first and removed again if the state was already reached.
     */
    private void awaitPowerState(PowerState target) {
        CompletableFuture<Void> waiter = waiters.computeIfAbsent(target, state -> new CompletableFuture<>());
        if (powerState() == target) {
            waiters.remove(target, waiter);
        } else {
            waiter.join();
        }
    }

    private void updatePowerState(ProtocolMessage message) {
        PowerState oldState = powerState();
        PowerState newState = getPowerState(message);
        this.deviceInfo = message;

        if (newState != oldState) {
            LOGGER.debug("Power state changed from {} to {}", oldState, newState);
            listeners.fire(listener -> listener.powerstateUpdate(oldState, newState));
        }

        CompletableFuture<Void> waiter = waiters.remove(newState);
        if (waiter != null) {
            MrpFutures.completeVoid(waiter);
        }
    }

    private static PowerState getPowerState(@Nullable ProtocolMessage deviceInfoMessage) {
        if (deviceInfoMessage == null) {
            return PowerState.Unknown;
        }
        DeviceInfoMessage inner = (DeviceInfoMessage) MrpExtensions.extractInner(deviceInfoMessage);
        int logicalDeviceCount = inner.getLogicalDeviceCount();
        if (logicalDeviceCount >= 1) {
            return PowerState.On;
        }
        if (logicalDeviceCount == 0) {
            return PowerState.Off;
        }
        return PowerState.Unknown;
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
        return Set.of(Capability.POWER_STATE, Capability.POWER_TURN_ON, Capability.POWER_TURN_OFF);
    }

    /** Latest device info message seen by this instance (test hook). */
    Optional<ProtocolMessage> latestDeviceInfo() {
        return Optional.ofNullable(deviceInfo);
    }
}
