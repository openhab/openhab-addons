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

/**
 * The Vehicle heating status representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class HeaterStatus {

    private @Nullable String errorCode;
    private int timerCount;
    private RemoteAuxiliaryHeating remoteAuxiliaryHeating = new RemoteAuxiliaryHeating();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public int getTimerCount() {
        return timerCount;
    }

    public RemoteAuxiliaryHeating getRemoteAuxiliaryHeating() {
        return remoteAuxiliaryHeating;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode).append("timerCount", timerCount)
                .append("remoteAuxiliaryHeating", remoteAuxiliaryHeating).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(timerCount).append(remoteAuxiliaryHeating).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeaterStatus)) {
            return false;
        }
        HeaterStatus rhs = ((HeaterStatus) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode).append(timerCount, rhs.timerCount)
                .append(remoteAuxiliaryHeating, rhs.remoteAuxiliaryHeating).isEquals();
    }

    public class RemoteAuxiliaryHeating {

        private Status status = new Status();
        private List<Timer> timers = new ArrayList<>();

        public Status getStatus() {
            return status;
        }

        public List<Timer> getTimers() {
            return timers;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("status", status).append("timers", timers).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(timers).append(status).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof RemoteAuxiliaryHeating)) {
                return false;
            }
            RemoteAuxiliaryHeating rhs = ((RemoteAuxiliaryHeating) other);
            return new EqualsBuilder().append(timers, rhs.timers).append(status, rhs.status).isEquals();
        }
    }

    public class Status {

        private boolean active;
        private @Nullable String operationMode;
        private @Nullable String outdoorTemp;
        private double temperature = BaseVehicle.UNDEFINED;
        private int remainingTime;

        public boolean isActive() {
            return active;
        }

        public @Nullable String getOperationMode() {
            return operationMode;
        }

        public @Nullable String getOutdoorTemp() {
            return outdoorTemp;
        }

        public double getTemperature() {
            if (outdoorTemp != null) {
                String[] tempArray = outdoorTemp.split(" ");
                double temp = Double.parseDouble(tempArray[0].replace(',', '.'));
                return temp;
            }
            return temperature;
        }

        public int getRemainingTime() {
            return remainingTime;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("active", active).append("operationMode", operationMode)
                    .append("outdoorTemp", outdoorTemp).append("remainingTime", remainingTime).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(active).append(operationMode).append(outdoorTemp).append(remainingTime)
                    .toHashCode();
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
            return new EqualsBuilder().append(active, rhs.active).append(operationMode, rhs.operationMode)
                    .append(outdoorTemp, rhs.outdoorTemp).append(remainingTime, rhs.remainingTime).isEquals();
        }
    }

    public class Time {

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
            if (!(other instanceof Time)) {
                return false;
            }
            Time rhs = ((Time) other);
            return new EqualsBuilder().append(hours, rhs.hours).append(minutes, rhs.minutes).isEquals();
        }
    }

    public class Timer {

        private int timerId;
        private boolean active;
        private int weekDay;
        private Time time = new Time();
        private @Nullable String nextActivationDate;

        public int getTimerId() {
            return timerId;
        }

        public boolean isActive() {
            return active;
        }

        public int getWeekDay() {
            return weekDay;
        }

        public Time getTime() {
            return time;
        }

        public @Nullable String getNextActivationDate() {
            return nextActivationDate;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("timerId", timerId).append("active", active)
                    .append("weekDay", weekDay).append("time", time).append("nextActivationDate", nextActivationDate)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(weekDay).append(nextActivationDate).append(active).append(time)
                    .append(timerId).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Timer)) {
                return false;
            }
            Timer rhs = ((Timer) other);
            return new EqualsBuilder().append(weekDay, rhs.weekDay).append(nextActivationDate, rhs.nextActivationDate)
                    .append(active, rhs.active).append(time, rhs.time).append(timerId, rhs.timerId).isEquals();
        }
    }
}
