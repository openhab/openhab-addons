/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * The {@link VacuumCommand} contains all known commands for the Xiaomi vacuum
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum VacuumCommand {

    START_VACUUM("app_start"),
    STOP_VACUUM("app_stop"),
    START_SPOT("app_spot"),
    PAUSE("app_pause"),
    CHARGE("app_charge"),
    FIND_ME("find_me"),

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

    REMOTE_START("app_rc_start"),
    REMOTE_END("app_rc_end"),
    REMOTE_MOVE("app_rc_move");

    private final String command;

    private VacuumCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

}
