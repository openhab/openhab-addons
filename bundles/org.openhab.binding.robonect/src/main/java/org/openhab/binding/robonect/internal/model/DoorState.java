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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Actual or pending door position returned by the Robonect API. */
@NonNullByDefault
public class DoorState {
    private @Nullable Boolean open;
    private @Nullable Boolean close;

    /**
     * Reports whether the open state is active.
     *
     * @return whether the open state is active, or {@code null} if absent
     */
    public @Nullable Boolean getOpen() {
        return open;
    }

    /**
     * Reports whether the closed state is active.
     *
     * @return whether the closed state is active, or {@code null} if absent
     */
    public @Nullable Boolean getClose() {
        return close;
    }
}
