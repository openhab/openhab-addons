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
package org.openhab.binding.volvooncall.internal.dto;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.UNDEFINED;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Status} is responsible for storing
 * Status information returned by vehicle status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Status extends VocAnswer {
    public enum FluidLevel {
        @SerializedName("Normal")
        NORMAL,
        @SerializedName("Low")
        LOW,
        @SerializedName("VeryLow")
        VERY_LOW,
        UNKNOWN;
    }

    public double averageFuelConsumption = UNDEFINED;
    public int averageSpeed = UNDEFINED;
    public int fuelAmount = UNDEFINED;
    public int fuelAmountLevel = UNDEFINED;
    public int distanceToEmpty = UNDEFINED;
    public int odometer = UNDEFINED;
    public int tripMeter1 = UNDEFINED;
    public int tripMeter2 = UNDEFINED;

    private @Nullable OnOffType carLocked;
    private @Nullable OnOffType engineRunning;
    @SerializedName("brakeFluid")
    public FluidLevel brakeFluidLevel = FluidLevel.UNKNOWN;
    public FluidLevel washerFluidLevel = FluidLevel.UNKNOWN;
    private @Nullable WindowsStatus windows;
    private @Nullable DoorsStatus doors;
    private @Nullable TyrePressure tyrePressure;
    private @Nullable HvBattery hvBattery;
    private @Nullable Heater heater;
    public String serviceWarningStatus = "";
    private @NonNullByDefault({}) List<Object> bulbFailures;

    public Optional<WindowsStatus> getWindows() {
        return Optional.ofNullable(windows);
    }

    public Optional<DoorsStatus> getDoors() {
        return Optional.ofNullable(doors);
    }

    public Optional<TyrePressure> getTyrePressure() {
        return Optional.ofNullable(tyrePressure);
    }

    public Optional<HvBattery> getHvBattery() {
        return Optional.ofNullable(hvBattery);
    }

    public Optional<Heater> getHeater() {
        return Optional.ofNullable(heater);
    }

    public Optional<OnOffType> getCarLocked() {
        return Optional.ofNullable(carLocked);
    }

    public Optional<OnOffType> getEngineRunning() {
        return Optional.ofNullable(engineRunning);
    }

    public boolean aFailedBulb() {
        return !bulbFailures.isEmpty();
    }

    /*
     * Currently not used in the binding, maybe interesting for the future
     *
     * @SerializedName("ERS")
     * private ERSStatus ers;
     * private ZonedDateTime averageFuelConsumptionTimestamp;
     * private ZonedDateTime averageSpeedTimestamp;
     * private ZonedDateTime brakeFluidTimestamp;
     * private ZonedDateTime bulbFailuresTimestamp;
     * private ZonedDateTime carLockedTimestamp;
     * private ZonedDateTime distanceToEmptyTimestamp;
     * private ZonedDateTime engineRunningTimestamp;
     * private ZonedDateTime fuelAmountLevelTimestamp;
     * private ZonedDateTime fuelAmountTimestamp;
     * private ZonedDateTime odometerTimestamp;
     * private Boolean privacyPolicyEnabled;
     * private ZonedDateTime privacyPolicyEnabledTimestamp;
     * private String remoteClimatizationStatus;
     * private ZonedDateTime remoteClimatizationStatusTimestamp;
     * private ZonedDateTime serviceWarningStatusTimestamp;
     * private Object theftAlarm;
     * private String timeFullyAccessibleUntil;
     * private String timePartiallyAccessibleUntil;
     * private ZonedDateTime tripMeter1Timestamp;
     * private ZonedDateTime tripMeter2Timestamp;
     * private ZonedDateTime washerFluidLevelTimestamp;
     */
}
