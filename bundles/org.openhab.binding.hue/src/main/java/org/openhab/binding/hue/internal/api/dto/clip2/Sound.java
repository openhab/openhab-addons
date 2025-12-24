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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SoundType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for a chime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Sound {
    private @Nullable @SerializedName("sound_values") List<String> soundValues;
    private @Nullable SoundStatus status;
    private @Nullable String sound;

    public @Nullable SoundStatus getStatus() {
        return status;
    }

    public List<SoundType> getSoundTypes() {
        return soundValues instanceof List<String> list ? list.stream().map(SoundType::of).toList() : List.of();
    }

    public @Nullable SoundType getSoundType() {
        return status instanceof SoundStatus s ? s.getSoundType() : null;
    }

    public Sound setSoundType(SoundType soundType) {
        sound = soundType.name().toLowerCase();
        return this;
    }
}
