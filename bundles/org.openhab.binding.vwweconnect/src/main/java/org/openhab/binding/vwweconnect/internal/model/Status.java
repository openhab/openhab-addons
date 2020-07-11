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
package org.openhab.binding.vwweconnect.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;

import com.google.gson.annotations.SerializedName;

/**
 * The Vehicle status representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class Status {

    private @Nullable String errorCode;
    private VehicleStatusData vehicleStatusData = new VehicleStatusData();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public VehicleStatusData getVehicleStatusData() {
        return vehicleStatusData;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode).append("vehicleStatusData", vehicleStatusData)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(vehicleStatusData).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Status)) {
            return false;
        }
        Status rhs = ((Status) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode).append(vehicleStatusData, rhs.vehicleStatusData)
                .isEquals();
    }

    public class VehicleStatusData {

        private boolean windowStatusSupported;
        private CarRenderData carRenderData = new CarRenderData();
        private LockData lockData = new LockData();
        private Object headerData = new Object();
        private @Nullable String requestStatus;
        private boolean lockDisabled;
        private boolean unlockDisabled;
        private boolean rluDisabled;
        private boolean hideCngFuelLevel;
        private boolean adBlueEnabled;
        private @Nullable String adBlueLevel;
        private boolean showAdBlueNotification;
        private boolean rluMibDeactivated;
        private double totalRange = BaseVehicle.UNDEFINED;
        private double primaryEngineRange = BaseVehicle.UNDEFINED;
        private double fuelRange = BaseVehicle.UNDEFINED;
        private double cngRange = BaseVehicle.UNDEFINED;
        private double batteryRange = BaseVehicle.UNDEFINED;
        private int fuelLevel = BaseVehicle.UNDEFINED;
        private int cngFuelLevel = BaseVehicle.UNDEFINED;
        private int batteryLevel = BaseVehicle.UNDEFINED;
        private @Nullable String sliceRootPath;

        public boolean getWindowStatusSupported() {
            return windowStatusSupported;
        }

        public CarRenderData getCarRenderData() {
            return carRenderData;
        }

        public LockData getLockData() {
            return lockData;
        }

        public Object getHeaderData() {
            return headerData;
        }

        public @Nullable String getRequestStatus() {
            return requestStatus;
        }

        public boolean getLockDisabled() {
            return lockDisabled;
        }

        public boolean getUnlockDisabled() {
            return unlockDisabled;
        }

        public boolean getRluDisabled() {
            return rluDisabled;
        }

        public boolean getHideCngFuelLevel() {
            return hideCngFuelLevel;
        }

        public boolean getAdBlueEnabled() {
            return adBlueEnabled;
        }

        public @Nullable String getAdBlueLevel() {
            return adBlueLevel;
        }

        public boolean getShowAdBlueNotification() {
            return showAdBlueNotification;
        }

        public boolean getRluMibDeactivated() {
            return rluMibDeactivated;
        }

        public double getTotalRange() {
            return totalRange;
        }

        public double getPrimaryEngineRange() {
            return primaryEngineRange;
        }

        public double getFuelRange() {
            return fuelRange;
        }

        public double getCngRange() {
            return cngRange;
        }

        public double getBatteryRange() {
            return batteryRange;
        }

        public int getFuelLevel() {
            return fuelLevel;
        }

        public int getCngFuelLevel() {
            return cngFuelLevel;
        }

        public int getBatteryLevel() {
            return batteryLevel;
        }

        public @Nullable String getSliceRootPath() {
            return sliceRootPath;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("windowStatusSupported", windowStatusSupported)
                    .append("carRenderData", carRenderData).append("lockData", lockData)
                    .append("headerData", headerData).append("requestStatus", requestStatus)
                    .append("lockDisabled", lockDisabled).append("unlockDisabled", unlockDisabled)
                    .append("rluDisabled", rluDisabled).append("hideCngFuelLevel", hideCngFuelLevel)
                    .append("adBlueEnabled", adBlueEnabled).append("adBlueLevel", adBlueLevel)
                    .append("showAdBlueNotification", showAdBlueNotification)
                    .append("rluMibDeactivated", rluMibDeactivated).append("totalRange", totalRange)
                    .append("primaryEngineRange", primaryEngineRange).append("fuelRange", fuelRange)
                    .append("cngRange", cngRange).append("batteryRange", batteryRange).append("fuelLevel", fuelLevel)
                    .append("cngFuelLevel", cngFuelLevel).append("batteryLevel", batteryLevel)
                    .append("sliceRootPath", sliceRootPath).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(headerData).append(fuelLevel).append(adBlueEnabled)
                    .append(unlockDisabled).append(adBlueLevel).append(batteryRange).append(rluDisabled)
                    .append(totalRange).append(cngFuelLevel).append(sliceRootPath).append(lockData)
                    .append(hideCngFuelLevel).append(rluMibDeactivated).append(cngRange).append(fuelRange)
                    .append(primaryEngineRange).append(lockDisabled).append(windowStatusSupported).append(carRenderData)
                    .append(requestStatus).append(showAdBlueNotification).append(batteryLevel).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof VehicleStatusData)) {
                return false;
            }
            VehicleStatusData rhs = ((VehicleStatusData) other);
            return new EqualsBuilder().append(headerData, rhs.headerData).append(fuelLevel, rhs.fuelLevel)
                    .append(adBlueEnabled, rhs.adBlueEnabled).append(unlockDisabled, rhs.unlockDisabled)
                    .append(adBlueLevel, rhs.adBlueLevel).append(batteryRange, rhs.batteryRange)
                    .append(rluDisabled, rhs.rluDisabled).append(totalRange, rhs.totalRange)
                    .append(cngFuelLevel, rhs.cngFuelLevel).append(sliceRootPath, rhs.sliceRootPath)
                    .append(lockData, rhs.lockData).append(hideCngFuelLevel, rhs.hideCngFuelLevel)
                    .append(rluMibDeactivated, rhs.rluMibDeactivated).append(cngRange, rhs.cngRange)
                    .append(fuelRange, rhs.fuelRange).append(primaryEngineRange, rhs.primaryEngineRange)
                    .append(lockDisabled, rhs.lockDisabled).append(windowStatusSupported, rhs.windowStatusSupported)
                    .append(carRenderData, rhs.carRenderData).append(requestStatus, rhs.requestStatus)
                    .append(showAdBlueNotification, rhs.showAdBlueNotification).append(batteryLevel, rhs.batteryLevel)
                    .isEquals();
        }
    }

    public class CarRenderData {

        private int parkingLights;
        private int hood;
        private Doors doors = new Doors();
        private Windows windows = new Windows();
        private int sunroof;
        private int roof;

        public OpenClosedType getDoorStatus(int status) {
            return status == 2 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }

        public int getParkingLights() {
            return parkingLights;
        }

        public OpenClosedType getHood() {
            return getDoorStatus(hood);
        }

        public Doors getDoors() {
            return doors;
        }

        public Windows getWindows() {
            return windows;
        }

        public OpenClosedType getSunroof() {
            return getDoorStatus(sunroof);
        }

        public OpenClosedType getRoof() {
            return getDoorStatus(roof);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("parkingLights", parkingLights).append("hood", hood)
                    .append("doors", doors).append("windows", windows).append("sunroof", sunroof).append("roof", roof)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(doors).append(roof).append(sunroof).append(parkingLights).append(hood)
                    .append(windows).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof CarRenderData)) {
                return false;
            }
            CarRenderData rhs = ((CarRenderData) other);
            return new EqualsBuilder().append(doors, rhs.doors).append(roof, rhs.roof).append(sunroof, rhs.sunroof)
                    .append(parkingLights, rhs.parkingLights).append(hood, rhs.hood).append(windows, rhs.windows)
                    .isEquals();
        }
    }

    public class LockData {

        @SerializedName("left_front")
        private int leftFront;
        @SerializedName("right_front")
        private int rightFront;
        @SerializedName("left_back")
        private int leftBack;
        @SerializedName("right_back")
        private int rightBack;
        private int trunk;

        public OnOffType getDoorsLocked() {
            return leftFront == 2 ? OnOffType.ON : OnOffType.OFF;
        }

        public int getLeftFront() {
            return leftFront;
        }

        public int getRightFront() {
            return rightFront;
        }

        public int getLeftBack() {
            return leftBack;
        }

        public int getRightBack() {
            return rightBack;
        }

        public OnOffType getTrunk() {
            return trunk == 2 ? OnOffType.ON : OnOffType.OFF;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("leftFront", leftFront).append("rightFront", rightFront)
                    .append("leftBack", leftBack).append("rightBack", rightBack).append("trunk", trunk).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(leftFront).append(rightBack).append(rightFront).append(leftBack)
                    .append(trunk).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof LockData)) {
                return false;
            }
            LockData rhs = ((LockData) other);
            return new EqualsBuilder().append(leftFront, rhs.leftFront).append(rightBack, rhs.rightBack)
                    .append(rightFront, rhs.rightFront).append(leftBack, rhs.leftBack).append(trunk, rhs.trunk)
                    .isEquals();
        }

    }

    public class Doors {

        @SerializedName("left_front")
        private int leftFront;
        @SerializedName("right_front")
        private int rightFront;
        @SerializedName("left_back")
        private int leftBack;
        @SerializedName("right_back")
        private int rightBack;
        private int trunk;
        @SerializedName("number_of_doors")
        private int numberOfDoors;

        public OpenClosedType getDoorStatus(int status) {
            return status == 2 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }

        public OpenClosedType getLeftFront() {
            return getDoorStatus(leftFront);
        }

        public OpenClosedType getRightFront() {
            return getDoorStatus(rightFront);
        }

        public OpenClosedType getLeftBack() {
            return getDoorStatus(leftBack);
        }

        public OpenClosedType getRightBack() {
            return getDoorStatus(rightBack);
        }

        public OpenClosedType getTrunk() {
            return getDoorStatus(trunk);
        }

        public int getNumberOfDoors() {
            return numberOfDoors;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("leftFront", leftFront).append("rightFront", rightFront)
                    .append("leftBack", leftBack).append("rightBack", rightBack).append("trunk", trunk)
                    .append("numberOfDoors", numberOfDoors).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(leftFront).append(rightBack).append(rightFront).append(numberOfDoors)
                    .append(leftBack).append(trunk).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Doors)) {
                return false;
            }
            Doors rhs = ((Doors) other);
            return new EqualsBuilder().append(leftFront, rhs.leftFront).append(rightBack, rhs.rightBack)
                    .append(rightFront, rhs.rightFront).append(numberOfDoors, rhs.numberOfDoors)
                    .append(leftBack, rhs.leftBack).append(trunk, rhs.trunk).isEquals();
        }
    }

    public class Windows {

        @SerializedName("left_front")
        private int leftFront;
        @SerializedName("right_front")
        private int rightFront;
        @SerializedName("left_back")
        private int leftBack;
        @SerializedName("right_back")
        private int rightBack;

        public OpenClosedType getWindowStatus(int status) {
            return status == 2 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }

        public OpenClosedType getLeftFront() {
            return getWindowStatus(leftFront);
        }

        public OpenClosedType getRightFront() {
            return getWindowStatus(rightFront);
        }

        public OpenClosedType getLeftBack() {
            return getWindowStatus(leftBack);
        }

        public OpenClosedType getRightBack() {
            return getWindowStatus(rightBack);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("leftFront", leftFront).append("rightFront", rightFront)
                    .append("leftBack", leftBack).append("rightBack", rightBack).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(leftFront).append(leftBack).append(rightBack).append(rightFront)
                    .toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Windows)) {
                return false;
            }
            Windows rhs = ((Windows) other);
            return new EqualsBuilder().append(leftFront, rhs.leftFront).append(leftBack, rhs.leftBack)
                    .append(rightBack, rhs.rightBack).append(rightFront, rhs.rightFront).isEquals();
        }
    }
}
