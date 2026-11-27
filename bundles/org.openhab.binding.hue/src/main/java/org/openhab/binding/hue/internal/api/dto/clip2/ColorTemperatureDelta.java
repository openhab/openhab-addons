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
 * DTO for color temperature delta of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ColorTemperatureDelta {
    private @Nullable String action;
    private @SerializedName("mirek_delta") int mirekDelta;

    public @Nullable String getAction() {
        return action;
    }

    public int getMirekDelta() {
        return mirekDelta;
    }

    public ColorTemperatureDelta setAction(IncreaseDecreaseType action) {
        this.action = ActionDeltaType.of(action).name().toLowerCase();
        return this;
    }

    public ColorTemperatureDelta setDelta(int mirekDelta) {
        this.mirekDelta = mirekDelta;
        return this;
    }
}
