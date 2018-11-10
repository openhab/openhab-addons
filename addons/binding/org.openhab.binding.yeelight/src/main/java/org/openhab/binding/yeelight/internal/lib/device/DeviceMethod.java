/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal.lib.device;

import org.openhab.binding.yeelight.internal.lib.enums.MethodAction;

/**
 * @author Coaster Li - Initial contribution
 */
public class DeviceMethod {

    public final static String EFFECT_SMOOTH = "smooth";
    public final static String EFFECT_SUDDEN = "sudden";

    public final static int CF_END_STATE_RECOVER = 0;
    public final static int CF_END_STATE_STAY = 1;
    public final static int CF_END_STATE_TURNOFF = 2;

    public final static int CF_ITEM_MODE_COLOR = 1;
    public final static int CF_ITEM_MODE_CT = 2;
    public final static int CF_ITEM_MODE_SLEEP = 7;

    public final static String SCENE_TYPE_COLOR = "color";
    public final static String SCENE_TYPE_HSV = "hsv";
    public final static String SCENE_TYPE_CT = "ct";
    public final static String SCENE_TYPE_CF = "cf";
    public final static String SCENE_TYPE_DELAY = "auto_delay_off";

    public final static String ADJUST_ACTION_INCREASE = "increase";
    public final static String ADJUST_ACTION_DECREASE = "decrease";
    public final static String ADJUST_ACTION_CIRCLE = "circle";

    public final static String ADJUST_PROP_BRIGHT = "bright";
    public final static String ADJUST_PROP_CT = "ct";
    public final static String ADJUST_PROP_COLOR = "color";

    public final static int MUSIC_ACTION_ON = 1;
    public final static int MUSIC_ACTION_OFF = 0;

    private static int sIndex = 0;

    private String mMethodAction;
    private Object[] mMethodParams;
    private int mIndex;

    public DeviceMethod(MethodAction action, Object[] params) {
        this.mMethodAction = action.action;
        this.mMethodParams = params;
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

    public String getCmdId() {
        return String.valueOf(mIndex);
    }

}
