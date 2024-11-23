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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Interface for Argo API parameter (individual HMI element)
 * Carries high-level command-management options
 *
 * @see IArgoElement
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public interface IArgoCommandableElement {
    /////////
    // TYPES
    /////////
    /**
     * Specialized interface for individual HMI elements, implementing low-level manipulation on their values
     *
     * @author Mateusz Bronk - Initial contribution
     */
    interface IArgoElement extends IArgoCommandableElement {

        /**
         * Returns the raw Argo command to be sent to the device (if update is pending)
         *
         * @return Command to send to device (if update pending), or
         *         {@link org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus#NO_VALUE NO_VALUE}
         *         - otherwise
         */
        public String getDeviceApiValue();

        /**
         * Handles channel command
         *
         * @param command The command to handle
         * @param isConfirmable Whether the command result is confirmable by the device
         * @return True - if command has been handled (= accepted by the framework and ready to be sent to device),
         *         False -
         *         otherwise
         */
        public boolean handleCommand(Command command, boolean isConfirmable);

        /**
         * Returns true if the value is always sent to the device on next communication cycle (regardless of whether
         * this
         * value has new updates or received a direct command).
         * Example: current time
         * <p>
         * Note items marked as always-sent do NOT count towards pending updates (unless they had received a direct
         * command). Ex. the always-sent comment will be sent together with any other "direct" commands, but won't
         * trigger
         * an update cycle on its own, and rather be appended to the user-triggered values on each update (for example,
         * time
         * update is NOT sent to the device each minute, but gets synchronized on every command)
         *
         * @return True if the value is always sent in an update cycle
         */
        public boolean isAlwaysSent();

        /**
         * Return **current** state of the element (including side-effects of any pending commands)
         *
         * @return Device's state as {@link State}
         */
        public State toState();

        /**
         * Updates this API element's state from device's response
         *
         * @param responseValue Raw API input
         * @return State after update
         */
        public State updateFromApiResponse(String responseValue);
    }

    /**
     * Notify that the withstanding command has just been sent to the device (and is now pending device-side
     * confirmation - if confirmable)
     *
     * @implNote Used for write-only params, to indicate they have been (hopefully) correctly sent to the device
     */
    public void notifyCommandSent();

    /**
     * Abort pending command targeting this knob (do not send it anymore, consider current device-side state as stable)
     */
    public void abortPendingCommand();

    /**
     * Checks if there's any command in flight (pending to be sent to the device, or sent and awaiting confirmation - if
     * confirmable)
     * <p>
     * This method is similar to {@link #isUpdatePending()}, but doesn't consider device's current state, only the
     * existence of non-finalized command
     *
     * @return True if command pending, False otherwise
     */
    public boolean hasInFlightCommand();

    /**
     * Checks if there's any update withstanding to be sent to the device (pending = not yet sent or not confirmed by
     * the device yet)
     * <p>
     * This method is similar to {@link #hasInFlightCommand()}, but also considers device's current state (if the device
     * reports the desired/commanded state already, it's considered not to have any update pending)
     *
     * @return True if update pending, False otherwise
     */
    public boolean isUpdatePending();

    /**
     * Return string representation of the current state of the device in a human-friendly format (for logging)
     * Returns mostly a protocol-like value, not necessarily the framework-converted one
     *
     * @return String representation of the element
     */
    @Override
    public String toString();
}
