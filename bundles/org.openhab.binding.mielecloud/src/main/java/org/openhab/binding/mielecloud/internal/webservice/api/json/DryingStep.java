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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Immutable POJO representing the current drying step, queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DryingStep {
    @SerializedName("value_raw")
    @Nullable
    private Integer valueRaw;
    @SerializedName("value_localized")
    @Nullable
    private String valueLocalized;
    @SerializedName("key_localized")
    @Nullable
    private String keyLocalized;

    public Optional<Integer> getValueRaw() {
        return Optional.ofNullable(valueRaw);
    }

    public Optional<String> getValueLocalized() {
        return Optional.ofNullable(valueLocalized);
    }

    public Optional<String> getKeyLocalized() {
        return Optional.ofNullable(keyLocalized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyLocalized, valueLocalized, valueRaw);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DryingStep other = (DryingStep) obj;
        return Objects.equals(keyLocalized, other.keyLocalized) && Objects.equals(valueLocalized, other.valueLocalized)
                && Objects.equals(valueRaw, other.valueRaw);
    }

    @Override
    public String toString() {
        return "DryingStep [valueRaw=" + valueRaw + ", valueLocalized=" + valueLocalized + ", keyLocalized="
                + keyLocalized + "]";
    }
}
