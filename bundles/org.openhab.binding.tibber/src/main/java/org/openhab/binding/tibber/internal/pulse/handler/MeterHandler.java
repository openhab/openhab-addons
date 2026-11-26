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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.pulse.config.MeterConfiguration;
import org.openhab.binding.tibber.internal.pulse.sml.SmlReading;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Presents the readings of one meter, identified by the device id of its Pulse, on the channels of a {@code meter}
 * thing. The {@link BridgeHandler} hands every reading over from the single stream reader thread; this class never
 * does any I/O.
 *
 * Threading: the reader thread (readings), the shared scheduler (stale timer), and framework threads (bridge status,
 * REFRESH, initialization) all reach the state. Every access to it, and every channel or status update that depends on
 * it, runs while holding the monitor of this handler, so one decision and its publication cannot interleave with
 * another: a stale expiry cannot overwrite a fresher reading. {@link BaseThingHandler} holds the same monitor whenever
 * it calls the framework for this handler ({@code updateState}, {@code updateStatus}, {@code setCallback}), so
 * publishing under it adds no lock and no lock order of its own. The bridge handler holds no lock when it calls a
 * meter.
 *
 * Status: the meter is ONLINE only after a valid reading for its device id. The default reaction of the framework to
 * a bridge that turns ONLINE would be to set the meter ONLINE as well, which is why {@link #bridgeStatusChanged} is
 * overridden.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class MeterHandler extends BaseThingHandler {

    /** Where the meter is in its life; decides which status it shows and whether a stale timer runs. */
    private enum Phase {
        /** The configuration is invalid; bridge status changes and readings are ignored. */
        CONFIG_ERROR,
        /** The stream is not open, or the bridge is not ONLINE. */
        BRIDGE_DOWN,
        /** The stream is open and no valid reading arrived yet (status UNKNOWN). */
        WAITING,
        ONLINE,
        /** No valid reading for the stale timeout (status OFFLINE, COMMUNICATION_ERROR). */
        STALE
    }

    private static final long MIN_TIMER_DELAY_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    /** The channels that keep their value on connection loss and when stale; only a new device id clears them. */
    private static final List<String> KEPT_CHANNELS = List.of(CHANNEL_ENERGY_IMPORT, CHANNEL_ENERGY_EXPORT);

    private final LongSupplier nanoClock;

    // Guarded by this, except deviceId, which the reader thread reads without it.
    private MeterState state = new MeterState();
    private Phase phase = Phase.CONFIG_ERROR;
    private long staleNanos = TimeUnit.SECONDS.toNanos(MeterConfiguration.DEFAULT_STALE_TIMEOUT);
    private @Nullable ScheduledFuture<?> staleTimer;
    /** Invalidates timer tasks that were scheduled before a cancel, initialize, or dispose. */
    private int generation;
    private boolean disposed = true;
    private @Nullable String publishedMeterId;
    /**
     * The device id whose values the cache and the channels hold. It survives {@link #dispose()}: the framework
     * disposes and initializes the same handler instance on a configuration change, and a restart must not show or
     * keep the values of a different meter. Null until a valid configuration was applied. Guarded by this.
     */
    private @Nullable String appliedDeviceId;
    private volatile @Nullable String deviceId;

    public MeterHandler(Thing thing) {
        this(thing, System::nanoTime);
    }

    MeterHandler(Thing thing, LongSupplier nanoClock) {
        super(thing);
        this.nanoClock = nanoClock;
    }

    /**
     * A lock-free pre-filter for the bridge handler: its answer can be outdated by the time a reading is applied.
     * {@link #onReading} decides.
     *
     * @param device the device id from a stream message, in lower case
     * @return true if this meter is configured for that device
     */
    boolean matches(String device) {
        return device.equals(deviceId);
    }

    @Override
    public void initialize() {
        MeterConfiguration config = getConfigAs(MeterConfiguration.class);
        String error = config.validate();
        synchronized (this) {
            disposed = false;
            cancelTimer();
            publishedMeterId = null;
            phase = Phase.CONFIG_ERROR;
            // Without a valid device id no meter is identified; an invalid stale timeout leaves the meter as it was.
            String newDeviceId = TEXT_INVALID_DEVICE_ID.equals(error) ? null : config.normalizedDeviceId();
            resetValues(newDeviceId);
            appliedDeviceId = newDeviceId;
            if (error != null) {
                deviceId = null;
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, error);
                return;
            }
            deviceId = config.normalizedDeviceId();
            staleNanos = TimeUnit.SECONDS.toNanos(config.staleTimeout);
            phase = Phase.BRIDGE_DOWN;
            Bridge bridge = getBridge();
            if (bridge == null) {
                phase = Phase.CONFIG_ERROR;
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, TEXT_NO_BRIDGE);
                return;
            }
            applyBridgeStatus(bridge.getStatus());
        }
        // The property is not ordered with the channel and status updates, so it is published outside the block.
        updateProperty(PROPERTY_DEVICE_ID, config.normalizedDeviceId());
    }

    @Override
    public void dispose() {
        synchronized (this) {
            disposed = true;
            deviceId = null;
            cancelTimer();
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            synchronized (this) {
                if (disposed) {
                    return;
                }
                // Anything that went stale since the timer last ran must show as UNDEF, not as a cached value.
                expireLocked(nanoClock.getAsLong());
                publishCached(channelUID.getId());
            }
        }
        // All channels are read-only: other commands are ignored and never reach the bridge.
    }

    /**
     * The default implementation would set a meter that waits for its first reading ONLINE as soon as the bridge is
     * ONLINE. The meter is ONLINE only after a valid reading.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (this) {
            if (disposed || phase == Phase.CONFIG_ERROR) {
                return;
            }
            // The notifications are asynchronous and may run out of order, so the argument can be outdated. The
            // bridge's current status is the truth, whichever notification is handled last.
            Bridge bridge = getBridge();
            if (bridge != null) {
                applyBridgeStatus(bridge.getStatus());
            }
        }
    }

    /**
     * Applies a valid reading of a device to this meter if the meter is configured for that device at this moment.
     * Called on the stream reader thread. The comparison with the configured device id and the application of the
     * reading are one critical section: a reconfiguration cannot run between them, so a reading that was assigned to
     * this meter before a device id change cannot repopulate the values of the new device. A reading that is not
     * applied is dropped without any effect: no state, status, property, or timer.
     *
     * @param device the device id of the stream message header, in lower case
     * @param reading the decoded reading
     */
    void onReading(String device, SmlReading reading) {
        String meterId = reading.meterId();
        synchronized (this) {
            if (disposed || phase == Phase.CONFIG_ERROR || !device.equals(deviceId)) {
                return;
            }
            long now = nanoClock.getAsLong();
            state.apply(reading.values(), now);
            for (var entry : reading.values().entrySet()) {
                updateState(entry.getKey(), entry.getValue());
            }
            if (phase != Phase.ONLINE) {
                phase = Phase.ONLINE;
                updateStatus(ThingStatus.ONLINE);
            }
            ensureTimer(now);
            if (meterId == null || meterId.equals(publishedMeterId)) {
                meterId = null;
            } else {
                publishedMeterId = meterId;
            }
        }
        if (meterId != null) {
            updateProperty(PROPERTY_METER_ID, meterId);
        }
    }

    /**
     * Runs the stale check at once. The timer calls the same code.
     */
    void checkStale() {
        synchronized (this) {
            if (disposed || phase == Phase.CONFIG_ERROR || phase == Phase.BRIDGE_DOWN) {
                return;
            }
            cancelTimer();
            expireLocked(nanoClock.getAsLong());
        }
    }

    /** For tests: whether the stale timer is scheduled. */
    boolean isStaleTimerArmed() {
        synchronized (this) {
            return staleTimer != null;
        }
    }

    // The methods below are called with the monitor of this handler held.

    /**
     * Makes the cache and the channels consistent with the device id that is applied now. The live channels are no
     * longer attested after a (re-)initialization and become UNDEF. The energy counters are
     * cleared only when the device id changed, because the values of the previous meter must not be shown for another
     * one. On the first initialization nothing is known and nothing is written to them, so that a state restored by
     * persistence is not overwritten.
     */
    private void resetValues(@Nullable String newDeviceId) {
        String previous = appliedDeviceId;
        if (previous != null && !previous.equals(newDeviceId)) {
            state = new MeterState();
            for (String channel : KEPT_CHANNELS) {
                updateState(channel, UnDefType.UNDEF);
            }
        } else {
            state.clearLive();
            state.stopClock();
        }
        for (String channel : LIVE_CHANNELS) {
            updateState(channel, UnDefType.UNDEF);
        }
    }

    private void applyBridgeStatus(ThingStatus bridgeStatus) {
        switch (bridgeStatus) {
            case ONLINE:
                if (phase == Phase.BRIDGE_DOWN) {
                    // The stream just opened. Only a valid reading makes the meter ONLINE.
                    phase = Phase.WAITING;
                    long now = nanoClock.getAsLong();
                    state.restartClock(now);
                    updateStatus(ThingStatus.UNKNOWN);
                    ensureTimer(now);
                }
                break;
            case UNKNOWN:
                bridgeDown();
                updateStatus(ThingStatus.UNKNOWN);
                break;
            default:
                // OFFLINE, and the states in which the bridge handler is not running. The framework itself sets
                // BRIDGE_UNINITIALIZED (with the status UNINITIALIZED, which a handler must not set), so a handler
                // shows BRIDGE_OFFLINE for all of them.
                bridgeDown();
                updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /** The stream is gone: live values are no longer attested, the counters and the update time stay. */
    private void bridgeDown() {
        phase = Phase.BRIDGE_DOWN;
        cancelTimer();
        state.stopClock();
        for (String channel : state.clearLive()) {
            updateState(channel, UnDefType.UNDEF);
        }
    }

    private void expireLocked(long now) {
        MeterState.Expiry expiry = state.expire(now, staleNanos);
        for (String channel : expiry.channels()) {
            updateState(channel, UnDefType.UNDEF);
        }
        if (expiry.meter() && (phase == Phase.ONLINE || phase == Phase.WAITING)) {
            phase = Phase.STALE;
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                    TEXT_NO_READING + " [\"" + TimeUnit.NANOSECONDS.toSeconds(staleNanos) + "\"]");
        }
        ensureTimer(now);
    }

    private void publishCached(String channel) {
        QuantityType<?> value = state.value(channel);
        updateState(channel, value != null ? value : UnDefType.UNDEF);
    }

    /**
     * Makes sure one timer is armed for the earliest pending expiry. A reading never cancels or moves it: when it
     * fires early nothing has expired and it re-arms for the next expiry, about once per stale timeout.
     */
    private void ensureTimer(long now) {
        if (staleTimer != null || disposed || phase == Phase.CONFIG_ERROR || phase == Phase.BRIDGE_DOWN) {
            return;
        }
        OptionalLong due = state.nextExpiry(staleNanos);
        if (due.isEmpty()) {
            return;
        }
        long delay = Math.max(due.getAsLong() - now, MIN_TIMER_DELAY_NANOS);
        int expected = generation;
        staleTimer = scheduler.schedule(() -> onTimer(expected), delay, TimeUnit.NANOSECONDS);
    }

    private void onTimer(int expected) {
        synchronized (this) {
            if (expected != generation) {
                return;
            }
            staleTimer = null;
            checkStale();
        }
    }

    private void cancelTimer() {
        generation++;
        ScheduledFuture<?> timer = staleTimer;
        if (timer != null) {
            timer.cancel(false);
            staleTimer = null;
        }
    }
}
