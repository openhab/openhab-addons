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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ActionDeltaType;
import org.openhab.core.library.types.IncreaseDecreaseType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for dimming delta of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DimmingDelta {
    private @Nullable String action;
    private @Nullable @SerializedName("brightness_delta") Double brightnessDelta;

    public @Nullable String getAction() {
        return action;
    }

    public @Nullable Double getBrightnessDelta() {
        return brightnessDelta;
    }

    public DimmingDelta setAction(IncreaseDecreaseType action) {
        this.action = ActionDeltaType.of(action).name().toLowerCase();
        return this;
    }

    public DimmingDelta setDelta(Double delta) {
        this.brightnessDelta = delta;
        return this;
    }
}
