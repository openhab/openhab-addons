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
package org.openhab.binding.miio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MiIoCommand} contains all known commands for the Xiaomi vacuum and various Mi IO commands for basic
 * devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum MiIoCommand {

    MIIO_INFO("miIO.info"),
    MIIO_WIFI("miIO.wifi_assoc_state"),
    MIIO_ROUTERCONFIG("miIO.miIO.config_router"),

    // Basic device commands
    GET_PROPERTY("get_prop"),
    GET_PROPERTIES("get_properties"),
    GET_DEVICE_PROPERTY_EXP("get_device_prop_exp"),
    GET_DEVICE_PROPERTY("get_device_prop"),
    GET_VALUE("get_value"),
    SET_PROPERTIES("set_properties"),
    SET_MODE_BASIC("set_mode"),
    SET_POWER("set_power"),
    SET_BRIGHT("set_bright"),
    SET_RGB("set_rgb"),
    SET_WIFI_LET("set_wifi_led"),
    SET_FAVORITE("set_level_favorite"),
    ACTION("action"),

    // vacuum commands
    START_VACUUM("app_start"),
    STOP_VACUUM("app_stop"),
    START_SPOT("app_spot"),
    PAUSE("app_pause"),
    CHARGE("app_charge"),
    START_ZONE("app_zoned_clean"),
    FIND_ME("find_me"),
    START_SEGMENT("app_segment_clean"),

    CONSUMABLES_GET("get_consumable"),
    CONSUMABLES_RESET("reset_consumable"),
    CLEAN_SUMMARY_GET("get_clean_summary"),
    CLEAN_RECORD_GET("get_clean_record"),
    CLEAN_RECORD_MAP_GET("get_clean_record_map"),

    GET_MAP("get_map_v1"),
    GET_STATUS("get_status"),
    GET_SERIAL_NUMBER("get_serial_number"),

    DND_GET("get_dnd_timer"),
    DND_SET("set_dnd_timer"),
    DND_CLOSE("close_dnd_timer"),

    TIMER_SET("set_timer"),
    TIMER_UPDATE("upd_timer"),
    TIMER_GET("get_timer"),
    TIMER_DEL("del_timer"),

    SOUND_INSTALL("dnld_install_sound"),
    SOUND_GET_CURRENT("get_current_sound"),
    LOG_UPLOAD_GET("get_log_upload_status"),
    LOG_UPLOAD_ENABLE("enable_log_upload"),

    SET_MODE("set_custom_mode"),
    GET_MODE("get_custom_mode"),
    SET_WATERBOX_MODE("set_water_box_custom_mode"),

    TIMERZONE_SET("set_timezone"),
    TIMERZONE_GET("get_timezone"),
    GATEWAY("gateway"),

    REMOTE_START("app_rc_start"),
    REMOTE_END("app_rc_end"),
    REMOTE_MOVE("app_rc_move"),

    GET_MAP_STATUS("get_map_status"),
    GET_SEGMENT_STATUS("get_segment_status"),
    GET_LED_STATUS("get_led_status"),
    GET_CARPET_MODE("get_carpet_mode"),
    GET_FW_FEATURES("get_fw_features"),
    GET_CUSTOMIZED_CLEAN_MODE("get_customize_clean_mode"),
    GET_MULTI_MAP_LIST("get_multi_maps_list"),
    GET_ROOM_MAPPING("get_room_mapping"),

    // Gateway & child device commands
    GET_ARMING("get_arming"),
    GET_ARMING_TIME("get_arming_time"),
    GET_DOORBEL_VOLUME("get_doorbell_volume"),
    GET_GATEWAY_VOLUME("get_gateway_volume"),
    GET_ALARMING_VOLUME("get_alarming_volume"),
    GET_CLOCK_VOLUME("get_clock_volume"),
    GET_DOORBELL_VOLUME("get_doorbell_volume"),
    GET_ARM_WAIT_TIME("get_arm_wait_time"),
    ALARM_TIME_LEN("alarm_time_len"),
    EN_ALARM_LIGHT("en_alarm_light"),
    GET_CORRIDOR_ON_TIME("get_corridor_on_time"),
    GET_ZIGBEE_CHANNEL("get_zigbee_channel"),
    GET_RGB("get_rgb"),
    GET_NIGHTLIGHT_RGB("get_night_light_rgb"),
    GET_LUMI_BIND("get_lumi_bind"),
    GET_PROP_PLUG("get_prop_plug"),

    UNKNOWN("");

    private final String command;

    private MiIoCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static MiIoCommand getCommand(String commandString) {
        for (MiIoCommand mioCmd : MiIoCommand.values()) {
            if (mioCmd.getCommand().equals(commandString)) {
                return mioCmd;
            }
        }
        return UNKNOWN;
    }
}
