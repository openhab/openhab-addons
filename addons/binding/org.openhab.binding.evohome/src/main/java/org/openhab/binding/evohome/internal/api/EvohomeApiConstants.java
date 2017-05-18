package org.openhab.binding.evohome.internal.api;

public class EvohomeApiConstants {
    public static final String URL_AUTH = "https://tccna.honeywell.com/Auth/OAuth/Token";

    public static final String URL_BASE = "https://tccna.honeywell.com/WebAPI/emea/api/v1/";

    public static final String URL_ACCOUNT = "userAccount";
    public static final String URL_LOCATIONS = "location/installationInfo?userId=%s&includeTemperatureControlSystems=True";// userId
    public static final String URL_LOCATION = "location/%s/installationInfo?includeTemperatureControlSystems=True"; // location
    public static final String URL_GATEWAY = "gateway";
    public static final String URL_HOT_WATER = "domesticHotWater/%s/state"; // hardwareId
    public static final String URL_SCHEDULE = "%s/%s/schedule"; // zone_type, zoneId
    public static final String URL_HEAT_SETPOINT = "temperatureZone/%s/heatSetpoint"; // zoneId
    public static final String URL_STATUS = "location/%s/status?includeTemperatureControlSystems=True"; // locationId
    public static final String URL_MODE = "temperatureControlSystem/%s/mode"; // systemId

}
