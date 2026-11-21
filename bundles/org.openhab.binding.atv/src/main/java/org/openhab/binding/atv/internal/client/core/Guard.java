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
package org.openhab.binding.atv.internal.client.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.BlockedStateError;

/**
 * Blocks access to the public interface of a closed device.
 *
 * <p>
 * A single {@code Guard} instance (one atomic flag) is shared by {@code AppleTVRelay} and all its relay
 * interface implementations. Every public relay method calls {@link #requireNotBlocked(String)} first; once
 * {@link #block()} has been called (when the device is closed), those methods throw {@link BlockedStateError}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Guard {

    private final AtomicBoolean blocked = new AtomicBoolean();

    /**
     * Changes the guard into blocking state. Irreversible.
     */
    public void block() {
        blocked.set(true);
    }

    /**
     * Returns if the guard is in blocking state.
     *
     * @return {@code true} if blocked
     */
    public boolean isBlocked() {
        return blocked.get();
    }

    /**
     * Throws when the guard is in blocking state.
     *
     * @param name name of the guarded method, used in the exception message
     * @throws BlockedStateError if the guard has been blocked
     */
    public void requireNotBlocked(String name) {
        if (blocked.get()) {
            throw new BlockedStateError(name + " is blocked");
        }
    }
}
