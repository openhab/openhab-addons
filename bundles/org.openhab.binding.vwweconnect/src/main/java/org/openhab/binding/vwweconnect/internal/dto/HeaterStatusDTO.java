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

/**
 * The Vehicle heating status representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class HeaterStatusDTO {

    private String errorCode = "";
    private int timerCount;
    private RemoteAuxiliaryHeatingDTO remoteAuxiliaryHeating = new RemoteAuxiliaryHeatingDTO();

    public String getErrorCode() {
        return errorCode;
    }

    public int getTimerCount() {
        return timerCount;
    }

    public RemoteAuxiliaryHeatingDTO getRemoteAuxiliaryHeating() {
        return remoteAuxiliaryHeating;
    }

    @Override
    public String toString() {
        return "HeaterStatusDTO [errorCode=" + errorCode + ", timerCount=" + timerCount + ", remoteAuxiliaryHeating="
                + remoteAuxiliaryHeating + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode.hashCode();
        result = prime * result + remoteAuxiliaryHeating.hashCode();
        result = prime * result + timerCount;
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
        HeaterStatusDTO other = (HeaterStatusDTO) obj;
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!remoteAuxiliaryHeating.equals(other.remoteAuxiliaryHeating)) {
            return false;
        }
        if (timerCount != other.timerCount) {
            return false;
        }
        return true;
    }

    public class RemoteAuxiliaryHeatingDTO {

        private StatusDTO status = new StatusDTO();
        private List<TimerDTO> timers = new ArrayList<>();

        public StatusDTO getStatus() {
            return status;
        }

        public List<TimerDTO> getTimers() {
            return timers;
        }

        @Override
        public String toString() {
            return "RemoteAuxiliaryHeatingDTO [status=" + status + ", timers=" + timers + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + status.hashCode();
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
            RemoteAuxiliaryHeatingDTO other = (RemoteAuxiliaryHeatingDTO) obj;
            if (!status.equals(other.status)) {
                return false;
            }
            if (!timers.equals(other.timers)) {
                return false;
            }
            return true;
        }

        private HeaterStatusDTO getEnclosingInstance() {
            return HeaterStatusDTO.this;
        }
    }

    public class StatusDTO {

        private boolean active;
        private String operationMode = "";
        private String outdoorTemp = "";
        private int remainingTime;

        public boolean isActive() {
            return active;
        }

        public String getOperationMode() {
            return operationMode;
        }

        public String getOutdoorTemp() {
            return outdoorTemp;
        }

        public double getTemperature() {
            String[] tempArray = outdoorTemp.split(" ");
            if (tempArray.length > 0 && tempArray[0] != "") {
                double temp = Double.parseDouble(tempArray[0].replace(',', '.'));
                return temp;
            } else {
                return BaseVehicleDTO.UNDEFINED;
            }
        }

        public int getRemainingTime() {
            return remainingTime;
        }

        @Override
        public String toString() {
            return "StatusDTO [active=" + active + ", operationMode=" + operationMode + ", outdoorTemp=" + outdoorTemp
                    + ", remainingTime=" + remainingTime + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (active ? 1231 : 1237);
            result = prime * result + operationMode.hashCode();
            result = prime * result + outdoorTemp.hashCode();
            result = prime * result + remainingTime;
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
            if (active != other.active) {
                return false;
            }
            if (!operationMode.equals(other.operationMode)) {
                return false;
            }
            if (!outdoorTemp.equals(other.outdoorTemp)) {
                return false;
            }
            if (remainingTime != other.remainingTime) {
                return false;
            }
            return true;
        }

        private HeaterStatusDTO getEnclosingInstance() {
            return HeaterStatusDTO.this;
        }
    }

    public class TimeDTO {

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
            return "TimeDTO [hours=" + hours + ", minutes=" + minutes + "]";
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
            TimeDTO other = (TimeDTO) obj;
            if (hours != other.hours) {
                return false;
            }
            if (minutes != other.minutes) {
                return false;
            }
            return true;
        }

        private HeaterStatusDTO getEnclosingInstance() {
            return HeaterStatusDTO.this;
        }
    }

    public class TimerDTO {

        private int timerId;
        private boolean active;
        private int weekDay;
        private TimeDTO time = new TimeDTO();
        private String nextActivationDate = "";

        public int getTimerId() {
            return timerId;
        }

        public boolean isActive() {
            return active;
        }

        public int getWeekDay() {
            return weekDay;
        }

        public TimeDTO getTime() {
            return time;
        }

        public String getNextActivationDate() {
            return nextActivationDate;
        }

        @Override
        public String toString() {
            return "TimerDTO [timerId=" + timerId + ", active=" + active + ", weekDay=" + weekDay + ", time=" + time
                    + ", nextActivationDate=" + nextActivationDate + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (active ? 1231 : 1237);
            result = prime * result + nextActivationDate.hashCode();
            result = prime * result + time.hashCode();
            result = prime * result + timerId;
            result = prime * result + weekDay;
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
            if (active != other.active) {
                return false;
            }
            if (!nextActivationDate.equals(other.nextActivationDate)) {
                return false;
            }
            if (!time.equals(other.time)) {
                return false;
            }
            if (timerId != other.timerId) {
                return false;
            }
            if (weekDay != other.weekDay) {
                return false;
            }
            return true;
        }

        private HeaterStatusDTO getEnclosingInstance() {
            return HeaterStatusDTO.this;
        }
    }
}
