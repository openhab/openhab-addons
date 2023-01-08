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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.EffectType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for 'effect' of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Effects {
    /**
     * Following fields are @Nullable since different cases use different subsets of the fields.
     */
    private @Nullable @SerializedName("effect_values") List<String> effectValues;
    private @Nullable String effect;
    private @Nullable @SerializedName("status_values") List<String> statusValues;
    private @Nullable String status;
    private @Nullable Integer duration;

    public List<EffectType> getEffectValues() {
        List<String> effectValues = this.effectValues;
        if (Objects.nonNull(effectValues)) {
            return effectValues.stream().map(EffectType::of).collect(Collectors.toList());
        }
        return List.of();
    }

    public List<EffectType> getStatusEffectValues() {
        List<String> statusValues = this.statusValues;
        if (Objects.nonNull(statusValues)) {
            return statusValues.stream().map(EffectType::of).collect(Collectors.toList());
        }
        return List.of();
    }

    public @Nullable EffectType getEffectType() {
        return Objects.nonNull(effect) ? EffectType.of(effect) : null;
    }

    public void setEffectType(EffectType effect) {
        effectValues = null;
        this.effect = effect.name().toLowerCase();
    }

    public @Nullable EffectType getStatusEffectType() {
        return Objects.nonNull(status) ? EffectType.of(status) : null;
    }

    public void setStatusEffectType(EffectType status) {
        statusValues = null;
        this.status = status.name().toLowerCase();
    }

    public @Nullable Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
