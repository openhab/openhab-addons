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

/** Door-system state returned by the Robonect API. */
@NonNullByDefault
public class Door {
    private @Nullable Boolean enabled;
    private @Nullable DoorState state;
    private @Nullable DoorState pending;
    private @Nullable Boolean arrested;

    public @Nullable Boolean getEnabled() {
        return enabled;
    }

    public @Nullable DoorState getState() {
        return state;
    }

    public @Nullable DoorState getPending() {
        return pending;
    }

    public @Nullable Boolean getArrested() {
        return arrested;
    }
}
