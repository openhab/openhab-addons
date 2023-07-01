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

    public State getActionState() {
        RotationEvent lastEvent = getLastEvent();
        return Objects.nonNull(lastEvent) ? lastEvent.getActionState() : UnDefType.NULL;
    }

    public @Nullable RotationEvent getLastEvent() {
        return lastEvent;
    }

    public State getStepsState() {
        RotationEvent lastEvent = getLastEvent();
        return Objects.nonNull(lastEvent) ? lastEvent.getStepsState() : UnDefType.NULL;
    }
}
