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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SoundValue;

import com.google.gson.annotations.SerializedName;

/**
 * Base DTO for the state of a chime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SoundBase {
    protected @Nullable @SerializedName("sound_values") List<String> soundValues;
    protected @Nullable String sound;

    public @Nullable SoundValue getSoundValue() {
        return sound instanceof String sound ? SoundValue.of(sound) : null;
    }

    public List<SoundValue> getSoundValues() {
        return soundValues instanceof List<String> list ? list.stream().map(SoundValue::of).toList() : List.of();
    }
}
