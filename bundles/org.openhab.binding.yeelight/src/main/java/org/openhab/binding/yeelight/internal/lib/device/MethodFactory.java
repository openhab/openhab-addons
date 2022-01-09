/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.openhab.binding.yeelight.internal.lib.enums.MethodAction;

/**
 * @author Coaster Li - Initial contribution
 * @author Nikita Pogudalov - Added DeviceMethod for Ceiling 1
 */
public class MethodFactory {
    public static DeviceMethod buildBrightnessMethd(int brightness, String effect, int duration) {
        return new DeviceMethod(MethodAction.BRIGHTNESS, new Object[] { brightness, effect, duration });
    }

    public static DeviceMethod buildRgbMethod(int color, String effect, int duration) {
        return new DeviceMethod(MethodAction.RGB, new Object[] { color, effect, duration });
    }

    public static DeviceMethod buildCTMethod(int colorTemperature, String effect, int duration) {
        return new DeviceMethod(MethodAction.COLORTEMPERATURE, new Object[] { colorTemperature, effect, duration });
    }

    public static DeviceMethod buildHsvMethod(int hue, int sat, String effect, int duration) {
        return new DeviceMethod(MethodAction.HSV, new Object[] { hue, sat, effect, duration });
    }

    public static DeviceMethod buildToggle() {
        return new DeviceMethod(MethodAction.HSV, null);
    }

    public static DeviceMethod buildSetDefault() {
        return new DeviceMethod(MethodAction.DEFAULT, null);
    }

    public static DeviceMethod buildStartCF(int count, int endAction, ColorFlowItem[] items) {
        if (items == null || items.length < 1) {
            return null;
        }

        String itemStr = "\"";
        StringBuilder builder;
        for (int i = 0; i < items.length; i++) {
            builder = new StringBuilder();
            ColorFlowItem item = items[i];
            builder.append(item.duration).append(",").append(item.mode).append(item.value).append(item.brightness);
            if (i < items.length - 1) {
                builder.append(",");
            }
        }
        itemStr += "\"";

        return new DeviceMethod(MethodAction.STARTCF, new Object[] { count, endAction, itemStr });
    }

    public static DeviceMethod buildStopCf() {
        return new DeviceMethod(MethodAction.STOPCF, null);
    }

    public static DeviceMethod buildScnene(String type, int value, int brightness) {
        return buildScene(type, value, brightness, 0, 0, null);
    }

    /**
     * @param type scene type {@link DeviceMethod#SCENE_TYPE_COLOR}
     *            {@link DeviceMethod#SCENE_TYPE_CT}
     *            {@link DeviceMethod#SCENE_TYPE_DELAY}
     *            {@link DeviceMethod#SCENE_TYPE_HSV}
     *            {@link DeviceMethod#SCENE_TYPE_CF}
     *
     */
    public static DeviceMethod buildScene(String type, int value, int brightness, int count, int endAction,
            ColorFlowItem[] items) {
        if (DeviceMethod.SCENE_TYPE_CF.equals(type) && items == null) {
            throw new IllegalArgumentException("Type is colorFlow, but no flow tuples given");
        }
        Object[] params = null;
        switch (type) {
            case DeviceMethod.SCENE_TYPE_COLOR:
                params = new Object[] { "color", value, brightness };
                break;
            case DeviceMethod.SCENE_TYPE_CT:
                params = new Object[] { "ct", value, brightness };
                break;
            case DeviceMethod.SCENE_TYPE_DELAY:
                params = new Object[] { "auto_delay_off", brightness, value };
                break;
            case DeviceMethod.SCENE_TYPE_HSV:
                params = new Object[] { "hsv", value, brightness };
                break;
            case DeviceMethod.SCENE_TYPE_CF:
                String itemStr = "\"";
                StringBuilder builder;
                for (int i = 0; i < items.length; i++) {
                    builder = new StringBuilder();
                    ColorFlowItem item = items[i];
                    builder.append(item.duration).append(",").append(item.mode).append(item.value)
                            .append(item.brightness);
                    if (i < items.length - 1) {
                        builder.append(",");
                    }
                }
                itemStr += "\"";
                params = new Object[] { "cf", count, endAction, itemStr };
                break;
            default:
                return null;
        }

        return new DeviceMethod(MethodAction.SCENE, params);
    }

    public static DeviceMethod buildCronAdd(int value) {
        return new DeviceMethod(MethodAction.CRON_ADD, new Object[] { 0, value });
    }

    public static DeviceMethod buildCronGet() {
        return new DeviceMethod(MethodAction.CRON_ADD, new Object[] { 0 });
    }

    public static DeviceMethod buildCronDel() {
        return new DeviceMethod(MethodAction.CRON_DEL, new Object[] { 0 });
    }

    public static DeviceMethod buildAdjust(String action, String prop) {
        if (DeviceMethod.ADJUST_PROP_COLOR.equals(prop) && !DeviceMethod.ADJUST_ACTION_CIRCLE.equals(action)) {
            throw new IllegalArgumentException("When prop is COLOR, the action can only be CIRCLE!!!");
        }
        return new DeviceMethod(MethodAction.ADJUST, new Object[] { action, prop });
    }

    public static DeviceMethod buildMusic(int action, String ipAddress, int port) {
        if (action == DeviceMethod.MUSIC_ACTION_ON) {
            return new DeviceMethod(MethodAction.MUSIC, new Object[] { action, ipAddress, port });
        } else {
            return new DeviceMethod(MethodAction.MUSIC, new Object[] { action });
        }
    }

    public static DeviceMethod buildName(String name) {
        return new DeviceMethod(MethodAction.NAME, new Object[] { name });
    }

    public static DeviceMethod buildBackgroundHSVMethod(int hue, int sat, String effect, int duration) {
        return new DeviceMethod(MethodAction.BG_HSV, new Object[] { hue, sat, effect, duration });
    }

    public static DeviceMethod buildBackgroundBrightnessMethd(int brightness, String effect, int duration) {
        return new DeviceMethod(MethodAction.BG_BRIGHTNESS, new Object[] { brightness, effect, duration });
    }

    public static DeviceMethod buildQuery(DeviceBase device) {
        DeviceType type = device.getDeviceType();
        switch (type) {
            case mono:
                return new DeviceMethod(MethodAction.PROP, new Object[] { "power", "name", "bright" });
            case color:
            case stripe:
                return new DeviceMethod(MethodAction.PROP,
                        new Object[] { "power", "name", "bright", "ct", "rgb", "hue", "sat" });
            case ceiling:
            case desklamp:
            case ct_bulb:
                return new DeviceMethod(MethodAction.PROP, new Object[] { "power", "name", "bright", "ct" });
            case ceiling1:
            case ceiling4:
                return new DeviceMethod(MethodAction.PROP, new Object[] { "power", "name", "bright", "ct", "bg_power",
                        "bg_bright", "bg_hue", "bg_sat", "active_mode" });
            default:
                return null;
        }
    }
}
