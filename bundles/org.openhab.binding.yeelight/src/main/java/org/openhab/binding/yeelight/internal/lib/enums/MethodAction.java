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
package org.openhab.binding.yeelight.internal.lib.enums;

/**
 * @author Coaster Li - Initial contribution
 */
public enum MethodAction {

    PROP("get_prop"),
    SWITCH("set_power"),
    TOGGLE("toggle"),
    BRIGHTNESS("set_bright"),
    COLORTEMPERATURE("set_ct_abx"),
    HSV("set_hsv"),
    RGB("set_rgb"),
    DEFAULT("set_default"),
    STARTCF("start_cf"),
    STOPCF("setop_cf"),
    SCENE("set_scene"),
    CRON_ADD("cron_add"),
    CRON_GET("cron_get"),
    CRON_DEL("cron_del"),
    ADJUST("set_adjust"),
    MUSIC("set_music"),
    NAME("set_name"),
    BG_HSV("bg_set_hsv"),
    BG_RGB("bg_set_rgb"),
    BG_BRIGHTNESS("bg_set_bright"),
    BG_SWITCH("bg_set_power");

    public String action;

    MethodAction(String action) {
        this.action = action;
    }
}
