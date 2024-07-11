/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.device.api.protocol.elements;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.configuration.IScheduleConfigurationProvider.ScheduleTimerType;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement.IArgoElement;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.device.api.types.TimerType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of common functionality across all API elements
 * (ex. handling pending commands and their confirmations)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public abstract class ArgoApiElementBase implements IArgoElement {
    ///////////
    // TYPES
    ///////////
    /**
     * Helper class for handling (pending) commands sent to the device (and awaiting confirmation)
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public static class HandleCommandResult {
        public final boolean handled;
        public final Optional<String> deviceCommandToSend;
        public final Optional<State> plannedState;
        private final Instant updateRequestedTime;
        private boolean deferred = false;
        private boolean requiresDeviceConfirmation = true;

        /**
         * Private C-tor
         *
         * @param handled If the command was handled
         * @param deviceCommandToSend The actual command to send to device (only if {@code handled=True})
         * @param plannedState The expected state of the device after the command (reaching it will serve as
         *            confirmation). present only if {@code handled=True}.
         */
        private HandleCommandResult(boolean handled, Optional<String> deviceCommandToSend,
                Optional<State> plannedState) {
            this.updateRequestedTime = Instant.now();
            this.handled = handled;
            this.deviceCommandToSend = deviceCommandToSend;
            this.plannedState = plannedState;
        }

        /**
         * Named c-tor for rejected command
         *
         * @return Rejected command ({@code handled = False})
         */
        public static HandleCommandResult rejected() {
            return new HandleCommandResult(false, Optional.empty(), Optional.empty());
        }

        /**
         * Named c-tor for accepted command
         * <p>
         * By default the command starts with: {@link #isConfirmable() confirmable}{@code =True} and
         * {@link #isDeferred() deferred}{@code =False}, which means caller expect device-side confirmation and the
         * command is effective immediately after sending to the device (standalone command)
         *
         * @param deviceCommandToSend The actual command to send to device
         * @param plannedState The expected state of the device after the command (if {@link #isConfirmable()
         *            confirmable} is {@code True}, reaching it will serve as confirmation)
         * @return Accepted command ({@code confirmable=True & deferred=False} - changeable via
         *         {@link #setConfirmable(boolean)} or {@link #setDeferred(boolean)})
         */
        public static HandleCommandResult accepted(String deviceCommandToSend, State plannedState) {
            return new HandleCommandResult(true, Optional.of(deviceCommandToSend), Optional.of(plannedState));
        }

        /**
         * Check if this command is stale (has been issued before
         * {@link ArgoClimaBindingConstants#PENDING_COMMAND_EXPIRE_TIME} ago.
         *
         * @implNote This class does NOT track actual command completion (only their issuance), hence it is expected
         *           that a completed command will be simply removed by the caller.
         * @implNote For the same reason, even though this check only makes sense for {@code confirmable} commands - it
         *           is not checked herein and responsibility of the caller
         * @return True if the command is obsolete (has been issued more than expire time ago)
         */
        public boolean hasExpired() {
            return Duration.between(updateRequestedTime, Instant.now())
                    .compareTo(ArgoClimaBindingConstants.PENDING_COMMAND_EXPIRE_TIME) > 0;
        }

        /**
         * Check if the command is confirmable (for R/W params, where the device acknowledges receipt of the command)
         *
         * @return True if the command is confirmable. False for write-only parameters
         */
        public boolean isConfirmable() {
            return requiresDeviceConfirmation;
        }

        /**
         * Set confirmable status (update from default: true)
         *
         * @param requiresDeviceConfirmation New {@code confirmable} value
         * @return This object (for chaining)
         */
        public HandleCommandResult setConfirmable(boolean requiresDeviceConfirmation) {
            this.requiresDeviceConfirmation = requiresDeviceConfirmation;
            return this;
        }

        /**
         * Check if the command is deferred
         * <p>
         * A command is considered "deferred", if it isn't standalone, and - even when sent to the device - doesn't
         * yield an immediate effect.
         * For example, setting a delay timer value, when the device is not in a timer mode doesn't make any meaningful
         * change to the device (until said mode is entered, which is controlled by different API element)
         *
         * @return True if the command is deferred (has no immediate effect). False - otherwise
         */
        public boolean isDeferred() {
            return deferred;
        }

        /**
         * Set deferred status (update from default: false)
         *
         * @see #isDeferred()
         * @param deferred New {@code deferred} value
         * @return This object (for chaining)
         */
        public HandleCommandResult setDeferred(boolean deferred) {
            this.deferred = deferred;
            return this;
        }

        @Override
        public String toString() {
            return String.format("HandleCommandResult(wasHandled=%s,deviceCommand=%s,plannedState=%s,isObsolete=%s)",
                    handled, deviceCommandToSend, plannedState, hasExpired());
        }
    }

    /**
     * Types of command finalization (reason why command is no longer tracked/retried)
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public enum CommandFinalizationReason {
        /** Command is confirmable and device confirmed now having the desired state */
        CONFIRMED_APPLIED,
        /** Command is not-confirmable has been just sent to the device (in good faith) */
        SENT_NON_CONFIRMABLE,
        /** Pending command has been aborted by the caller */
        ABORTED,
        /**
         * Pending (confirmable) command has not received confirmation within
         * {@link ArgoClimaBindingConstants#PENDING_COMMAND_EXPIRE_TIME}
         */
        EXPIRED
    }

    ///////////
    // FIELDS
    ///////////
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgoApiElementBase.class);
    protected final IArgoSettingProvider settingsProvider;

    /**
     * Last status value received from device (has most accurate device-side state, but may be stale if there are
     * in-flight commands!)
     */
    private Optional<String> lastRawValueFromDevice = Optional.empty();

    /**
     * Active (in-flight) change request (upon accepting framework's Command) issued against this element. Tracked since
     * acceptance (before send to the device) all the way to finalization (confirmed/successful, but also aborted,
     * non-confirmable etc.)
     */
    private Optional<HandleCommandResult> inFlightCommand = Optional.empty();

    /**
     * Internal (element type-specific) method for handling the command (accepting or rejecting it).
     *
     * @implNote Tracking of command result is handled by this class through {@link #handleCommand(Command, boolean)}
     *
     * @param command The command to handle
     * @return Handling result (an accepted or rejected command, with handling traits such as confirmable/deferred)
     */
    protected abstract HandleCommandResult handleCommandInternalEx(Command command);

    /**
     * Internal (element type-specific) method for handling the element status update.
     *
     * @implNote Tracking of command confirmations and/or expiration is handled by this class through
     *           {@link #updateFromApiResponse(String)}
     * @param responseValue The raw API value (from device)
     */
    protected abstract void updateFromApiResponseInternal(String responseValue);

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     */
    public ArgoApiElementBase(IArgoSettingProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    public final State updateFromApiResponse(String responseValue) {
        var noPendingUpdates = !isUpdatePending(); // Capturing the current in-flight state (before modifying this
                                                   // object and introducing side-effects)

        synchronized (this) {
            this.lastRawValueFromDevice = Optional.of(responseValue); // Persist last value from device (Side-effect:
                                                                      // may change behavior of isUpdatePending()
            if (noPendingUpdates) {
                this.updateFromApiResponseInternal(responseValue); // No in-flight commands => Update THIS object with
                                                                   // the new state

                if (!this.hasInFlightCommand()) {
                    // No in-flight command, we're done
                    return this.toState();
                }
            }
        }

        // There's an ongoing confirmable command (not yet acknowledged), so we're *NOT* simply taking device-side
        // value as the ACTUAL one (b/c it is slow to respond and we don't want values flapping). Instead, we try to
        // see if the value is matching what we'd expect to change (confirming our command)
        var expectedStateValue = getInFlightCommandsRawValueOrDefault();

        if (responseValue.equals(expectedStateValue)) { // Comparing by raw values, not by planned state
            confirmPendingCommand(CommandFinalizationReason.CONFIRMED_APPLIED);
        } else if (this.inFlightCommand.map(x -> x.hasExpired()).orElse(false)) {
            confirmPendingCommand(CommandFinalizationReason.EXPIRED);
        } else {
            LOGGER.debug("Update made, but values mismatch... {} (device) != {} (command)", responseValue,
                    expectedStateValue);
        }
        return this.toState(); // Return previous state (of the pending command, not the one device just reported)
    }

    @Override
    public final void notifyCommandSent() {
        if (this.isUpdatePending()) {
            inFlightCommand.ifPresent(cmd -> {
                if (!cmd.isConfirmable()) {
                    confirmPendingCommand(CommandFinalizationReason.SENT_NON_CONFIRMABLE);
                }
            });
        }
    }

    @Override
    public final void abortPendingCommand() {
        confirmPendingCommand(CommandFinalizationReason.ABORTED);
    }

    @Override
    public String toString() {
        return String.format("RAW[%s]", lastRawValueFromDevice.orElse("N/A"));
    }

    @Override
    public final boolean isUpdatePending() {
        if (!hasInFlightCommand()) {
            return false;
        }

        // Check if the device is not already reporting the requested state (nothing pending if so)
        // (not inlining this code for better readability)
        var deviceReportsValueAlready = lastRawValueFromDevice
                .map(devValue -> devValue.equals(getInFlightCommandsRawValueOrDefault())).orElse(false);
        return !deviceReportsValueAlready;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Wrapper implementation for handling confirmations/deferrals. Delegates actual work to
     * {@link #handleCommandInternalEx(Command)}
     */
    @Override
    public final boolean handleCommand(Command command, boolean isConfirmable) {
        var result = this.handleCommandInternalEx(command);

        if (result.handled) {
            if (!isConfirmable) {
                // The value is not confirmable (upon sending to the device, we'll just assume it will flip to the
                // desired state)
                result.setConfirmable(false);
            }
            if (!result.isDeferred()) {
                // Deferred commands do not count as in-flight (will get intercepted when other command uses their
                // value)
                synchronized (this) {
                    this.inFlightCommand = Optional.of(result);
                }
            }
        }
        return result.handled;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation of a typical param, which is NOT always sent (to be further overridden in inheriting
     * classes)
     */
    @Override
    public boolean isAlwaysSent() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation (to be further overridden in inheriting classes) getting pending command or
     * {@code NO_VALUE} special value to not effect any change
     */
    @Override
    public String getDeviceApiValue() {
        if (!isUpdatePending()) {
            return ArgoDeviceStatus.NO_VALUE;
        }
        return this.inFlightCommand.get().deviceCommandToSend.get();
    }

    /**
     * Helper method to check if any one of the schedule timers is currently running
     *
     * @return Index of one of the schedule timers (1|2|3) which is currently active on the device. Empty optional -
     *         otherwise
     */
    protected final Optional<ScheduleTimerType> isScheduleTimerEnabled() {
        var currentTimer = EnumParam
                .fromType(settingsProvider.getSetting(ArgoDeviceSettingType.ACTIVE_TIMER).getState(), TimerType.class);

        if (currentTimer.isEmpty()) {
            return Optional.empty();
        }

        switch (currentTimer.orElseThrow()) {
            case SCHEDULE_TIMER_1:
            case SCHEDULE_TIMER_2:
            case SCHEDULE_TIMER_3:
                return Optional.of(TimerType.toScheduleTimerType(currentTimer.orElseThrow()));
            default:
                return Optional.empty();
        }
    }

    /**
     * Called when an in-flight command reaches a final state (successful or not) and no longer requires tracking
     *
     * @param reason The reason for finalizing the command (for logging)
     */
    private final void confirmPendingCommand(CommandFinalizationReason reason) {
        var commandName = inFlightCommand.map(c -> c.plannedState.map(s -> s.toFullString()).orElse("N/A"))
                .orElse("Unknown");
        switch (reason) {
            case CONFIRMED_APPLIED:
                LOGGER.debug("[{}] Update confirmed!", commandName);
                break;
            case ABORTED:
                LOGGER.debug("[{}] Command aborted!", commandName);
                break;
            case EXPIRED:
                LOGGER.debug("[{}] Long-pending update found. Cancelling...!", commandName);
                break;
            case SENT_NON_CONFIRMABLE:
                LOGGER.debug("[{}] Update confirmed (in good faith)!", commandName);
                break;
        }
        synchronized (this) {
            this.inFlightCommand = Optional.empty();
        }
    }

    @Override
    public final boolean hasInFlightCommand() {
        if (inFlightCommand.isEmpty()) {
            return false; // no withstanding command
        }

        // If last command was not handled correctly -> there's nothing to update
        return inFlightCommand.map(c -> c.handled).orElse(false);
    }

    private final String getInFlightCommandsRawValueOrDefault() {
        final String valueNotAvailablePlaceholder = "N/A";
        return Objects
                .requireNonNull(inFlightCommand.map(c -> c.deviceCommandToSend.orElse(valueNotAvailablePlaceholder))
                        .orElse(valueNotAvailablePlaceholder));
    }

    /////////////
    // HELPERS
    /////////////
    /**
     * Utility function trying to convert from String to int
     *
     * @param value Value to convert
     * @return Converted value (if successful) or empty (on failure)
     */
    protected static Optional<Integer> strToInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            LOGGER.trace("The value {} is not a valid integer. Error: {}", value, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Normalize the value to be within range (and multiple of step, if any)
     *
     * @param <T> The number type
     * @param newValue Value to convert
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param step Optional step for the value (result will be rounded to nearest step)
     * @param unitDescription Unit description (for logging)
     * @return Range within MIN..MAX bounds (which is a multiple of step). Returned as a {@code Number} for the caller
     *         to convert back to the desired type. Note we're not casting back to {@code T} as it would need to be an
     *         unchecked cast
     */
    protected static <T extends Number & Comparable<T>> Number adjustRange(T newValue, final T minValue,
            final T maxValue, final Optional<T> step, final String unitDescription) {
        if (newValue.compareTo(minValue) < 0) {
            LOGGER.debug("Requested value: [{}{}] would exceed minimum value: [{}{}]. Setting: {}{}.", newValue,
                    unitDescription, minValue, unitDescription, minValue, unitDescription); // The over-repetition is
                                                                                            // due to SLF4J formatter
                                                                                            // not supporting numbered
                                                                                            // params, and using full
                                                                                            // MessageFormat is not only
                                                                                            // an overkill but also
                                                                                            // SLOWER
            return minValue;
        }
        if (newValue.compareTo(maxValue) > 0) {
            LOGGER.debug("Requested value: [{}{}] would exceed maximum value: [{}{}]. Setting: {}{}.", newValue,
                    unitDescription, maxValue, unitDescription, maxValue, unitDescription); // See comment above
            return maxValue;
        }

        if (step.isEmpty()) {
            return newValue; // No rounding to step value
        }

        return Math.round(newValue.doubleValue() / step.orElseThrow().doubleValue()) * step.orElseThrow().doubleValue();
    }

    /**
     * Normalizes the incoming value (respecting steps), with amplification of movement
     * <p>
     * Ex. if the step is 10, current value is 50 and the new value is 51... while 50 is still a closest, we're moving
     * to a full next step (60), not to ignore user's intent to change something
     *
     * @param newValue Value to convert
     * @param currentValue The current value to amplify (in case normalization wouldn't otherwise change anything). If
     *            empty, this method doesn't amplify anything
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param step Optional step for the value (result will be rounded to nearest step)
     * @param unitDescription Unit description (for logging)
     * @return Sanitized value (with amplified movement). Returned as a {@code Number} for the caller
     *         to convert back to the desired type. Note we're not casting back to {@code T} as it would need to be an
     *         unchecked cast
     */
    protected static <T extends Number & Comparable<T>> Number adjustRangeWithAmplification(T newValue,
            Optional<T> currentValue, final T minValue, final T maxValue, final T step, final String unitDescription) {
        Number normalized = adjustRange(newValue, minValue, maxValue, Optional.of(step), unitDescription);

        if (currentValue.isEmpty() || normalized.doubleValue() == newValue.doubleValue()
                || newValue.compareTo(minValue) < 0 || newValue.compareTo(maxValue) > 0) {
            return normalized; // there was no previous value or normalization didn't remove any precision or reached a
                               // boundary -> new normalized value wins
        }

        final Number thisValue = currentValue.orElseThrow();
        if (normalized.doubleValue() != thisValue.doubleValue()) {
            return normalized; // the normalized value changed enough to be meaningful on its own-> use it
        }

        // Value before normalization has moved, but not enough to move a step (and would have been ignored). Let's
        // amplify that effect and add a new step
        var movementDirection = Math.signum((newValue.doubleValue() - normalized.doubleValue()));
        return normalized.doubleValue() + movementDirection * step.doubleValue();
    }
}
