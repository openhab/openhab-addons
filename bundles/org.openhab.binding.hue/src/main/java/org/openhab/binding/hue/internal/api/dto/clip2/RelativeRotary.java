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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 tap switch rotary dial state.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class RelativeRotary {
    private @Nullable @SerializedName("last_event") RotationEvent lastEvent;
    private @Nullable @SerializedName("rotary_report") RotaryReport rotaryReport;

    public State getActionState() {
        RotationEvent lastEvent = getLastEvent();
        return Objects.nonNull(lastEvent) ? lastEvent.getActionState() : UnDefType.NULL;
    }

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Renamed to RelativeRotaryReport. Indicate which type of rotary event is received.
     * Should be used only as fallback for older firmwares.
     */
    public @Nullable RotationEvent getLastEvent() {
        return lastEvent;
    }

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Renamed to RelativeRotaryReport.
     * Should be used only as fallback for older firmwares.
     */
    public State getStepsState() {
        RotationEvent lastEvent = getLastEvent();
        return Objects.nonNull(lastEvent) ? lastEvent.getStepsState() : UnDefType.NULL;
    }

    public @Nullable RotaryReport getRotaryReport() {
        return rotaryReport;
    }
}
