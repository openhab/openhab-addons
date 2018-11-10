/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    NAME("set_name");

    public String action;

    MethodAction(String action) {
        this.action = action;
    }
}
