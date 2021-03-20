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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The Emanager representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class EManagerDTO {
    private String errorCode = "";
    @SerializedName("EManager")
    private EManagerModel eManager = new EManagerModel();

    public String getErrorCode() {
        return errorCode;
    }

    public EManagerModel getEManager() {
        return eManager;
    }

    @Override
    public String toString() {
        return "EManagerDTO [errorCode=" + errorCode + ", eManager=" + eManager + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + eManager.hashCode();
        result = prime * result + errorCode.hashCode();
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
        EManagerDTO other = (EManagerDTO) obj;
        if (!eManager.equals(other.eManager)) {
            return false;
        }
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        return true;
    }

    public class EManagerModel {
        private RbcDTO rbc = new RbcDTO();
        private RpcDTO rpc = new RpcDTO();
        private RdtDTO rdt = new RdtDTO();
        private boolean actionPending;
        private boolean rdtAvailable;

        public RbcDTO getRbc() {
            return rbc;
        }

        public RpcDTO getRpc() {
            return rpc;
        }

        public RdtDTO getRdt() {
            return rdt;
        }

        public boolean isActionPending() {
            return actionPending;
        }

        public boolean isRdtAvailable() {
            return rdtAvailable;
        }

        @Override
        public String toString() {
            return "EManagerModel [rbc=" + rbc + ", rpc=" + rpc + ", rdt=" + rdt + ", actionPending=" + actionPending
                    + ", rdtAvailable=" + rdtAvailable + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (actionPending ? 1231 : 1237);
            result = prime * result + rbc.hashCode();
            result = prime * result + rdt.hashCode();
            result = prime * result + (rdtAvailable ? 1231 : 1237);
            result = prime * result + rpc.hashCode();
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
            EManagerModel other = (EManagerModel) obj;
            if (actionPending != other.actionPending) {
                return false;
            }
            if (!rbc.equals(other.rbc)) {
                return false;
            }
            if (!rdt.equals(other.rdt)) {
                return false;
            }
            if (rdtAvailable != other.rdtAvailable) {
                return false;
            }
            if (!rpc.equals(other.rpc)) {
                return false;
            }
            return true;
        }

        public class ProfileDTO {
            private int profileId;
            private String profileName = "";
            private String timeStamp = "";
            private boolean charging;
            private boolean climatisation;
            private int targetChargeLevel;
            private boolean nightRateActive;
            private String nightRateTimeStart = "";
            private String nightRateTimeEnd = "";
            private int chargeMaxCurrent;
            private String heaterSource = "";

            public int getProfileId() {
                return profileId;
            }

            public String getProfileName() {
                return profileName;
            }

            public String getTimeStamp() {
                return timeStamp;
            }

            public boolean isCharging() {
                return charging;
            }

            public boolean isClimatisation() {
                return climatisation;
            }

            public int getTargetChargeLevel() {
                return targetChargeLevel;
            }

            public boolean isNightRateActive() {
                return nightRateActive;
            }

            public String getNightRateTimeStart() {
                return nightRateTimeStart;
            }

            public String getNightRateTimeEnd() {
                return nightRateTimeEnd;
            }

            public int getChargeMaxCurrent() {
                return chargeMaxCurrent;
            }

            public String getHeaterSource() {
                return heaterSource;
            }

            @Override
            public String toString() {
                return "ProfileDTO [profileId=" + profileId + ", profileName=" + profileName + ", timeStamp="
                        + timeStamp + ", charging=" + charging + ", climatisation=" + climatisation
                        + ", targetChargeLevel=" + targetChargeLevel + ", nightRateActive=" + nightRateActive
                        + ", nightRateTimeStart=" + nightRateTimeStart + ", nightRateTimeEnd=" + nightRateTimeEnd
                        + ", chargeMaxCurrent=" + chargeMaxCurrent + ", heaterSource=" + heaterSource + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + chargeMaxCurrent;
                result = prime * result + (charging ? 1231 : 1237);
                result = prime * result + (climatisation ? 1231 : 1237);
                result = prime * result + heaterSource.hashCode();
                result = prime * result + (nightRateActive ? 1231 : 1237);
                result = prime * result + nightRateTimeEnd.hashCode();
                result = prime * result + nightRateTimeStart.hashCode();
                result = prime * result + profileId;
                result = prime * result + profileName.hashCode();
                result = prime * result + targetChargeLevel;
                result = prime * result + timeStamp.hashCode();
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
                ProfileDTO other = (ProfileDTO) obj;
                if (chargeMaxCurrent != other.chargeMaxCurrent) {
                    return false;
                }
                if (charging != other.charging) {
                    return false;
                }
                if (climatisation != other.climatisation) {
                    return false;
                }
                if (!heaterSource.equals(other.heaterSource)) {
                    return false;
                }
                if (nightRateActive != other.nightRateActive) {
                    return false;
                }
                if (!nightRateTimeEnd.equals(other.nightRateTimeEnd)) {
                    return false;
                }
                if (!nightRateTimeStart.equals(other.nightRateTimeStart)) {
                    return false;
                }
                if (profileId != other.profileId) {
                    return false;
                }
                if (!profileName.equals(other.profileName)) {
                    return false;
                }
                if (targetChargeLevel != other.targetChargeLevel) {
                    return false;
                }
                if (!timeStamp.equals(other.timeStamp)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RbcDTO {

            private StatusDTO status = new StatusDTO();
            private SettingsDTO settings = new SettingsDTO();

            public StatusDTO getStatus() {
                return status;
            }

            public SettingsDTO getSettings() {
                return settings;
            }

            @Override
            public String toString() {
                return "RbcDTO [status=" + status + ", settings=" + settings + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + settings.hashCode();
                result = prime * result + status.hashCode();
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
                RbcDTO other = (RbcDTO) obj;
                if (!settings.equals(other.settings)) {
                    return false;
                }
                if (!status.equals(other.status)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RdtDTO {
            private RdtStatusDTO status = new RdtStatusDTO();
            private RdtSettingsDTO settings = new RdtSettingsDTO();
            private boolean auxHeatingAllowed;
            private boolean auxHeatingEnabled;

            public RdtStatusDTO getStatus() {
                return status;
            }

            public RdtSettingsDTO getSettings() {
                return settings;
            }

            public boolean isAuxHeatingAllowed() {
                return auxHeatingAllowed;
            }

            public boolean isAuxHeatingEnabled() {
                return auxHeatingEnabled;
            }

            @Override
            public String toString() {
                return "RdtDTO [status=" + status + ", settings=" + settings + ", auxHeatingAllowed="
                        + auxHeatingAllowed + ", auxHeatingEnabled=" + auxHeatingEnabled + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + (auxHeatingAllowed ? 1231 : 1237);
                result = prime * result + (auxHeatingEnabled ? 1231 : 1237);
                result = prime * result + settings.hashCode();
                result = prime * result + status.hashCode();
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
                RdtDTO other = (RdtDTO) obj;
                if (auxHeatingAllowed != other.auxHeatingAllowed) {
                    return false;
                }
                if (auxHeatingEnabled != other.auxHeatingEnabled) {
                    return false;
                }
                if (!settings.equals(other.settings)) {
                    return false;
                }
                if (!status.equals(other.status)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RpcDTO {
            private RpcStatusDTO status = new RpcStatusDTO();
            private RpcSettingsDTO settings = new RpcSettingsDTO();
            private String climaterActionState = "";
            private boolean auAvailable;

            public RpcStatusDTO getStatus() {
                return status;
            }

            public RpcSettingsDTO getSettings() {
                return settings;
            }

            public String getClimaterActionState() {
                return climaterActionState;
            }

            public boolean isAuAvailable() {
                return auAvailable;
            }

            @Override
            public String toString() {
                return "RpcDTO [status=" + status + ", settings=" + settings + ", climaterActionState="
                        + climaterActionState + ", auAvailable=" + auAvailable + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + (auAvailable ? 1231 : 1237);
                result = prime * result + climaterActionState.hashCode();
                result = prime * result + settings.hashCode();
                result = prime * result + status.hashCode();
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
                RpcDTO other = (RpcDTO) obj;
                if (auAvailable != other.auAvailable) {
                    return false;
                }
                if (!climaterActionState.equals(other.climaterActionState)) {
                    return false;
                }
                if (!settings.equals(other.settings)) {
                    return false;
                }
                if (!status.equals(other.status)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class ScheduleDTO {
            private int type;
            private StartDTO start = new StartDTO();
            private EndDTO end = new EndDTO();
            private Object index = new Object();
            private List<String> daypicker = new ArrayList<>();
            private @Nullable String startDateActive = "";
            private @Nullable String endDateActive = "";

            public int getType() {
                return type;
            }

            public StartDTO getStart() {
                return start;
            }

            public EndDTO getEnd() {
                return end;
            }

            public Object getIndex() {
                return index;
            }

            public List<String> getDaypicker() {
                return daypicker;
            }

            public @Nullable String getStartDateActive() {
                return startDateActive;
            }

            public @Nullable String getEndDateActive() {
                return endDateActive;
            }

            @Override
            public String toString() {
                return "ScheduleDTO [type=" + type + ", start=" + start + ", end=" + end + ", index=" + index
                        + ", daypicker=" + daypicker + ", startDateActive=" + startDateActive + ", endDateActive="
                        + endDateActive + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + daypicker.hashCode();
                result = prime * result + end.hashCode();
                String endDateActive2 = endDateActive;
                result = prime * result + ((endDateActive2 == null) ? 0 : endDateActive2.hashCode());
                result = prime * result + index.hashCode();
                result = prime * result + start.hashCode();
                String startDateActive2 = startDateActive;
                result = prime * result + ((startDateActive2 == null) ? 0 : startDateActive2.hashCode());
                result = prime * result + type;
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
                ScheduleDTO other = (ScheduleDTO) obj;
                if (!daypicker.equals(other.daypicker)) {
                    return false;
                }
                if (!end.equals(other.end)) {
                    return false;
                }
                if (endDateActive != null && !endDateActive.equals(other.endDateActive)) {
                    return false;
                }
                if (!start.equals(other.start)) {
                    return false;
                }
                if (startDateActive != null && !startDateActive.equals(other.startDateActive)) {
                    return false;
                }
                if (type != other.type) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class SettingsDTO {
            private int chargerMaxCurrent = BaseVehicleDTO.UNDEFINED;
            private int maxAmpere = BaseVehicleDTO.UNDEFINED;
            private boolean maxCurrentReduced;

            public int getChargerMaxCurrent() {
                return chargerMaxCurrent;
            }

            public int getMaxAmpere() {
                return maxAmpere;
            }

            public boolean isMaxCurrentReduced() {
                return maxCurrentReduced;
            }

            @Override
            public String toString() {
                return "SettingsDTO [chargerMaxCurrent=" + chargerMaxCurrent + ", maxAmpere=" + maxAmpere
                        + ", maxCurrentReduced=" + maxCurrentReduced + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + chargerMaxCurrent;
                result = prime * result + maxAmpere;
                result = prime * result + (maxCurrentReduced ? 1231 : 1237);
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
                SettingsDTO other = (SettingsDTO) obj;
                if (chargerMaxCurrent != other.chargerMaxCurrent) {
                    return false;
                }
                if (maxAmpere != other.maxAmpere) {
                    return false;
                }
                if (maxCurrentReduced != other.maxCurrentReduced) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RpcSettingsDTO {
            private String targetTemperature = "";
            private boolean climatisationWithoutHVPower;
            private boolean electric;

            public String getTargetTemperature() {
                return targetTemperature;
            }

            public boolean isClimatisationWithoutHVPower() {
                return climatisationWithoutHVPower;
            }

            public boolean isElectric() {
                return electric;
            }

            @Override
            public String toString() {
                return "RpcSettingsDTO [targetTemperature=" + targetTemperature + ", climatisationWithoutHVPower="
                        + climatisationWithoutHVPower + ", electric=" + electric + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + (climatisationWithoutHVPower ? 1231 : 1237);
                result = prime * result + (electric ? 1231 : 1237);
                result = prime * result + targetTemperature.hashCode();
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
                RpcSettingsDTO other = (RpcSettingsDTO) obj;
                if (climatisationWithoutHVPower != other.climatisationWithoutHVPower) {
                    return false;
                }
                if (electric != other.electric) {
                    return false;
                }
                if (!targetTemperature.equals(other.targetTemperature)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RdtSettingsDTO {

            private int minChargeLimit;
            private int lowerLimitMax;

            public int getMinChargeLimit() {
                return minChargeLimit;
            }

            public int getLowerLimitMax() {
                return lowerLimitMax;
            }

            @Override
            public String toString() {
                return "RdtSettingsDTO [minChargeLimit=" + minChargeLimit + ", lowerLimitMax=" + lowerLimitMax + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + lowerLimitMax;
                result = prime * result + minChargeLimit;
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
                RdtSettingsDTO other = (RdtSettingsDTO) obj;
                if (lowerLimitMax != other.lowerLimitMax) {
                    return false;
                }
                if (minChargeLimit != other.minChargeLimit) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class StartDTO {
            private int hours;
            private int minutes;

            public int getHours() {
                return hours;
            }

            public int getMinutes() {
                return minutes;
            }

            @Override
            public String toString() {
                return "StartDTO [hours=" + hours + ", minutes=" + minutes + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + hours;
                result = prime * result + minutes;
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
                StartDTO other = (StartDTO) obj;
                if (hours != other.hours) {
                    return false;
                }
                if (minutes != other.minutes) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class EndDTO {
            private int hours;
            private int minutes;

            public int getHours() {
                return hours;
            }

            public int getMinutes() {
                return minutes;
            }

            @Override
            public String toString() {
                return "EndDTO [hours=" + hours + ", minutes=" + minutes + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + hours;
                result = prime * result + minutes;
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
                EndDTO other = (EndDTO) obj;
                if (hours != other.hours) {
                    return false;
                }
                if (minutes != other.minutes) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class StatusDTO {
            private int batteryPercentage;
            private String chargingState = "";
            private String chargingRemaningHour = "";
            private String chargingRemaningMinute = "";
            private String chargingReason = "";
            private String pluginState = "";
            private String lockState = "";
            private String extPowerSupplyState = "";
            private double range = BaseVehicleDTO.UNDEFINED;
            private double electricRange = BaseVehicleDTO.UNDEFINED;
            private double combustionRange = BaseVehicleDTO.UNDEFINED;
            private double combinedRange = BaseVehicleDTO.UNDEFINED;
            private boolean rlzeUp;

            public int getBatteryPercentage() {
                return batteryPercentage;
            }

            public boolean getChargingState() {
                return chargingState.equals("CHARGING") ? true : false;
            }

            public int getChargingRemainingHour() {
                String localChargingRemaningHour = chargingRemaningHour;
                if (!localChargingRemaningHour.equals("--") && !localChargingRemaningHour.equals("")) {
                    return Integer.parseInt(localChargingRemaningHour);
                }
                return BaseVehicleDTO.UNDEFINED;
            }

            public int getChargingRemainingMinute() {
                String localChargingRemaningMinute = chargingRemaningMinute;
                if (!localChargingRemaningMinute.equals("--") && !localChargingRemaningMinute.equals("")) {
                    return Integer.parseInt(localChargingRemaningMinute);
                }
                return BaseVehicleDTO.UNDEFINED;
            }

            public String getChargingReason() {
                return chargingReason;
            }

            public boolean getPluginState() {
                return pluginState.equals("CONNECTED") ? true : false;
            }

            public boolean getLockState() {
                return lockState.equals("LOCKED") ? true : false;
            }

            public boolean getExtPowerSupplyState() {
                return extPowerSupplyState.equals("STATION_CONNECTED") ? true : false;
            }

            public double getRange() {
                return range;
            }

            public double getElectricRange() {
                return electricRange;
            }

            public double getCombustionRange() {
                return combustionRange;
            }

            public double getCombinedRange() {
                return combinedRange;
            }

            public boolean isRlzeUp() {
                return rlzeUp;
            }

            @Override
            public String toString() {
                return "StatusDTO [batteryPercentage=" + batteryPercentage + ", chargingState=" + chargingState
                        + ", chargingRemaningHour=" + chargingRemaningHour + ", chargingRemaningMinute="
                        + chargingRemaningMinute + ", chargingReason=" + chargingReason + ", pluginState=" + pluginState
                        + ", lockState=" + lockState + ", extPowerSupplyState=" + extPowerSupplyState + ", range="
                        + range + ", electricRange=" + electricRange + ", combustionRange=" + combustionRange
                        + ", combinedRange=" + combinedRange + ", rlzeUp=" + rlzeUp + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + batteryPercentage;
                result = prime * result + chargingReason.hashCode();
                result = prime * result + chargingRemaningHour.hashCode();
                result = prime * result + chargingRemaningMinute.hashCode();
                result = prime * result + chargingState.hashCode();
                long temp;
                temp = Double.doubleToLongBits(combinedRange);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                temp = Double.doubleToLongBits(combustionRange);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                temp = Double.doubleToLongBits(electricRange);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                result = prime * result + extPowerSupplyState.hashCode();
                result = prime * result + lockState.hashCode();
                result = prime * result + pluginState.hashCode();
                temp = Double.doubleToLongBits(range);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                result = prime * result + (rlzeUp ? 1231 : 1237);
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
                if (batteryPercentage != other.batteryPercentage) {
                    return false;
                }
                if (!chargingReason.equals(other.chargingReason)) {
                    return false;
                }
                if (!chargingRemaningHour.equals(other.chargingRemaningHour)) {
                    return false;
                }
                if (!chargingRemaningMinute.equals(other.chargingRemaningMinute)) {
                    return false;
                }
                if (!chargingState.equals(other.chargingState)) {
                    return false;
                }
                if (Double.doubleToLongBits(combinedRange) != Double.doubleToLongBits(other.combinedRange)) {
                    return false;
                }
                if (Double.doubleToLongBits(combustionRange) != Double.doubleToLongBits(other.combustionRange)) {
                    return false;
                }
                if (Double.doubleToLongBits(electricRange) != Double.doubleToLongBits(other.electricRange)) {
                    return false;
                }
                if (!extPowerSupplyState.equals(other.extPowerSupplyState)) {
                    return false;
                }
                if (!lockState.equals(other.lockState)) {
                    return false;
                }
                if (!pluginState.equals(other.pluginState)) {
                    return false;
                }
                if (Double.doubleToLongBits(range) != Double.doubleToLongBits(other.range)) {
                    return false;
                }
                if (rlzeUp != other.rlzeUp) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RpcStatusDTO {
            private String climatisationState = "";
            private int climatisationRemaningTime = BaseVehicleDTO.UNDEFINED;
            private String windowHeatingStateFront = "";
            private String windowHeatingStateRear = "";
            private @Nullable String climatisationReason = "";
            private boolean windowHeatingAvailable;

            public boolean getClimatisationState() {
                return climatisationState.equals("OFF") ? false : true;
            }

            public int getClimatisationRemaningTime() {
                return climatisationRemaningTime;
            }

            public boolean getWindowHeatingStateFront() {
                return windowHeatingStateFront.equals("ON") ? true : false;
            }

            public boolean getWindowHeatingStateRear() {
                return windowHeatingStateRear.equals("ON") ? true : false;
            }

            public boolean getWindowHeatingState() {
                return getWindowHeatingStateFront() || getWindowHeatingStateRear();
            }

            public @Nullable String getClimatisationReason() {
                return climatisationReason;
            }

            public boolean isWindowHeatingAvailable() {
                return windowHeatingAvailable;
            }

            @Override
            public String toString() {
                return "RpcStatusDTO [climatisationState=" + climatisationState + ", climatisationRemaningTime="
                        + climatisationRemaningTime + ", windowHeatingStateFront=" + windowHeatingStateFront
                        + ", windowHeatingStateRear=" + windowHeatingStateRear + ", climatisationReason="
                        + climatisationReason + ", windowHeatingAvailable=" + windowHeatingAvailable + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                String climatisationReason2 = climatisationReason;
                result = prime * result + ((climatisationReason2 == null) ? 0 : climatisationReason2.hashCode());
                result = prime * result + climatisationRemaningTime;
                result = prime * result + climatisationState.hashCode();
                result = prime * result + (windowHeatingAvailable ? 1231 : 1237);
                result = prime * result + windowHeatingStateFront.hashCode();
                result = prime * result + windowHeatingStateRear.hashCode();
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
                RpcStatusDTO other = (RpcStatusDTO) obj;
                if (climatisationReason != null && !climatisationReason.equals(other.climatisationReason)) {
                    return false;
                }
                if (climatisationRemaningTime != other.climatisationRemaningTime) {
                    return false;
                }
                if (!climatisationState.equals(other.climatisationState)) {
                    return false;
                }
                if (windowHeatingAvailable != other.windowHeatingAvailable) {
                    return false;
                }
                if (!windowHeatingStateFront.equals(other.windowHeatingStateFront)) {
                    return false;
                }
                if (!windowHeatingStateRear.equals(other.windowHeatingStateRear)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class RdtStatusDTO {
            private List<TimerDTO> timers = new ArrayList<>();
            private List<ProfileDTO> profiles = new ArrayList<>();

            public List<TimerDTO> getTimers() {
                return timers;
            }

            public List<ProfileDTO> getProfiles() {
                return profiles;
            }

            @Override
            public String toString() {
                return "RdtStatusDTO [timers=" + timers + ", profiles=" + profiles + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + profiles.hashCode();
                result = prime * result + timers.hashCode();
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
                RdtStatusDTO other = (RdtStatusDTO) obj;
                if (!profiles.equals(other.profiles)) {
                    return false;
                }
                if (!timers.equals(other.timers)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        public class TimerDTO {
            private int timerId;
            private int timerProfileId;
            private String timerStatus = "";
            private String timerChargeScheduleStatus = "";
            private String timerClimateScheduleStatus = "";
            private String timerExpStatusTimestamp = "";
            private String timerProgrammedStatus = "";
            private ScheduleDTO schedule = new ScheduleDTO();
            private String startDateActive = "";
            private String timeRangeActive = "";

            public int getTimerId() {
                return timerId;
            }

            public int getTimerProfileId() {
                return timerProfileId;
            }

            public String getTimerStatus() {
                return timerStatus;
            }

            public String getTimerChargeScheduleStatus() {
                return timerChargeScheduleStatus;
            }

            public String getTimerClimateScheduleStatus() {
                return timerClimateScheduleStatus;
            }

            public String getTimerExpStatusTimestamp() {
                return timerExpStatusTimestamp;
            }

            public String getTimerProgrammedStatus() {
                return timerProgrammedStatus;
            }

            public ScheduleDTO getSchedule() {
                return schedule;
            }

            public String getStartDateActive() {
                return startDateActive;
            }

            public String getTimeRangeActive() {
                return timeRangeActive;
            }

            @Override
            public String toString() {
                return "TimerDTO [timerId=" + timerId + ", timerProfileId=" + timerProfileId + ", timerStatus="
                        + timerStatus + ", timerChargeScheduleStatus=" + timerChargeScheduleStatus
                        + ", timerClimateScheduleStatus=" + timerClimateScheduleStatus + ", timerExpStatusTimestamp="
                        + timerExpStatusTimestamp + ", timerProgrammedStatus=" + timerProgrammedStatus + ", schedule="
                        + schedule + ", startDateActive=" + startDateActive + ", timeRangeActive=" + timeRangeActive
                        + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + schedule.hashCode();
                result = prime * result + startDateActive.hashCode();
                result = prime * result + timeRangeActive.hashCode();
                result = prime * result + timerChargeScheduleStatus.hashCode();
                result = prime * result + timerClimateScheduleStatus.hashCode();
                result = prime * result + timerExpStatusTimestamp.hashCode();
                result = prime * result + timerId;
                result = prime * result + timerProfileId;
                result = prime * result + timerProgrammedStatus.hashCode();
                result = prime * result + timerStatus.hashCode();
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
                TimerDTO other = (TimerDTO) obj;
                if (!schedule.equals(other.schedule)) {
                    return false;
                }
                if (!startDateActive.equals(other.startDateActive)) {
                    return false;
                }
                if (!timeRangeActive.equals(other.timeRangeActive)) {
                    return false;
                }
                if (!timerChargeScheduleStatus.equals(other.timerChargeScheduleStatus)) {
                    return false;
                }
                if (!timerClimateScheduleStatus.equals(other.timerClimateScheduleStatus)) {
                    return false;
                }
                if (!timerExpStatusTimestamp.equals(other.timerExpStatusTimestamp)) {
                    return false;
                }
                if (timerId != other.timerId) {
                    return false;
                }
                if (timerProfileId != other.timerProfileId) {
                    return false;
                }
                if (!timerProgrammedStatus.equals(other.timerProgrammedStatus)) {
                    return false;
                }
                if (!timerStatus.equals(other.timerStatus)) {
                    return false;
                }
                return true;
            }

            private EManagerModel getEnclosingInstance() {
                return EManagerModel.this;
            }
        }

        private EManagerDTO getEnclosingInstance() {
            return EManagerDTO.this;
        }
    }
}
