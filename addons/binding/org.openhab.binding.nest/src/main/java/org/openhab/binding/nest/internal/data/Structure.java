/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openhab.binding.nest.internal.data.SmokeDetector.AlarmState;

import com.google.gson.annotations.SerializedName;

/**
 * The structure details from Nest.
 *
 * @author David Bennett - Initial Contribution
 */
public class Structure implements NestIdentifiable {

    private String structureId;
    private List<String> thermostats;
    private List<String> smokeCoAlarms;
    private List<String> cameras;
    private String countryCode;
    private String postalCode;
    private Date peakPeriodStartTime;
    private Date peakPeriodEndTime;
    private String timeZone;
    private Date etaBegin;
    private SmokeDetector.AlarmState coAlarmState;
    private SmokeDetector.AlarmState smokeAlarmState;
    private Boolean rhrEnrollment;
    private Map<String, Where> wheres;
    private HomeAwayState away;
    private String name;
    private ETA eta;
    private SecurityState wwnSecurityState;

    @Override
    public String getId() {
        return structureId;
    }

    public HomeAwayState getAway() {
        return away;
    }

    public void setAway(HomeAwayState away) {
        this.away = away;
    }

    public String getStructureId() {
        return structureId;
    }

    public List<String> getThermostats() {
        return thermostats;
    }

    public List<String> getSmokeCoAlarms() {
        return smokeCoAlarms;
    }

    public List<String> getCameras() {
        return cameras;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Date getPeakPeriodStartTime() {
        return peakPeriodStartTime;
    }

    public Date getPeakPeriodEndTime() {
        return peakPeriodEndTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Date getEtaBegin() {
        return etaBegin;
    }

    public AlarmState getCoAlarmState() {
        return coAlarmState;
    }

    public AlarmState getSmokeAlarmState() {
        return smokeAlarmState;
    }

    public Boolean isRhrEnrollment() {
        return rhrEnrollment;
    }

    public Map<String, Where> getWheres() {
        return wheres;
    }

    public ETA getEta() {
        return eta;
    }

    public String getName() {
        return name;
    }

    public SecurityState getWwnSecurityState() {
        return wwnSecurityState;
    }

    /**
     * Used to set and update the eta values for Nest.
     */
    public class ETA {

        private String tripId;
        private Date estimatedArrivalWindowBegin;
        private Date estimatedArrivalWindowEnd;

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        public Date getEstimatedArrivalWindowBegin() {
            return estimatedArrivalWindowBegin;
        }

        public void setEstimatedArrivalWindowBegin(Date estimatedArrivalWindowBegin) {
            this.estimatedArrivalWindowBegin = estimatedArrivalWindowBegin;
        }

        public Date getEstimatedArrivalWindowEnd() {
            return estimatedArrivalWindowEnd;
        }

        public void setEstimatedArrivalWindowEnd(Date estimatedArrivalWindowEnd) {
            this.estimatedArrivalWindowEnd = estimatedArrivalWindowEnd;
        }
    }

    public class Where {
        private String whereId;
        private String name;

        public String getWhereId() {
            return whereId;
        }

        public String getName() {
            return name;
        }
    }

    public enum HomeAwayState {
        @SerializedName("home")
        HOME,
        @SerializedName("away")
        AWAY,
        @SerializedName("unknown")
        UNKNOWN
    }

    public enum SecurityState {
        @SerializedName("ok")
        OK,
        @SerializedName("deter")
        DETER
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Structure [structureId=").append(structureId).append(", thermostats=").append(thermostats)
                .append(", smokeCoAlarms=").append(smokeCoAlarms).append(", cameras=").append(cameras)
                .append(", countryCode=").append(countryCode).append(", postalCode=").append(postalCode)
                .append(", peakPeriodStartTime=").append(peakPeriodStartTime).append(", peakPeriodEndTime=")
                .append(peakPeriodEndTime).append(", timeZone=").append(timeZone).append(", etaBegin=").append(etaBegin)
                .append(", coAlarmState=").append(coAlarmState).append(", smokeAlarmState=").append(smokeAlarmState)
                .append(", rhrEnrollment=").append(rhrEnrollment).append(", wheres=").append(wheres).append(", away=")
                .append(away).append(", name=").append(name).append(", eta=").append(eta).append(", wwnSecurityState=")
                .append(wwnSecurityState).append("]");
        return builder.toString();
    }

}
