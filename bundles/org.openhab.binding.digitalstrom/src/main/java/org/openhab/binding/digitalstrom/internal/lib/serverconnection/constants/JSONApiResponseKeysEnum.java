/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants;

/**
 * The {@link JSONApiResponseKeysEnum} contains digitalSTROM-JSON response keys.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel completely changed and updated only methods remained
 * @author Matthias Siegele completely changed and updated only methods remained
 */
public enum JSONApiResponseKeysEnum {

    // GENERAL
    OK("ok"),
    MESSAGE("message"),
    RESULT("result"),

    // STRUCTURE
    APARTMENT("apartment"),
    DS_METERS("dSMeters"),
    ZONES("zones"),
    CIRCUITS("circuits"),
    DEVICES("devices"),
    GROUPS("groups"),
    REACHABLE_SCENES("reachableScenes"),

    // SENSORS
    // device
    CONSUMPTION("consumption"),
    SENSOR_VALUE("sensorValue"),
    SENSOR_INDEX("sensorIndex"),
    METER_VALUE("meterValue"),
    TYPE("type"),

    // meter sensors
    POWER_CONSUMPTION("powerConsumption"),
    ENERGY_METER_VALUE("energyMeterValue"),
    ENERGY_METER_VALUE_WS("energyMeterValueWs"),
    RESOLUTIONS("resolutions"),
    RESOLUTION("resolution"),
    SERIES("series"),
    UNIT("unit"),
    VALUES("values"),
    DATE("date"),

    // zone/apartment sensors
    SENSOR_TYPE("sensorType"),
    TEMPERATION_VALUE("TemperatureValue"),
    TEMPERATION_VALUE_TIME("TemperatureValueTime"),
    HUMIDITY_VALUE("HumidityValue"),
    HUMIDITY_VALUE_TIME("HumidityValueTime"),
    BRIGHTNESS_VALUE("BrightnessValue"),
    BRIGHTNESS_VALUE_TIME("BrightnessValueTime"),
    CO2_CONCENTRATION_VALUE("CO2ConcentrationValue"),
    CO2_CONCENTRATION_VALUE_TIME("CO2ConcentrationValueTime"),
    SENSORS("sensors"),
    WEATHER_ICON_ID("WeatherIconId"),
    WEATHER_CONDITION_ID("WeatherConditionId"),
    WEATHER_SERVICE_ID("WeatherServiceId"),
    WEATHER_SERVICE_TIME("WeatherServiceTime"),

    // IDs
    DSID("dSID"),
    DSUID("dSUID"),
    DSID_LOWER_CASE("dsid"),
    METER_DSID("meterDSID"),
    ZONE_ID("ZoneID"),
    ZONE_ID_Lower_Z("zoneID"),
    DSUID_LOWER_CASE("dsuid"),
    GROUP_ID("groupID"),
    METER_ID("meterID"),
    ID("id"),
    SCENE_ID("sceneID"),
    NAME("name"),
    DISPLAY_ID("DisplayID"),

    // DEVICE
    // status
    IS_PRESENT("isPresent"),
    IS_VALID("isValid"),
    IS_ON("isOn"),
    PRESENT("present"),
    ON("on"),
    // descriptions
    FUNCTION_ID("functionID"),
    PRODUCT_REVISION("productRevision"),
    PRODUCT_ID("productID"),
    HW_INFO("hwInfo"),
    OUTPUT_MODE("outputMode"),
    BUTTON_ID("buttonID"),
    HAS_TAG("hasTag"),
    TAGS("tags"),
    REVISION_ID("revisionID"),

    // channel
    OUTPUT_CHANNELS("outputChannels"),

    // config
    CLASS("class"),
    INDEX("index"),
    VALUE("value"),

    DONT_CARE("dontCare"),
    LOCAL_PRIO("localPrio"),
    SPECIAL_MODE("specialMode"),
    FLASH_MODE("flashMode"),
    LEDCON_INDEX("ledconIndex"),
    DIM_TIME_INDEX("dimtimeIndex"),
    UP("up"),
    DOWN("down"),

    // event table
    TEST("test"),
    ACTION("action"),
    HYSTERSIS("hysteresis"),
    VALIDITY("validity"),

    // EVENTS
    EVENTS("events"),
    PROPERTIES("properties"),
    EVENT_INDEX("eventIndex"),
    EVENT_NAME("eventName"),

    // SYSTEM & LOGIN
    VERSION("version"),
    TIME("time"),
    TOKEN("token"),
    APPLICATION_TOKEN("applicationToken"),

    SELF("self"),

    // CLIMATE
    IS_CONFIGURED("IsConfigured"),
    CONTROL_MODE("ControlMode"),
    CONTROL_STATE("ControlState"),
    CONTROL_DSUID("ControlDSUID"),
    OPERATION_MODE("OperationMode"),
    TEMPERATURE_VALUE("TemperatureValue"),
    NOMINAL_VALUE("NominalValue"),
    CONTROL_VALUE("ControlValue"),
    TEMPERATURE_VALUE_TIME("TemperatureValueTime"),
    NOMINAL_VALUE_TIME("NominalValueTime"),
    CONTROL_VALUE_TIME("ControlValueTime"),
    CTRL_T_RECENT("CtrlTRecent"),
    CTRL_T_REFERENCE("CtrlTReference"),
    CTRL_T_ERROR("CtrlTError"),
    CTRL_T_ERROR_PREV("CtrlTErrorPrev"),
    CTRL_INTEGRAL("CtrlIntegral"),
    CTRL_YP("CtrlYp"),
    CTRL_YI("CtrlYi"),
    CTRL_YD("CtrlYd"),
    CTRL_Y("CtrlY"),
    CTRL_ANTI_WIND_UP("CtrlAntiWindUp"),
    REFERENCE_ZONE("ReferenceZone"),
    CTRL_OFFSET("CtrlOffset"),
    EMERGENCY_VALUE("EmergencyValue"),
    CTRL_KP("CtrlKp"),
    CTRL_TS("CtrlTs"),
    CTRL_TI("CtrlTi"),
    CTRL_KD("CtrlKd"),
    CTRL_MIN("CtrlImin"),
    CTRL_MAX("CtrlImax"),
    CTRL_Y_MIN("CtrlYmin"),
    CTRL_Y_MAX("CtrlYmax"),
    CTRL_KEEP_FLOOR_WARM("CtrlKeepFloorWarm"),

    // UNDEF
    COLOR_SELECT("colorSelect"),
    MODE_SELECT("modeSelect"),
    DIM_MODE("dimMode"),
    RGB_MODE("rgbMode"),
    GROUP_COLOR_MODE("groupColorMode"),
    SOURCE("source"),
    IS_SCENE_DEVICE("isSceneDevice"),

    // Circuit
    HW_VERSION("hwVersion"),
    HW_VERSION_STRING("hwVersionString"),
    SW_VERSION("swVersion"),
    ARM_SW_VERSION("armSwVersion"),
    DSP_SW_VERSION("dspSwVersion"),
    API_VERSION("apiVersion"),
    HW_NAME("hwName"),
    BUS_MEMBER_TYPE("busMemberType"),
    HAS_DEVICES("hasDevices"),
    HAS_METERING("hasMetering"),
    VDC_CONFIG_URL("VdcConfigURL"),
    VDC_MODEL_UID("VdcModelUID"),
    VDC_HARDWARE_GUID("VdcHardwareGuid"),
    VDC_HARDWARE_MODEL_GUID("VdcHardwareModelGuid"),
    VDC_VENDOR_GUID("VdcVendorGuid"),
    VDC_OEM_GUID("VdcOemGuid"),
    IGNORE_ACTIONS_FROM_NEW_DEVICES("ignoreActionsFromNewDevices"),

    DS_METER_DSID("DSMeterDSID"),
    HW_INFO_UPPER_HW("HWInfo"),
    VALID("valid"),
    VALUE_DS("valueDS"),
    TIMESTAMP("timestamp"),
    SENSOR_INPUTS("sensorInputs"),
    GROUP("group"),
    LAST_CALL_SCENE("lastCalledScene"),
    ANGLE("angle"),

    // Binary inputs
    BINARY_INPUTS("binaryInputs"),
    STATE("state"),
    STATE_VALUE("stateValue"),
    TARGET_GROUP_TYPE("targetGroupType"),
    TARGET_GROUP("targetGroup"),
    INPUT_TYPE("inputType"),
    INPUT_ID("inputId");

    private final String key;

    private JSONApiResponseKeysEnum(String key) {
        this.key = key;
    }

    /**
     * Returns the key.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }
}
