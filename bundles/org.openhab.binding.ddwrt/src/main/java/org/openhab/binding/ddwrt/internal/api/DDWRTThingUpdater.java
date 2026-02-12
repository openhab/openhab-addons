/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;

/**
 * Callback interface for DD-WRT device implementations to update thing states.
 * 
 * This interface is implemented by thing handlers to receive state updates
 * from device implementations. It provides methods to update channel states
 * and report communication status changes.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public interface DDWRTThingUpdater {
    /**
     * Update a channel state for this thing.
     */
    void updateChannel(String channelId, State state);

    /**
     * Optional: allow the device to report communication status.
     */
    default void reportOffline(@Nullable String detail) {
        // no-op
    }

    default void reportOnline() {
        // no-op
    }
}
