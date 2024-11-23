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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Immutable POJO representing the device actions queried from the Miele REST API.
 *
 * @author Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public class Actions {
    @SerializedName("processAction")
    @Nullable
    private final List<ProcessAction> processAction = null;
    @SerializedName("light")
    @Nullable
    private final List<Integer> light = null;
    @SerializedName("startTime")
    @Nullable
    private final List<List<Integer>> startTime = null;
    @SerializedName("programId")
    @Nullable
    private final List<Integer> programId = null;
    @SerializedName("deviceName")
    @Nullable
    private String deviceName;
    @SerializedName("powerOff")
    @Nullable
    private Boolean powerOff;
    @SerializedName("powerOn")
    @Nullable
    private Boolean powerOn;

    public List<ProcessAction> getProcessAction() {
        if (processAction == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(processAction);
    }

    public List<Light> getLight() {
        final List<Integer> lightRefCopy = light;
        if (lightRefCopy == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(lightRefCopy.stream().map(Light::fromId).collect(Collectors.toList()));
    }

    /**
     * Gets the start time encoded as {@link List} of {@link List} of {@link Integer} values.
     * The first list entry defines the lower time constraint for setting the delayed start time. The second list
     * entry defines the upper time constraint. The time constraints are defined as a list of integers with the full
     * hour as first and minutes as second element.
     *
     * @return The possible start time interval encoded as described above.
     */
    public Optional<List<List<Integer>>> getStartTime() {
        if (startTime == null) {
            return Optional.empty();
        }

        return Optional.of(Collections.unmodifiableList(startTime));
    }

    public List<Integer> getProgramId() {
        if (programId == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(programId);
    }

    public Optional<String> getDeviceName() {
        return Optional.ofNullable(deviceName);
    }

    public Optional<Boolean> getPowerOn() {
        return Optional.ofNullable(powerOn);
    }

    public Optional<Boolean> getPowerOff() {
        return Optional.ofNullable(powerOff);
    }

    @Override
    public String toString() {
        return "ActionState [processAction=" + processAction + ", light=" + light + ", startTime=" + startTime
                + ", programId=" + programId + ", deviceName=" + deviceName + ", powerOff=" + powerOff + ", powerOn="
                + powerOn + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName, light, powerOn, powerOff, processAction, startTime, programId);
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
        Actions other = (Actions) obj;
        return Objects.equals(deviceName, other.deviceName) && Objects.equals(light, other.light)
                && Objects.equals(powerOn, other.powerOn) && Objects.equals(powerOff, other.powerOff)
                && Objects.equals(processAction, other.processAction) && Objects.equals(startTime, other.startTime)
                && Objects.equals(programId, other.programId);
    }
}
