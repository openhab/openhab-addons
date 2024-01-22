/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.tools.JsonReaderDelegate;

import com.google.gson.stream.JsonReader;

/**
 * JsonReader which will replace specific names.
 * Abbreviations are defined at
 * https://github.com/home-assistant/core/blob/dev/homeassistant/components/mqtt/abbreviations.py
 * The documentation at https://www.home-assistant.io/integrations/mqtt/#discovery-payload is often out of date
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class MappingJsonReader extends JsonReaderDelegate {

    private static final Map<String, String> ABBREVIATIONS = new HashMap<>();
    private static final Map<String, String> DEVICE_ABBREVIATIONS = new HashMap<>();

    static {
        ABBREVIATIONS.put("act_t", "action_topic");
        ABBREVIATIONS.put("act_tpl", "action_template");
        ABBREVIATIONS.put("act_stat_t", "activity_state_topic");
        ABBREVIATIONS.put("act_val_tpl", "activity_value_template");
        ABBREVIATIONS.put("atype", "automation_type");
        ABBREVIATIONS.put("aux_cmd_t", "aux_command_topic");
        ABBREVIATIONS.put("aux_stat_tpl", "aux_state_template");
        ABBREVIATIONS.put("aux_stat_t", "aux_state_topic");
        ABBREVIATIONS.put("av_tones", "available_tones");
        ABBREVIATIONS.put("avty", "availability");
        ABBREVIATIONS.put("avty_mode", "availability_mode");
        ABBREVIATIONS.put("avty_t", "availability_topic");
        ABBREVIATIONS.put("avty_tpl", "availability_template");
        ABBREVIATIONS.put("b_tpl", "blue_template");
        ABBREVIATIONS.put("bri_cmd_tpl", "brightness_command_template");
        ABBREVIATIONS.put("bri_cmd_t", "brightness_command_topic");
        ABBREVIATIONS.put("bri_scl", "brightness_scale");
        ABBREVIATIONS.put("bri_stat_t", "brightness_state_topic");
        ABBREVIATIONS.put("bri_tpl", "brightness_template");
        ABBREVIATIONS.put("bri_val_tpl", "brightness_value_template");
        ABBREVIATIONS.put("clr_temp_cmd_tpl", "color_temp_command_template");
        ABBREVIATIONS.put("clrm", "color_mode");
        ABBREVIATIONS.put("clrm_stat_t", "color_mode_state_topic");
        ABBREVIATIONS.put("clrm_val_tpl", "color_mode_value_template");
        ABBREVIATIONS.put("clr_temp_cmd_t", "color_temp_command_topic");
        ABBREVIATIONS.put("clr_temp_stat_t", "color_temp_state_topic");
        ABBREVIATIONS.put("clr_temp_tpl", "color_temp_template");
        ABBREVIATIONS.put("clr_temp_val_tpl", "color_temp_value_template");
        ABBREVIATIONS.put("cmd_off_tpl", "command_off_template");
        ABBREVIATIONS.put("cmd_on_tpl", "command_on_template");
        ABBREVIATIONS.put("cmd_t", "command_topic");
        ABBREVIATIONS.put("cmd_tpl", "command_template");
        ABBREVIATIONS.put("cod_arm_req", "code_arm_required");
        ABBREVIATIONS.put("cod_dis_req", "code_disarm_required");
        ABBREVIATIONS.put("cod_form", "code_format");
        ABBREVIATIONS.put("cod_trig_req", "code_trigger_required");
        ABBREVIATIONS.put("cont_type", "content_type");
        ABBREVIATIONS.put("curr_hum_t", "current_humidity_topic");
        ABBREVIATIONS.put("curr_hum_tpl", "current_humidity_template");
        ABBREVIATIONS.put("curr_temp_t", "current_temperature_topic");
        ABBREVIATIONS.put("curr_temp_tpl", "current_temperature_template");
        ABBREVIATIONS.put("dev", "device");
        ABBREVIATIONS.put("dev_cla", "device_class");
        ABBREVIATIONS.put("dir_cmd_t", "direction_command_topic");
        ABBREVIATIONS.put("dir_cmd_tpl", "direction_command_template");
        ABBREVIATIONS.put("dir_stat_t", "direction_state_topic");
        ABBREVIATIONS.put("dir_val_tpl", "direction_value_template");
        ABBREVIATIONS.put("dock_cmd_t", "dock_command_topic");
        ABBREVIATIONS.put("dock_cmd_tpl", "dock_command_template");
        ABBREVIATIONS.put("e", "encoding");
        ABBREVIATIONS.put("en", "enabled_by_default");
        ABBREVIATIONS.put("ent_cat", "entity_category");
        ABBREVIATIONS.put("ent_pic", "entity_picture");
        ABBREVIATIONS.put("evt_typ", "event_types");
        ABBREVIATIONS.put("fanspd_lst", "fan_speed_list");
        ABBREVIATIONS.put("flsh_tlng", "flash_time_long");
        ABBREVIATIONS.put("flsh_tsht", "flash_time_short");
        ABBREVIATIONS.put("fx_cmd_tpl", "effect_command_template");
        ABBREVIATIONS.put("fx_cmd_t", "effect_command_topic");
        ABBREVIATIONS.put("fx_list", "effect_list");
        ABBREVIATIONS.put("fx_stat_t", "effect_state_topic");
        ABBREVIATIONS.put("fx_tpl", "effect_template");
        ABBREVIATIONS.put("fx_val_tpl", "effect_value_template");
        ABBREVIATIONS.put("exp_aft", "expire_after");
        ABBREVIATIONS.put("fan_mode_cmd_tpl", "fan_mode_command_template");
        ABBREVIATIONS.put("fan_mode_cmd_t", "fan_mode_command_topic");
        ABBREVIATIONS.put("fan_mode_stat_tpl", "fan_mode_state_template");
        ABBREVIATIONS.put("fan_mode_stat_t", "fan_mode_state_topic");
        ABBREVIATIONS.put("frc_upd", "force_update");
        ABBREVIATIONS.put("g_tpl", "green_template");
        ABBREVIATIONS.put("hs_cmd_t", "hs_command_topic");
        ABBREVIATIONS.put("hs_cmd_tpl", "hs_command_template");
        ABBREVIATIONS.put("hs_stat_t", "hs_state_topic");
        ABBREVIATIONS.put("hs_val_tpl", "hs_value_template");
        ABBREVIATIONS.put("ic", "icon");
        ABBREVIATIONS.put("img_e", "image_encoding");
        ABBREVIATIONS.put("img_t", "image_topic");
        ABBREVIATIONS.put("init", "initial");
        ABBREVIATIONS.put("hum_cmd_t", "target_humidity_command_topic");
        ABBREVIATIONS.put("hum_cmd_tpl", "target_humidity_command_template");
        ABBREVIATIONS.put("hum_stat_t", "target_humidity_state_topic");
        ABBREVIATIONS.put("hum_state_tpl", "target_humidity_state_template");
        ABBREVIATIONS.put("json_attr", "json_attributes");
        ABBREVIATIONS.put("json_attr_t", "json_attributes_topic");
        ABBREVIATIONS.put("json_attr_tpl", "json_attributes_template");
        ABBREVIATIONS.put("lrst_t", "last_reset_topic");
        ABBREVIATIONS.put("lrst_val_tpl", "last_reset_value_template");
        ABBREVIATIONS.put("max", "max");
        ABBREVIATIONS.put("min", "min");
        ABBREVIATIONS.put("max_hum", "max_humidity");
        ABBREVIATIONS.put("min_hum", "min_humidity");
        ABBREVIATIONS.put("max_mirs", "max_mireds");
        ABBREVIATIONS.put("min_mirs", "min_mireds");
        ABBREVIATIONS.put("max_temp", "max_temp");
        ABBREVIATIONS.put("min_temp", "min_temp");
        ABBREVIATIONS.put("mode", "mode");
        ABBREVIATIONS.put("mode_cmd_tpl", "mode_command_template");
        ABBREVIATIONS.put("mode_cmd_t", "mode_command_topic");
        ABBREVIATIONS.put("mode_stat_t", "mode_state_topic");
        ABBREVIATIONS.put("mode_stat_tpl", "mode_state_template");
        ABBREVIATIONS.put("modes", "modes");
        ABBREVIATIONS.put("name", "name");
        ABBREVIATIONS.put("o", "origin");
        ABBREVIATIONS.put("obj_id", "object_id");
        ABBREVIATIONS.put("off_dly", "off_delay");
        ABBREVIATIONS.put("on_cmd_type", "on_command_type");
        ABBREVIATIONS.put("ops", "options");
        ABBREVIATIONS.put("opt", "optimistic");
        ABBREVIATIONS.put("osc_cmd_t", "oscillation_command_topic");
        ABBREVIATIONS.put("osc_cmd_tpl", "oscillation_command_template");
        ABBREVIATIONS.put("osc_stat_t", "oscillation_state_topic");
        ABBREVIATIONS.put("osc_val_tpl", "oscillation_value_template");
        ABBREVIATIONS.put("pause_cmd_t", "pause_command_topic");
        ABBREVIATIONS.put("pause_mw_cmd_tpl", "pause_command_template");
        ABBREVIATIONS.put("pct_cmd_t", "percentage_command_topic");
        ABBREVIATIONS.put("pct_cmd_tpl", "percentage_command_template");
        ABBREVIATIONS.put("pct_stat_t", "percentage_state_topic");
        ABBREVIATIONS.put("pct_val_tpl", "percentage_value_template");
        ABBREVIATIONS.put("pl", "payload");
        ABBREVIATIONS.put("pl_arm_away", "payload_arm_away");
        ABBREVIATIONS.put("pl_arm_home", "payload_arm_home");
        ABBREVIATIONS.put("pl_arm_nite", "payload_arm_night");
        ABBREVIATIONS.put("pl_arm_vacation", "payload_arm_vacation");
        ABBREVIATIONS.put("pl_arm_custom_b", "payload_arm_custom_bypass");
        ABBREVIATIONS.put("pl_avail", "payload_available");
        ABBREVIATIONS.put("pl_cln_sp", "payload_clean_spot");
        ABBREVIATIONS.put("pl_cls", "payload_close");
        ABBREVIATIONS.put("pl_disarm", "payload_disarm");
        ABBREVIATIONS.put("pl_home", "payload_home");
        ABBREVIATIONS.put("pl_lock", "payload_lock");
        ABBREVIATIONS.put("pl_loc", "payload_locate");
        ABBREVIATIONS.put("pl_not_avail", "payload_not_available");
        ABBREVIATIONS.put("pl_not_home", "payload_not_home");
        ABBREVIATIONS.put("pl_off", "payload_off");
        ABBREVIATIONS.put("pl_on", "payload_on");
        ABBREVIATIONS.put("pl_open", "payload_open");
        ABBREVIATIONS.put("pl_osc_off", "payload_oscillation_off");
        ABBREVIATIONS.put("pl_osc_on", "payload_oscillation_on");
        ABBREVIATIONS.put("pl_paus", "payload_pause");
        ABBREVIATIONS.put("pl_prs", "payload_press");
        ABBREVIATIONS.put("pl_rst", "payload_reset");
        ABBREVIATIONS.put("pl_rst_hum", "payload_reset_humidity");
        ABBREVIATIONS.put("pl_rst_mode", "payload_reset_mode");
        ABBREVIATIONS.put("pl_rst_pct", "payload_reset_percentage");
        ABBREVIATIONS.put("pl_rst_pr_mode", "payload_reset_preset_mode");
        ABBREVIATIONS.put("pl_stop", "payload_stop");
        ABBREVIATIONS.put("pl_strt", "payload_start");
        ABBREVIATIONS.put("pl_ret", "payload_return_to_base");
        ABBREVIATIONS.put("pl_toff", "payload_turn_off");
        ABBREVIATIONS.put("pl_ton", "payload_turn_on");
        ABBREVIATIONS.put("pl_trig", "payload_trigger");
        ABBREVIATIONS.put("pl_unlk", "payload_unlock");
        ABBREVIATIONS.put("pos", "reports_position");
        ABBREVIATIONS.put("pos_clsd", "position_closed");
        ABBREVIATIONS.put("pos_open", "position_open");
        ABBREVIATIONS.put("pow_cmd_t", "power_command_topic");
        ABBREVIATIONS.put("pow_cmd_tpl", "power_command_template");
        ABBREVIATIONS.put("pow_stat_t", "power_state_topic");
        ABBREVIATIONS.put("pow_stat_tpl", "power_state_template");
        ABBREVIATIONS.put("pr_mode_cmd_t", "preset_mode_command_topic");
        ABBREVIATIONS.put("pr_mode_cmd_tpl", "preset_mode_command_template");
        ABBREVIATIONS.put("pr_mode_stat_t", "preset_mode_state_topic");
        ABBREVIATIONS.put("pr_mode_val_tpl", "preset_mode_value_template");
        ABBREVIATIONS.put("pr_modes", "preset_modes");
        ABBREVIATIONS.put("ptrn", "pattern");
        ABBREVIATIONS.put("r_tpl", "red_template");
        ABBREVIATIONS.put("rel_s", "release_summary");
        ABBREVIATIONS.put("rel_u", "release_url");
        ABBREVIATIONS.put("ret", "retain");
        ABBREVIATIONS.put("rgb_cmd_tpl", "rgb_command_template");
        ABBREVIATIONS.put("rgb_cmd_t", "rgb_command_topic");
        ABBREVIATIONS.put("rgb_stat_t", "rgb_state_topic");
        ABBREVIATIONS.put("rgb_val_tpl", "rgb_value_template");
        ABBREVIATIONS.put("rgbw_cmd_tpl", "rgbw_command_template");
        ABBREVIATIONS.put("rgbw_cmd_t", "rgbw_command_topic");
        ABBREVIATIONS.put("rgbw_stat_t", "rgbw_state_topic");
        ABBREVIATIONS.put("rgbw_val_tpl", "rgbw_value_template");
        ABBREVIATIONS.put("rgbww_cmd_tpl", "rgbww_command_template");
        ABBREVIATIONS.put("rgbww_cmd_t", "rgbww_command_topic");
        ABBREVIATIONS.put("rgbww_stat_t", "rgbww_state_topic");
        ABBREVIATIONS.put("rgbww_val_tpl", "rgbww_value_template");
        ABBREVIATIONS.put("send_cmd_t", "send_command_topic");
        ABBREVIATIONS.put("send_if_off", "send_if_off");
        ABBREVIATIONS.put("set_fan_spd_t", "set_fan_speed_topic");
        ABBREVIATIONS.put("set_pos_tpl", "set_position_template");
        ABBREVIATIONS.put("set_pos_t", "set_position_topic");
        ABBREVIATIONS.put("pos_t", "position_topic");
        ABBREVIATIONS.put("pos_tpl", "position_template");
        ABBREVIATIONS.put("spd_rng_min", "speed_range_min");
        ABBREVIATIONS.put("spd_rng_max", "speed_range_max");
        ABBREVIATIONS.put("src_type", "source_type");
        ABBREVIATIONS.put("stat_cla", "state_class");
        ABBREVIATIONS.put("stat_clsd", "state_closed");
        ABBREVIATIONS.put("stat_closing", "state_closing");
        ABBREVIATIONS.put("stat_jam", "state_jammed");
        ABBREVIATIONS.put("stat_off", "state_off");
        ABBREVIATIONS.put("stat_on", "state_on");
        ABBREVIATIONS.put("stat_open", "state_open");
        ABBREVIATIONS.put("stat_opening", "state_opening");
        ABBREVIATIONS.put("stat_stopped", "state_stopped");
        ABBREVIATIONS.put("stat_locked", "state_locked");
        ABBREVIATIONS.put("stat_locking", "state_locking");
        ABBREVIATIONS.put("stat_unlocked", "state_unlocked");
        ABBREVIATIONS.put("stat_unlocking", "state_unlocking");
        ABBREVIATIONS.put("stat_t", "state_topic");
        ABBREVIATIONS.put("stat_tpl", "state_template");
        ABBREVIATIONS.put("stat_val_tpl", "state_value_template");
        ABBREVIATIONS.put("step", "step");
        ABBREVIATIONS.put("strt_mw_cmd_t", "start_mowing_command_topic");
        ABBREVIATIONS.put("strt_mw_cmd_tpl", "start_mowing_command_template");
        ABBREVIATIONS.put("stype", "subtype");
        ABBREVIATIONS.put("sug_dsp_prc", "suggested_display_precision");
        ABBREVIATIONS.put("sup_dur", "support_duration");
        ABBREVIATIONS.put("sup_vol", "support_volume_set");
        ABBREVIATIONS.put("sup_feat", "supported_features");
        ABBREVIATIONS.put("sup_clrm", "supported_color_modes");
        ABBREVIATIONS.put("swing_mode_cmd_tpl", "swing_mode_command_template");
        ABBREVIATIONS.put("swing_mode_cmd_t", "swing_mode_command_topic");
        ABBREVIATIONS.put("swing_mode_stat_tpl", "swing_mode_state_template");
        ABBREVIATIONS.put("swing_mode_stat_t", "swing_mode_state_topic");
        ABBREVIATIONS.put("temp_cmd_tpl", "temperature_command_template");
        ABBREVIATIONS.put("temp_cmd_t", "temperature_command_topic");
        ABBREVIATIONS.put("temp_hi_cmd_tpl", "temperature_high_command_template");
        ABBREVIATIONS.put("temp_hi_cmd_t", "temperature_high_command_topic");
        ABBREVIATIONS.put("temp_hi_stat_tpl", "temperature_high_state_template");
        ABBREVIATIONS.put("temp_hi_stat_t", "temperature_high_state_topic");
        ABBREVIATIONS.put("temp_lo_cmd_tpl", "temperature_low_command_template");
        ABBREVIATIONS.put("temp_lo_cmd_t", "temperature_low_command_topic");
        ABBREVIATIONS.put("temp_lo_stat_tpl", "temperature_low_state_template");
        ABBREVIATIONS.put("temp_lo_stat_t", "temperature_low_state_topic");
        ABBREVIATIONS.put("temp_stat_tpl", "temperature_state_template");
        ABBREVIATIONS.put("temp_stat_t", "temperature_state_topic");
        ABBREVIATIONS.put("temp_unit", "temperature_unit");
        ABBREVIATIONS.put("tilt_clsd_val", "tilt_closed_value");
        ABBREVIATIONS.put("tilt_cmd_t", "tilt_command_topic");
        ABBREVIATIONS.put("tilt_cmd_tpl", "tilt_command_template");
        ABBREVIATIONS.put("tilt_max", "tilt_max");
        ABBREVIATIONS.put("tilt_min", "tilt_min");
        ABBREVIATIONS.put("tilt_opnd_val", "tilt_opened_value");
        ABBREVIATIONS.put("tilt_opt", "tilt_optimistic");
        ABBREVIATIONS.put("tilt_status_t", "tilt_status_topic");
        ABBREVIATIONS.put("tilt_status_tpl", "tilt_status_template");
        ABBREVIATIONS.put("tit", "title");
        ABBREVIATIONS.put("t", "topic");
        ABBREVIATIONS.put("uniq_id", "unique_id");
        ABBREVIATIONS.put("unit_of_meas", "unit_of_measurement");
        ABBREVIATIONS.put("url_t", "url_topic");
        ABBREVIATIONS.put("url_tpl", "url_template");
        ABBREVIATIONS.put("val_tpl", "value_template");
        ABBREVIATIONS.put("whit_cmd_t", "white_command_topic");
        ABBREVIATIONS.put("whit_scl", "white_scale");
        ABBREVIATIONS.put("xy_cmd_t", "xy_command_topic");
        ABBREVIATIONS.put("xy_cmd_tpl", "xy_command_template");
        ABBREVIATIONS.put("xy_stat_t", "xy_state_topic");
        ABBREVIATIONS.put("xy_val_tpl", "xy_value_template");
        ABBREVIATIONS.put("l_ver_t", "latest_version_topic");
        ABBREVIATIONS.put("l_ver_tpl", "latest_version_template");
        ABBREVIATIONS.put("pl_inst", "payload_install");

        DEVICE_ABBREVIATIONS.put("cu", "configuration_url");
        DEVICE_ABBREVIATIONS.put("cns", "connections");
        DEVICE_ABBREVIATIONS.put("hw", "hw_version");
        DEVICE_ABBREVIATIONS.put("ids", "identifiers");
        DEVICE_ABBREVIATIONS.put("mf", "manufacturer");
        DEVICE_ABBREVIATIONS.put("mdl", "model");
        DEVICE_ABBREVIATIONS.put("sa", "suggested_area");
        DEVICE_ABBREVIATIONS.put("sw", "sw_version");
    }

    private final Map<String, String> mapping;

    /**
     *
     * @param delegate
     * @return return a JsonReader which replaces all config abbreviations
     */
    public static MappingJsonReader getConfigMapper(JsonReader delegate) {
        return new MappingJsonReader(JsonReaderDelegate.getDelegate(delegate), ABBREVIATIONS);
    }

    /**
     *
     * @param delegate
     * @return return a JsonReader which replaces all config.device abbreviations
     */
    public static MappingJsonReader getDeviceMapper(JsonReader delegate) {
        return new MappingJsonReader(JsonReaderDelegate.getDelegate(delegate), DEVICE_ABBREVIATIONS);
    }

    private MappingJsonReader(JsonReader delegate, Map<String, String> mapping) {
        super(delegate);
        this.mapping = mapping;
    }

    @Override
    public String nextName() throws IOException {
        String name = super.nextName();
        return mapping.getOrDefault(name, name);
    }
}
