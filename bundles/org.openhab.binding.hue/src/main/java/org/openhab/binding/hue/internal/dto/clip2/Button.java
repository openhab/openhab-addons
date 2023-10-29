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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.ButtonEventType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 button state.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Button {
    private @Nullable @SerializedName("last_event") String lastEvent;
    private @Nullable @SerializedName("button_report") ButtonReport buttonReport;
    private @SerializedName("repeat_interval") int repeatInterval;

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Moved to button_report/event
     *
     * @return the last button event as an enum (null if none or invalid).
     */
    public @Nullable ButtonEventType getLastEvent() {
        String lastEvent = this.lastEvent;
        if (lastEvent == null) {
            return null;
        }

        try {
            return ButtonEventType.valueOf(lastEvent.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public @Nullable ButtonReport getButtonReport() {
        return buttonReport;
    }
}
