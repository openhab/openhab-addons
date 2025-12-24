/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SoundType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for 'sound status' of a chime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SoundStatus {
    private @Nullable String sound;
    private @Nullable @SerializedName("sound_values") List<String> soundValues;

    public @Nullable SoundType getSound() {
        String sound = this.sound;
        return Objects.nonNull(sound) ? SoundType.of(sound) : null;
    }

    public List<SoundType> getSoundValues() {
        List<String> soundValues = this.soundValues;
        if (Objects.nonNull(soundValues)) {
            return soundValues.stream().map(SoundType::of).toList();
        }
        return List.of();
    }

    public SoundStatus setSound(SoundType soundType) {
        this.sound = soundType.name().toLowerCase();
        return this;
    }
}
