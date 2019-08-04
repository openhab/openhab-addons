/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
public class Status {
    public static final int UNDEFINED = -1;

    public double averageFuelConsumption = UNDEFINED;
    public @NonNullByDefault({}) OnOffType carLocked;
    public int distanceToEmpty = UNDEFINED;
    public @NonNullByDefault({}) OnOffType engineRunning;
    public int fuelAmount = UNDEFINED;
    public int fuelAmountLevel = UNDEFINED;
    public int odometer = UNDEFINED;
    public @NonNullByDefault({}) String serviceWarningStatus;
    public int tripMeter1 = UNDEFINED;
    public int tripMeter2 = UNDEFINED;
    public @NonNullByDefault({}) String washerFluidLevel;
    public @Nullable WindowsStatus windows;
    public @Nullable DoorsStatus doors;

    /*
     * Currently not used in the binding, maybe interesting for the future
     *
     * @SerializedName("ERS")
     * private ERSStatus ers;
     * private ZonedDateTime averageFuelConsumptionTimestamp;
     * private Integer averageSpeed;
     * private ZonedDateTime averageSpeedTimestamp;
     * private String brakeFluid;
     * private ZonedDateTime brakeFluidTimestamp;
     * private List<String> bulbFailures = null;
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
     * private TyrePressure tyrePressure;
     * private ZonedDateTime washerFluidLevelTimestamp;
     */
}
