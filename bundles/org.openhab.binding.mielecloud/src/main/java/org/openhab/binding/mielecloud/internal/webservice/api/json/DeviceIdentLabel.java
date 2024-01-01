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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing the full device identification queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceIdentLabel {
    @Nullable
    private String fabNumber;
    @Nullable
    private String fabIndex;
    @Nullable
    private String techType;
    @Nullable
    private String matNumber;
    @Nullable
    private final List<String> swids = null;

    public Optional<String> getFabNumber() {
        return Optional.ofNullable(fabNumber);
    }

    public Optional<String> getFabIndex() {
        return Optional.ofNullable(fabIndex);
    }

    public Optional<String> getTechType() {
        return Optional.ofNullable(techType);
    }

    public Optional<String> getMatNumber() {
        return Optional.ofNullable(matNumber);
    }

    public List<String> getSwids() {
        if (swids == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(swids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fabIndex, fabNumber, matNumber, swids, techType);
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
        DeviceIdentLabel other = (DeviceIdentLabel) obj;
        return Objects.equals(fabIndex, other.fabIndex) && Objects.equals(fabNumber, other.fabNumber)
                && Objects.equals(matNumber, other.matNumber) && Objects.equals(swids, other.swids)
                && Objects.equals(techType, other.techType);
    }

    @Override
    public String toString() {
        return "DeviceIdentLabel [fabNumber=" + fabNumber + ", fabIndex=" + fabIndex + ", techType=" + techType
                + ", matNumber=" + matNumber + ", swids=" + swids + "]";
    }
}
