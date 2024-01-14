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

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.EffectType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for 'effects' of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Effects {
    private @Nullable @SerializedName("effect_values") List<String> effectValues;
    private @Nullable String effect;
    private @Nullable @SerializedName("status_values") List<String> statusValues;
    private @Nullable String status;

    public boolean allows(EffectType effect) {
        List<String> statusValues = this.statusValues;
        return Objects.nonNull(statusValues) ? statusValues.contains(effect.name().toLowerCase()) : false;
    }

    public EffectType getEffect() {
        String effect = this.effect;
        return Objects.nonNull(effect) ? EffectType.of(effect) : EffectType.NO_EFFECT;
    }

    public EffectType getStatus() {
        return Objects.nonNull(status) ? EffectType.of(status) : EffectType.NO_EFFECT;
    }

    public List<String> getStatusValues() {
        List<String> statusValues = this.statusValues;
        return Objects.nonNull(statusValues) ? statusValues : List.of();
    }

    public Effects setEffect(EffectType effectType) {
        effect = effectType.name().toLowerCase();
        return this;
    }

    public Effects setStatusValues(List<String> statusValues) {
        this.statusValues = statusValues;
        return this;
    }
}
