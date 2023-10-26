/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.RotationEventType;

/**
 * DTO for CLIP 2 relative rotary report.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class RotaryReport {
    private @NonNullByDefault({}) Instant updated;
    private @Nullable String action;
    private @Nullable Rotation rotation;

    /**
     * @return last time the value of this property is changed.
     */
    public Instant getLastChanged() {
        return updated;
    }

    /**
     * @return which type of rotary event is received
     */
    public @Nullable RotationEventType getAction() {
        String action = this.action;
        return action == null ? null : RotationEventType.valueOf(action.toUpperCase());
    }

    public @Nullable Rotation getRotation() {
        return rotation;
    }
}
