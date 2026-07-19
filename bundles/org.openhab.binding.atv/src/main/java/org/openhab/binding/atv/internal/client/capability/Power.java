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
package org.openhab.binding.atv.internal.client.capability;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for retrieving and changing the power state of an Apple TV.
 *
 * <p>
 * Listener interface: {@link PowerListener}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Power {

    /**
     * Returns the device power state.
     *
     * @return current power state
     * @throws NotSupportedError if not supported
     */
    default PowerState powerState() {
        throw new NotSupportedError("powerState is not supported");
    }

    /**
     * Turns the device on.
     *
     * @param awaitNewState if {@code true}, the future completes first when the new state has been reached
     * @return future completing when command has been sent (or state reached)
     */
    default CompletableFuture<Void> turnOn(boolean awaitNewState) {
        return CompletableFuture.failedFuture(new NotSupportedError("turnOn is not supported"));
    }

    /**
     * Turns the device on without awaiting the new state.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> turnOn() {
        return turnOn(false);
    }

    /**
     * Turns the device off.
     *
     * @param awaitNewState if {@code true}, the future completes first when the new state has been reached
     * @return future completing when command has been sent (or state reached)
     */
    default CompletableFuture<Void> turnOff(boolean awaitNewState) {
        return CompletableFuture.failedFuture(new NotSupportedError("turnOff is not supported"));
    }

    /**
     * Turns the device off without awaiting the new state.
     *
     * @return future completing when command has been sent
     */
    default CompletableFuture<Void> turnOff() {
        return turnOff(false);
    }

    /**
     * Adds a listener receiving power state updates.
     *
     * @param listener listener to add
     */
    void addListener(PowerListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener listener to remove
     */
    void removeListener(PowerListener listener);
}
