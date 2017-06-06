package org.openhab.binding.nest.internal.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openhab.binding.nest.internal.data.SmokeDetector.AlarmState;

import com.google.gson.annotations.SerializedName;

/**
 * The structure details from nest.
 */
public class Structure {
    public HomeAwayState getAway() {
        return away;
    }

    public void setAway(HomeAwayState away) {
        this.away = away;
    }

    public String getStructureId() {
        return structureId;
    }

    public List<String> getThermostatIds() {
        return thermostatIds;
    }

    public List<String> getSmokeAlarmIds() {
        return smokeAlarmIds;
    }

    public List<String> getCameraIds() {
        return cameraIds;
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

    public boolean isRushHourRewardsEnrollement() {
        return rushHourRewardsEnrollement;
    }

    public Map<String, Where> getWhereIds() {
        return whereIds;
    }

    public ETA getEta() {
        return eta;
    }

    public void setEta(ETA eta) {
        this.eta = eta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("structure_id")
    private String structureId;
    @SerializedName("thermostats")
    private List<String> thermostatIds;
    @SerializedName("smoke_co_alarms")
    private List<String> smokeAlarmIds;
    @SerializedName("cameras")
    private List<String> cameraIds;
    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("postal_code")
    private String postalCode;
    @SerializedName("peak_period_start_time")
    private Date peakPeriodStartTime;
    @SerializedName("peak_period_end_time")
    private Date peakPeriodEndTime;
    @SerializedName("time_zone")
    private String timeZone;
    @SerializedName("eta_begin")
    private Date etaBegin;
    @SerializedName("co_alarm_state")
    private SmokeDetector.AlarmState coAlarmState;
    @SerializedName("smoke_alarm_state")
    private SmokeDetector.AlarmState smokeAlarmState;
    @SerializedName("rhr_enrollment")
    private boolean rushHourRewardsEnrollement;
    @SerializedName("wheres")
    private Map<String, Where> whereIds;
    @SerializedName("away")
    private HomeAwayState away;
    @SerializedName("name")
    private String name;
    @SerializedName("eta")
    private ETA eta;

    /**
     * Used to set and update the eta values for nest.
     */
    public class ETA {
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

        @SerializedName("trip_id")
        private String tripId;
        @SerializedName("estimated_arrival_window_begin")
        private Date estimatedArrivalWindowBegin;
        @SerializedName("estimated_arrival_window_end")
        private Date estimatedArrivalWindowEnd;
    }

    public class Where {

        public String getWhereId() {
            return whereId;
        }

        public String getName() {
            return name;
        }

        @SerializedName("where_id")
        private String whereId;
        @SerializedName("name")
        private String name;
    }

    public enum HomeAwayState {
        home,
        away,
        unknown
    }
}
