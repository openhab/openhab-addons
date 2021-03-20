/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;

import com.google.gson.annotations.SerializedName;

/**
 * The Vehicle status representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class StatusDTO {

    private String errorCode = "";
    private VehicleStatusDataDTO vehicleStatusData = new VehicleStatusDataDTO();

    public String getErrorCode() {
        return errorCode;
    }

    public VehicleStatusDataDTO getVehicleStatusData() {
        return vehicleStatusData;
    }

    @Override
    public String toString() {
        return "StatusDTO [errorCode=" + errorCode + ", vehicleStatusData=" + vehicleStatusData + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode.hashCode();
        result = prime * result + vehicleStatusData.hashCode();
        return result;
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
        StatusDTO other = (StatusDTO) obj;
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!vehicleStatusData.equals(other.vehicleStatusData)) {
            return false;
        }
        return true;
    }

    public class VehicleStatusDataDTO {

        private boolean windowStatusSupported;
        private CarRenderDataDTO carRenderData = new CarRenderDataDTO();
        private LockDataDTO lockData = new LockDataDTO();
        private @Nullable Object headerData = new Object();
        private @Nullable String requestStatus = "";
        private boolean lockDisabled;
        private boolean unlockDisabled;
        private boolean rluDisabled;
        private boolean hideCngFuelLevel;
        private boolean adBlueEnabled;
        private @Nullable String adBlueLevel = "";
        private boolean showAdBlueNotification;
        private boolean rluMibDeactivated;
        private double totalRange = BaseVehicleDTO.UNDEFINED;
        private double primaryEngineRange = BaseVehicleDTO.UNDEFINED;
        private double fuelRange = BaseVehicleDTO.UNDEFINED;
        private double cngRange = BaseVehicleDTO.UNDEFINED;
        private double batteryRange = BaseVehicleDTO.UNDEFINED;
        private int fuelLevel = BaseVehicleDTO.UNDEFINED;
        private int cngFuelLevel = BaseVehicleDTO.UNDEFINED;
        private int batteryLevel = BaseVehicleDTO.UNDEFINED;
        private String sliceRootPath = "";

        public boolean getWindowStatusSupported() {
            return windowStatusSupported;
        }

        public CarRenderDataDTO getCarRenderData() {
            return carRenderData;
        }

        public LockDataDTO getLockData() {
            return lockData;
        }

        public @Nullable Object getHeaderData() {
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

        public String getSliceRootPath() {
            return sliceRootPath;
        }

        @Override
        public String toString() {
            return "VehicleStatusDataDTO [windowStatusSupported=" + windowStatusSupported + ", carRenderData="
                    + carRenderData + ", lockData=" + lockData + ", headerData=" + headerData + ", requestStatus="
                    + requestStatus + ", lockDisabled=" + lockDisabled + ", unlockDisabled=" + unlockDisabled
                    + ", rluDisabled=" + rluDisabled + ", hideCngFuelLevel=" + hideCngFuelLevel + ", adBlueEnabled="
                    + adBlueEnabled + ", adBlueLevel=" + adBlueLevel + ", showAdBlueNotification="
                    + showAdBlueNotification + ", rluMibDeactivated=" + rluMibDeactivated + ", totalRange=" + totalRange
                    + ", primaryEngineRange=" + primaryEngineRange + ", fuelRange=" + fuelRange + ", cngRange="
                    + cngRange + ", batteryRange=" + batteryRange + ", fuelLevel=" + fuelLevel + ", cngFuelLevel="
                    + cngFuelLevel + ", batteryLevel=" + batteryLevel + ", sliceRootPath=" + sliceRootPath + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (adBlueEnabled ? 1231 : 1237);
            String adBlueLevel2 = adBlueLevel;
            result = prime * result + ((adBlueLevel2 == null) ? 0 : adBlueLevel2.hashCode());
            result = prime * result + batteryLevel;
            long temp;
            temp = Double.doubleToLongBits(batteryRange);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + carRenderData.hashCode();
            result = prime * result + cngFuelLevel;
            temp = Double.doubleToLongBits(cngRange);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + fuelLevel;
            temp = Double.doubleToLongBits(fuelRange);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            Object headerData2 = headerData;
            result = prime * result + ((headerData2 == null) ? 0 : headerData2.hashCode());
            result = prime * result + (hideCngFuelLevel ? 1231 : 1237);
            result = prime * result + lockData.hashCode();
            result = prime * result + (lockDisabled ? 1231 : 1237);
            temp = Double.doubleToLongBits(primaryEngineRange);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            String requestStatus2 = requestStatus;
            result = prime * result + ((requestStatus2 == null) ? 0 : requestStatus2.hashCode());
            result = prime * result + (rluDisabled ? 1231 : 1237);
            result = prime * result + (rluMibDeactivated ? 1231 : 1237);
            result = prime * result + (showAdBlueNotification ? 1231 : 1237);
            result = prime * result + sliceRootPath.hashCode();
            temp = Double.doubleToLongBits(totalRange);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + (unlockDisabled ? 1231 : 1237);
            result = prime * result + (windowStatusSupported ? 1231 : 1237);
            return result;
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
            VehicleStatusDataDTO other = (VehicleStatusDataDTO) obj;
            if (adBlueEnabled != other.adBlueEnabled) {
                return false;
            }
            if (adBlueLevel != null && !adBlueLevel.equals(other.adBlueLevel)) {
                return false;
            }
            if (batteryLevel != other.batteryLevel) {
                return false;
            }
            if (Double.doubleToLongBits(batteryRange) != Double.doubleToLongBits(other.batteryRange)) {
                return false;
            }
            if (!carRenderData.equals(other.carRenderData)) {
                return false;
            }
            if (cngFuelLevel != other.cngFuelLevel) {
                return false;
            }
            if (Double.doubleToLongBits(cngRange) != Double.doubleToLongBits(other.cngRange)) {
                return false;
            }
            if (fuelLevel != other.fuelLevel) {
                return false;
            }
            if (Double.doubleToLongBits(fuelRange) != Double.doubleToLongBits(other.fuelRange)) {
                return false;
            }
            if (headerData != null && !headerData.equals(other.headerData)) {
                return false;
            }
            if (hideCngFuelLevel != other.hideCngFuelLevel) {
                return false;
            }
            if (!lockData.equals(other.lockData)) {
                return false;
            }
            if (lockDisabled != other.lockDisabled) {
                return false;
            }
            if (Double.doubleToLongBits(primaryEngineRange) != Double.doubleToLongBits(other.primaryEngineRange)) {
                return false;
            }
            if (requestStatus != null && !requestStatus.equals(other.requestStatus)) {
                return false;
            }
            if (rluDisabled != other.rluDisabled) {
                return false;
            }
            if (rluMibDeactivated != other.rluMibDeactivated) {
                return false;
            }
            if (showAdBlueNotification != other.showAdBlueNotification) {
                return false;
            }
            if (!sliceRootPath.equals(other.sliceRootPath)) {
                return false;
            }
            if (Double.doubleToLongBits(totalRange) != Double.doubleToLongBits(other.totalRange)) {
                return false;
            }
            if (unlockDisabled != other.unlockDisabled) {
                return false;
            }
            if (windowStatusSupported != other.windowStatusSupported) {
                return false;
            }
            return true;
        }

        private StatusDTO getEnclosingInstance() {
            return StatusDTO.this;
        }
    }

    public class CarRenderDataDTO {

        private int parkingLights;
        private int hood;
        private DoorsDTO doors = new DoorsDTO();
        private WindowsDTO windows = new WindowsDTO();
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

        public DoorsDTO getDoors() {
            return doors;
        }

        public WindowsDTO getWindows() {
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
            return "CarRenderDataDTO [parkingLights=" + parkingLights + ", hood=" + hood + ", doors=" + doors
                    + ", windows=" + windows + ", sunroof=" + sunroof + ", roof=" + roof + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + doors.hashCode();
            result = prime * result + hood;
            result = prime * result + parkingLights;
            result = prime * result + roof;
            result = prime * result + sunroof;
            result = prime * result + windows.hashCode();
            return result;
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
            CarRenderDataDTO other = (CarRenderDataDTO) obj;
            if (!doors.equals(other.doors)) {
                return false;
            }
            if (hood != other.hood) {
                return false;
            }
            if (parkingLights != other.parkingLights) {
                return false;
            }
            if (roof != other.roof) {
                return false;
            }
            if (sunroof != other.sunroof) {
                return false;
            }
            if (!windows.equals(other.windows)) {
                return false;
            }
            return true;
        }

        private StatusDTO getEnclosingInstance() {
            return StatusDTO.this;
        }
    }

    public class LockDataDTO {

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
            return "LockDataDTO [leftFront=" + leftFront + ", rightFront=" + rightFront + ", leftBack=" + leftBack
                    + ", rightBack=" + rightBack + ", trunk=" + trunk + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + leftBack;
            result = prime * result + leftFront;
            result = prime * result + rightBack;
            result = prime * result + rightFront;
            result = prime * result + trunk;
            return result;
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
            LockDataDTO other = (LockDataDTO) obj;
            if (leftBack != other.leftBack) {
                return false;
            }
            if (leftFront != other.leftFront) {
                return false;
            }
            if (rightBack != other.rightBack) {
                return false;
            }
            if (rightFront != other.rightFront) {
                return false;
            }
            if (trunk != other.trunk) {
                return false;
            }
            return true;
        }

        private StatusDTO getEnclosingInstance() {
            return StatusDTO.this;
        }
    }

    public class DoorsDTO {

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
            return "DoorsDTO [leftFront=" + leftFront + ", rightFront=" + rightFront + ", leftBack=" + leftBack
                    + ", rightBack=" + rightBack + ", trunk=" + trunk + ", numberOfDoors=" + numberOfDoors + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + leftBack;
            result = prime * result + leftFront;
            result = prime * result + numberOfDoors;
            result = prime * result + rightBack;
            result = prime * result + rightFront;
            result = prime * result + trunk;
            return result;
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
            DoorsDTO other = (DoorsDTO) obj;
            if (leftBack != other.leftBack) {
                return false;
            }
            if (leftFront != other.leftFront) {
                return false;
            }
            if (numberOfDoors != other.numberOfDoors) {
                return false;
            }
            if (rightBack != other.rightBack) {
                return false;
            }
            if (rightFront != other.rightFront) {
                return false;
            }
            if (trunk != other.trunk) {
                return false;
            }
            return true;
        }

        private StatusDTO getEnclosingInstance() {
            return StatusDTO.this;
        }
    }

    public class WindowsDTO {

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
            return "WindowsDTO [leftFront=" + leftFront + ", rightFront=" + rightFront + ", leftBack=" + leftBack
                    + ", rightBack=" + rightBack + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + leftBack;
            result = prime * result + leftFront;
            result = prime * result + rightBack;
            result = prime * result + rightFront;
            return result;
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
            WindowsDTO other = (WindowsDTO) obj;
            if (leftBack != other.leftBack) {
                return false;
            }
            if (leftFront != other.leftFront) {
                return false;
            }
            if (rightBack != other.rightBack) {
                return false;
            }
            if (rightFront != other.rightFront) {
                return false;
            }
            return true;
        }

        private StatusDTO getEnclosingInstance() {
            return StatusDTO.this;
        }
    }
}
