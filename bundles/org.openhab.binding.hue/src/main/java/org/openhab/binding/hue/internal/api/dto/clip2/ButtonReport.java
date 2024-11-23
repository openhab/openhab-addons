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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ButtonEventType;

/**
 * DTO for CLIP 2 button report.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ButtonReport {
    private @NonNullByDefault({}) Instant updated;
    private @Nullable String event;

    /**
     * @return last time the value of this property is updated.
     */
    public Instant getLastChanged() {
        return updated;
    }

    /**
     * @return event which can be sent by a button control (null if none or invalid).
     */
    public @Nullable ButtonEventType getLastEvent() {
        String event = this.event;
        if (event == null) {
            return null;
        }

        try {
            return ButtonEventType.valueOf(event.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
