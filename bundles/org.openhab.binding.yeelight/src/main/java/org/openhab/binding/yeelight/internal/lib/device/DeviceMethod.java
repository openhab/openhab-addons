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
package org.openhab.binding.yeelight.internal.lib.device;

import org.openhab.binding.yeelight.internal.lib.enums.MethodAction;

/**
 * @author Coaster Li - Initial contribution
 */
public class DeviceMethod {

    public static final String EFFECT_SMOOTH = "smooth";
    public static final String EFFECT_SUDDEN = "sudden";

    public static final int CF_END_STATE_RECOVER = 0;
    public static final int CF_END_STATE_STAY = 1;
    public static final int CF_END_STATE_TURNOFF = 2;

    public static final int CF_ITEM_MODE_COLOR = 1;
    public static final int CF_ITEM_MODE_CT = 2;
    public static final int CF_ITEM_MODE_SLEEP = 7;

    public static final String SCENE_TYPE_COLOR = "color";
    public static final String SCENE_TYPE_HSV = "hsv";
    public static final String SCENE_TYPE_CT = "ct";
    public static final String SCENE_TYPE_CF = "cf";
    public static final String SCENE_TYPE_DELAY = "auto_delay_off";

    public static final String ADJUST_ACTION_INCREASE = "increase";
    public static final String ADJUST_ACTION_DECREASE = "decrease";
    public static final String ADJUST_ACTION_CIRCLE = "circle";

    public static final String ADJUST_PROP_BRIGHT = "bright";
    public static final String ADJUST_PROP_CT = "ct";
    public static final String ADJUST_PROP_COLOR = "color";

    public static final int MUSIC_ACTION_ON = 1;
    public static final int MUSIC_ACTION_OFF = 0;

    private static int sIndex = 0;

    private String mMethodAction;
    private Object[] mMethodParams;
    private String mCustomMethodParams;
    private int mIndex;

    public DeviceMethod(MethodAction action, Object[] params) {
        this.mMethodAction = action.action;
        this.mMethodParams = params;
        this.mCustomMethodParams = "";
        mIndex = ++sIndex;
    }

    public DeviceMethod(String action, String params) {
        this.mMethodAction = action;
        this.mMethodParams = null;
        this.mCustomMethodParams = params;
        mIndex = ++sIndex;
    }

    public String getParamsStr() {
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("{\"id\":").append(mIndex).append(",");
        cmdBuilder.append("\"method\":\"").append(mMethodAction).append("\",");
        cmdBuilder.append("\"params\":[");
        if (mMethodParams != null && mMethodParams.length > 0) {
            for (Object param : mMethodParams) {
                if (param instanceof String) {
                    cmdBuilder.append("\"" + param.toString() + "\"");
                } else {
                    cmdBuilder.append(Integer.parseInt(param.toString()));
                }
                cmdBuilder.append(",");
            }
            // delete last ","
            cmdBuilder.deleteCharAt(cmdBuilder.length() - 1);
        }
        cmdBuilder.append("]}\r\n");
        return cmdBuilder.toString();
    }

    public String getCustomParamsStr() {
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("{\"id\":").append(mIndex).append(",");
        cmdBuilder.append("\"method\":\"").append(mMethodAction).append("\",");
        cmdBuilder.append("\"params\":[").append(mCustomMethodParams).append("]}\r\n");
        return cmdBuilder.toString();
    }

    public String getCmdId() {
        return String.valueOf(mIndex);
    }
}
