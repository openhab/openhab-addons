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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * API for push/async updates from an Apple TV.
 *
 * <p>
 * A push updater shall only publish updates in case the state actually changes. Listener interface:
 * {@link PushListener}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface PushUpdater {

    /**
     * Returns if the push updater has been started.
     *
     * @return {@code true} if started
     */
    boolean active();

    /**
     * Begins listening to updates. If an error occurs, start must be called again.
     *
     * @param initialDelay delay before requesting the initial state
     */
    void start(Duration initialDelay);

    /**
     * Begins listening to updates without initial delay.
     */
    default void start() {
        start(Duration.ZERO);
    }

    /**
     * Stops forwarding updates to listeners.
     */
    void stop();

    /**
     * Adds a listener receiving push updates.
     *
     * @param listener listener to add
     */
    void addListener(PushListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener listener to remove
     */
    void removeListener(PushListener listener);
}
