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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
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
public class EManager {
    private @Nullable String errorCode;
    @SerializedName("EManager")
    private EManagerModel eManager = new EManagerModel();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public EManagerModel getEManager() {
        return eManager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode).append("eManager", eManager).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eManager).append(errorCode).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EManager) == false) {
            return false;
        }
        EManager rhs = ((EManager) other);
        return new EqualsBuilder().append(eManager, rhs.eManager).append(errorCode, rhs.errorCode).isEquals();
    }

    public class EManagerModel {
        private Rbc rbc = new Rbc();
        private Rpc rpc = new Rpc();
        private Rdt rdt = new Rdt();
        private boolean actionPending;
        private boolean rdtAvailable;

        public Rbc getRbc() {
            return rbc;
        }

        public Rpc getRpc() {
            return rpc;
        }

        public Rdt getRdt() {
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
            return new ToStringBuilder(this).append("rbc", rbc).append("rpc", rpc).append("rdt", rdt)
                    .append("actionPending", actionPending).append("rdtAvailable", rdtAvailable).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(rbc).append(rdt).append(rdtAvailable).append(rpc).append(actionPending)
                    .toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof EManager) == false) {
                return false;
            }
            EManagerModel rhs = ((EManagerModel) other);
            return new EqualsBuilder().append(rbc, rhs.rbc).append(rdt, rhs.rdt).append(rdtAvailable, rhs.rdtAvailable)
                    .append(rpc, rhs.rpc).append(actionPending, rhs.actionPending).isEquals();
        }

        public class Profile {
            private int profileId;
            private @Nullable String profileName;
            private @Nullable String timeStamp;
            private boolean charging;
            private boolean climatisation;
            private int targetChargeLevel;
            private boolean nightRateActive;
            private @Nullable String nightRateTimeStart;
            private @Nullable String nightRateTimeEnd;
            private int chargeMaxCurrent;
            private @Nullable String heaterSource;

            public int getProfileId() {
                return profileId;
            }

            public @Nullable String getProfileName() {
                return profileName;
            }

            public @Nullable String getTimeStamp() {
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

            public @Nullable String getNightRateTimeStart() {
                return nightRateTimeStart;
            }

            public @Nullable String getNightRateTimeEnd() {
                return nightRateTimeEnd;
            }

            public int getChargeMaxCurrent() {
                return chargeMaxCurrent;
            }

            public @Nullable String getHeaterSource() {
                return heaterSource;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this).append("profileId", profileId).append("profileName", profileName)
                        .append("timeStamp", timeStamp).append("charging", charging)
                        .append("climatisation", climatisation).append("targetChargeLevel", targetChargeLevel)
                        .append("nightRateActive", nightRateActive).append("nightRateTimeStart", nightRateTimeStart)
                        .append("nightRateTimeEnd", nightRateTimeEnd).append("chargeMaxCurrent", chargeMaxCurrent)
                        .append("heaterSource", heaterSource).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(profileName).append(timeStamp).append(targetChargeLevel)
                        .append(nightRateTimeEnd).append(chargeMaxCurrent).append(climatisation)
                        .append(nightRateTimeStart).append(profileId).append(heaterSource).append(nightRateActive)
                        .append(charging).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Profile) == false) {
                    return false;
                }
                Profile rhs = ((Profile) other);
                return new EqualsBuilder().append(profileName, rhs.profileName).append(timeStamp, rhs.timeStamp)
                        .append(targetChargeLevel, rhs.targetChargeLevel).append(nightRateTimeEnd, rhs.nightRateTimeEnd)
                        .append(chargeMaxCurrent, rhs.chargeMaxCurrent).append(climatisation, rhs.climatisation)
                        .append(nightRateTimeStart, rhs.nightRateTimeStart).append(profileId, rhs.profileId)
                        .append(heaterSource, rhs.heaterSource).append(nightRateActive, rhs.nightRateActive)
                        .append(charging, rhs.charging).isEquals();
            }
        }

        public class Rbc {

            private Status status = new Status();
            private Settings settings = new Settings();

            public Status getStatus() {
                return status;
            }

            public Settings getSettings() {
                return settings;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this).append("status", status).append("settings", settings).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(settings).append(status).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Rbc) == false) {
                    return false;
                }
                Rbc rhs = ((Rbc) other);
                return new EqualsBuilder().append(settings, rhs.settings).append(status, rhs.status).isEquals();
            }
        }

        public class Rdt {
            private RdtStatus status = new RdtStatus();
            private RdtSettings settings = new RdtSettings();
            private boolean auxHeatingAllowed;
            private boolean auxHeatingEnabled;

            public RdtStatus getStatus() {
                return status;
            }

            public RdtSettings getSettings() {
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
                return new ToStringBuilder(this).append("status", status).append("settings", settings)
                        .append("auxHeatingAllowed", auxHeatingAllowed).append("auxHeatingEnabled", auxHeatingEnabled)
                        .toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(settings).append(auxHeatingAllowed).append(status)
                        .append(auxHeatingEnabled).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Rdt) == false) {
                    return false;
                }
                Rdt rhs = ((Rdt) other);
                return new EqualsBuilder().append(settings, rhs.settings)
                        .append(auxHeatingAllowed, rhs.auxHeatingAllowed).append(status, rhs.status)
                        .append(auxHeatingEnabled, rhs.auxHeatingEnabled).isEquals();
            }
        }

        public class Rpc {
            private RpcStatus status = new RpcStatus();
            private RpcSettings settings = new RpcSettings();
            private @Nullable String climaterActionState;
            private boolean auAvailable;

            public RpcStatus getStatus() {
                return status;
            }

            public RpcSettings getSettings() {
                return settings;
            }

            public @Nullable String getClimaterActionState() {
                return climaterActionState;
            }

            public boolean isAuAvailable() {
                return auAvailable;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this).append("status", status).append("settings", settings)
                        .append("climaterActionState", climaterActionState).append("auAvailable", auAvailable)
                        .toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(settings).append(auAvailable).append(climaterActionState)
                        .append(status).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Rpc) == false) {
                    return false;
                }
                Rpc rhs = ((Rpc) other);
                return new EqualsBuilder().append(settings, rhs.settings).append(auAvailable, rhs.auAvailable)
                        .append(climaterActionState, rhs.climaterActionState).append(status, rhs.status).isEquals();
            }
        }

        public class Schedule {
            private int type;
            private Start start = new Start();
            private End end = new End();
            private @Nullable Object index;
            private List<String> daypicker = new ArrayList<>();
            private @Nullable String startDateActive;
            private @Nullable String endDateActive;

            public int getType() {
                return type;
            }

            public Start getStart() {
                return start;
            }

            public End getEnd() {
                return end;
            }

            public @Nullable Object getIndex() {
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
                return new ToStringBuilder(this).append("type", type).append("start", start).append("end", end)
                        .append("index", index).append("daypicker", daypicker)
                        .append("startDateActive", startDateActive).append("endDateActive", endDateActive).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(endDateActive).append(startDateActive).append(start).append(index)
                        .append(end).append(type).append(daypicker).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Schedule) == false) {
                    return false;
                }
                Schedule rhs = ((Schedule) other);
                return new EqualsBuilder().append(endDateActive, rhs.endDateActive)
                        .append(startDateActive, rhs.startDateActive).append(start, rhs.start).append(index, rhs.index)
                        .append(end, rhs.end).append(type, rhs.type).append(daypicker, rhs.daypicker).isEquals();
            }
        }

        public class Settings {
            private int chargerMaxCurrent = BaseVehicle.UNDEFINED;
            private int maxAmpere = BaseVehicle.UNDEFINED;
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
                return new ToStringBuilder(this).append("chargerMaxCurrent", chargerMaxCurrent)
                        .append("maxAmpere", maxAmpere).append("maxCurrentReduced", maxCurrentReduced).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(maxAmpere).append(maxCurrentReduced).append(chargerMaxCurrent)
                        .toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Settings) == false) {
                    return false;
                }
                Settings rhs = ((Settings) other);
                return new EqualsBuilder().append(maxAmpere, rhs.maxAmpere)
                        .append(maxCurrentReduced, rhs.maxCurrentReduced)
                        .append(chargerMaxCurrent, rhs.chargerMaxCurrent).isEquals();
            }
        }

        public class RpcSettings {
            private @Nullable String targetTemperature;
            private boolean climatisationWithoutHVPower;
            private boolean electric;

            public @Nullable String getTargetTemperature() {
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
                return new ToStringBuilder(this).append("targetTemperature", targetTemperature)
                        .append("climatisationWithoutHVPower", climatisationWithoutHVPower).append("electric", electric)
                        .toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(climatisationWithoutHVPower).append(targetTemperature)
                        .append(electric).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof RpcSettings) == false) {
                    return false;
                }
                RpcSettings rhs = ((RpcSettings) other);
                return new EqualsBuilder().append(climatisationWithoutHVPower, rhs.climatisationWithoutHVPower)
                        .append(targetTemperature, rhs.targetTemperature).append(electric, rhs.electric).isEquals();
            }
        }

        public class RdtSettings {

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
                return new ToStringBuilder(this).append("minChargeLimit", minChargeLimit)
                        .append("lowerLimitMax", lowerLimitMax).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(minChargeLimit).append(lowerLimitMax).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof RdtSettings) == false) {
                    return false;
                }
                RdtSettings rhs = ((RdtSettings) other);
                return new EqualsBuilder().append(minChargeLimit, rhs.minChargeLimit)
                        .append(lowerLimitMax, rhs.lowerLimitMax).isEquals();
            }
        }

        public class Start {
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
                return new ToStringBuilder(this).append("hours", hours).append("minutes", minutes).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(hours).append(minutes).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Start) == false) {
                    return false;
                }
                Start rhs = ((Start) other);
                return new EqualsBuilder().append(hours, rhs.hours).append(minutes, rhs.minutes).isEquals();
            }
        }

        public class End {
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
                return new ToStringBuilder(this).append("hours", hours).append("minutes", minutes).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(hours).append(minutes).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof End) == false) {
                    return false;
                }
                End rhs = ((End) other);
                return new EqualsBuilder().append(hours, rhs.hours).append(minutes, rhs.minutes).isEquals();
            }
        }

        public class Status {
            private int batteryPercentage;
            private @Nullable String chargingState;
            private @Nullable String chargingRemaningHour;
            private @Nullable String chargingRemaningMinute;
            private @Nullable String chargingReason;
            private @Nullable String pluginState;
            private @Nullable String lockState;
            private @Nullable String extPowerSupplyState;
            private double range = BaseVehicle.UNDEFINED;
            private double electricRange = BaseVehicle.UNDEFINED;
            private double combustionRange = BaseVehicle.UNDEFINED;
            private double combinedRange = BaseVehicle.UNDEFINED;
            private boolean rlzeUp;

            public int getBatteryPercentage() {
                return batteryPercentage;
            }

            public boolean getChargingState() {
                return chargingState != null && chargingState.equals("CHARGING") ? true : false;
            }

            public int getChargingRemainingHour() {
                if (chargingRemaningHour != null && !chargingRemaningHour.equals("")) {
                    return Integer.parseInt(chargingRemaningHour);
                }
                return BaseVehicle.UNDEFINED;
            }

            public int getChargingRemainingMinute() {
                if (chargingRemaningMinute != null && !chargingRemaningMinute.equals("")) {
                    return Integer.parseInt(chargingRemaningMinute);
                }
                return BaseVehicle.UNDEFINED;
            }

            public @Nullable String getChargingReason() {
                return chargingReason;
            }

            public boolean getPluginState() {
                return pluginState != null && pluginState.equals("CONNECTED") ? true : false;
            }

            public boolean getLockState() {
                return lockState != null && lockState.equals("LOCKED") ? true : false;
            }

            public boolean getExtPowerSupplyState() {
                return extPowerSupplyState != null && extPowerSupplyState.equals("AVAILABLE") ? true : false;
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
                return new ToStringBuilder(this).append("batteryPercentage", batteryPercentage)
                        .append("chargingState", chargingState).append("chargingRemaningHour", chargingRemaningHour)
                        .append("chargingRemaningMinute", chargingRemaningMinute)
                        .append("chargingReason", chargingReason).append("pluginState", pluginState)
                        .append("lockState", lockState).append("extPowerSupplyState", extPowerSupplyState)
                        .append("range", range).append("electricRange", electricRange)
                        .append("combustionRange", combustionRange).append("combinedRange", combinedRange)
                        .append("rlzeUp", rlzeUp).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(combustionRange).append(extPowerSupplyState)
                        .append(chargingRemaningHour).append(chargingReason).append(range)
                        .append(chargingRemaningMinute).append(pluginState).append(lockState).append(chargingState)
                        .append(rlzeUp).append(combinedRange).append(batteryPercentage).append(electricRange)
                        .toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Status) == false) {
                    return false;
                }
                Status rhs = ((Status) other);
                return new EqualsBuilder().append(combustionRange, rhs.combustionRange)
                        .append(extPowerSupplyState, rhs.extPowerSupplyState)
                        .append(chargingRemaningHour, rhs.chargingRemaningHour)
                        .append(chargingReason, rhs.chargingReason).append(range, rhs.range)
                        .append(chargingRemaningMinute, rhs.chargingRemaningMinute).append(pluginState, rhs.pluginState)
                        .append(lockState, rhs.lockState).append(chargingState, rhs.chargingState)
                        .append(rlzeUp, rhs.rlzeUp).append(combinedRange, rhs.combinedRange)
                        .append(batteryPercentage, rhs.batteryPercentage).append(electricRange, rhs.electricRange)
                        .isEquals();
            }
        }

        public class RpcStatus {
            private @Nullable String climatisationState;
            private int climatisationRemaningTime = BaseVehicle.UNDEFINED;
            private @Nullable String windowHeatingStateFront;
            private @Nullable String windowHeatingStateRear;
            private @Nullable String climatisationReason;
            private boolean windowHeatingAvailable;

            public boolean getClimatisationState() {
                return climatisationState != null && climatisationState.equals("OFF") ? false : true;
            }

            public int getClimatisationRemaningTime() {
                return climatisationRemaningTime;
            }

            public boolean getWindowHeatingStateFront() {
                return windowHeatingStateFront != null && windowHeatingStateFront.equals("ON") ? true : false;
            }

            public boolean getWindowHeatingStateRear() {
                return windowHeatingStateRear != null && windowHeatingStateRear.equals("ON") ? true : false;
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
                return new ToStringBuilder(this).append("climatisationState", climatisationState)
                        .append("climatisationRemaningTime", climatisationRemaningTime)
                        .append("windowHeatingStateFront", windowHeatingStateFront)
                        .append("windowHeatingStateRear", windowHeatingStateRear)
                        .append("climatisationReason", climatisationReason)
                        .append("windowHeatingAvailable", windowHeatingAvailable).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(climatisationReason).append(windowHeatingAvailable)
                        .append(climatisationState).append(windowHeatingStateRear).append(climatisationRemaningTime)
                        .append(windowHeatingStateFront).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof RpcStatus) == false) {
                    return false;
                }
                RpcStatus rhs = ((RpcStatus) other);
                return new EqualsBuilder().append(climatisationReason, rhs.climatisationReason)
                        .append(windowHeatingAvailable, rhs.windowHeatingAvailable)
                        .append(climatisationState, rhs.climatisationState)
                        .append(windowHeatingStateRear, rhs.windowHeatingStateRear)
                        .append(climatisationRemaningTime, rhs.climatisationRemaningTime)
                        .append(windowHeatingStateFront, rhs.windowHeatingStateFront).isEquals();
            }
        }

        public class RdtStatus {
            private List<Timer> timers = new ArrayList<>();
            private List<Profile> profiles = new ArrayList<>();

            public List<Timer> getTimers() {
                return timers;
            }

            public List<Profile> getProfiles() {
                return profiles;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this).append("timers", timers).append("profiles", profiles).toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(profiles).append(timers).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof RdtStatus) == false) {
                    return false;
                }
                RdtStatus rhs = ((RdtStatus) other);
                return new EqualsBuilder().append(profiles, rhs.profiles).append(timers, rhs.timers).isEquals();
            }
        }

        public class Timer {
            private int timerId;
            private int timerProfileId;
            private @Nullable String timerStatus;
            private @Nullable String timerChargeScheduleStatus;
            private @Nullable String timerClimateScheduleStatus;
            private @Nullable String timerExpStatusTimestamp;
            private @Nullable String timerProgrammedStatus;
            private Schedule schedule = new Schedule();
            private @Nullable String startDateActive;
            private @Nullable String timeRangeActive;

            public int getTimerId() {
                return timerId;
            }

            public int getTimerProfileId() {
                return timerProfileId;
            }

            public @Nullable String getTimerStatus() {
                return timerStatus;
            }

            public @Nullable String getTimerChargeScheduleStatus() {
                return timerChargeScheduleStatus;
            }

            public @Nullable String getTimerClimateScheduleStatus() {
                return timerClimateScheduleStatus;
            }

            public @Nullable String getTimerExpStatusTimestamp() {
                return timerExpStatusTimestamp;
            }

            public @Nullable String getTimerProgrammedStatus() {
                return timerProgrammedStatus;
            }

            public @Nullable Schedule getSchedule() {
                return schedule;
            }

            public @Nullable String getStartDateActive() {
                return startDateActive;
            }

            public @Nullable String getTimeRangeActive() {
                return timeRangeActive;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this).append("timerId", timerId).append("timerProfileId", timerProfileId)
                        .append("timerStatus", timerStatus)
                        .append("timerChargeScheduleStatus", timerChargeScheduleStatus)
                        .append("timerClimateScheduleStatus", timerClimateScheduleStatus)
                        .append("timerExpStatusTimestamp", timerExpStatusTimestamp)
                        .append("timerProgrammedStatus", timerProgrammedStatus).append("schedule", schedule)
                        .append("startDateActive", startDateActive).append("timeRangeActive", timeRangeActive)
                        .toString();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder().append(timerProgrammedStatus).append(schedule).append(startDateActive)
                        .append(timerStatus).append(timerClimateScheduleStatus).append(timeRangeActive)
                        .append(timerChargeScheduleStatus).append(timerExpStatusTimestamp).append(timerProfileId)
                        .append(timerId).toHashCode();
            }

            @Override
            public boolean equals(@Nullable Object other) {
                if (other == this) {
                    return true;
                }
                if ((other instanceof Timer) == false) {
                    return false;
                }
                Timer rhs = ((Timer) other);
                return new EqualsBuilder().append(timerProgrammedStatus, rhs.timerProgrammedStatus)
                        .append(schedule, rhs.schedule).append(startDateActive, rhs.startDateActive)
                        .append(timerStatus, rhs.timerStatus)
                        .append(timerClimateScheduleStatus, rhs.timerClimateScheduleStatus)
                        .append(timeRangeActive, rhs.timeRangeActive)
                        .append(timerChargeScheduleStatus, rhs.timerChargeScheduleStatus)
                        .append(timerExpStatusTimestamp, rhs.timerExpStatusTimestamp)
                        .append(timerProfileId, rhs.timerProfileId).append(timerId, rhs.timerId).isEquals();
            }
        }
    }
}
