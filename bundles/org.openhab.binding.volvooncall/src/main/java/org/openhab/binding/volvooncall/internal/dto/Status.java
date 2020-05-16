/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * The {@link Status} is responsible for storing
 * Door Status informations returned by vehicule status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Status extends VocAnswer {
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
    public String brakeFluid = "";
    public String washerFluidLevel = "";
    private @Nullable WindowsStatus windows;
    private @Nullable DoorsStatus doors;
    private @Nullable TyrePressure tyrePressure;
    private @Nullable HvBattery hvBattery;
    private @Nullable Heater heater;
    public String serviceWarningStatus = "";
    private @NonNullByDefault({}) List<Object> bulbFailures;

    public Optional<WindowsStatus> getWindows() {
        WindowsStatus windows = this.windows;
        if (windows != null) {
            return Optional.of(windows);
        }
        return Optional.empty();
    }

    public Optional<DoorsStatus> getDoors() {
        DoorsStatus doors = this.doors;
        if (doors != null) {
            return Optional.of(doors);
        }
        return Optional.empty();
    }

    public Optional<TyrePressure> getTyrePressure() {
        TyrePressure tyrePressure = this.tyrePressure;
        if (tyrePressure != null) {
            return Optional.of(tyrePressure);
        }
        return Optional.empty();
    }

    public Optional<HvBattery> getHvBattery() {
        HvBattery hvBattery = this.hvBattery;
        if (hvBattery != null) {
            return Optional.of(hvBattery);
        }
        return Optional.empty();
    }

    public Optional<Heater> getHeater() {
        Heater heater = this.heater;
        if (heater != null) {
            return Optional.of(heater);
        }
        return Optional.empty();
    }

    public Optional<OnOffType> getCarLocked() {
        OnOffType carLocked = this.carLocked;
        if (carLocked != null) {
            return Optional.of(carLocked);
        }
        return Optional.empty();
    }

    public Optional<OnOffType> getEngineRunning() {
        OnOffType engineRunning = this.engineRunning;
        if (engineRunning != null) {
            return Optional.of(engineRunning);
        }
        return Optional.empty();
    }

    public boolean aFailedBulb() {
        return bulbFailures.size() > 0;
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
