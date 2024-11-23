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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Properties of yeelight devices as described in the specification.
 *
 * @see <a href="https://www.yeelight.com/download/Yeelight_Inter-Operation_Spec.pdf"></a>
 *
 * @author Viktor Koop - Initial contribution
 */
public enum YeelightDeviceProperty {
    POWER("power"),
    BRIGHT("bright"),
    CT("ct"),
    RGB("rgb"),
    HUE("hue"),
    SAT("sat"),
    COLOR_MODE("color_mode"),
    FLOWING("flowing"),
    DELAYOFF("delayoff"),
    FLOW_PARAMS("flow_params"),
    MUSIC_ON("music_on"),
    NAME("name"),
    BG_POWER("bg_power"),
    BG_FLOWING("bg_flowing"),
    BG_FLOW_PARAMS("bg_flow_params"),
    BG_CT("bg_ct"),
    BG_LMODE("bg_lmode"),
    BG_BRIGHT("bg_bright"),
    BG_RGB("bg_rgb"),
    BG_HUE("bg_hue"),
    BG_SAT("bg_sat"),
    NL_BR("nl_br"),
    ACTIVE_MODE("active_mode"),
    MAIN_POWER("main_power");

    private String value;

    private static final Map<String, YeelightDeviceProperty> ENUM_MAP;

    static {
        final Map<String, YeelightDeviceProperty> tempMap = new HashMap<>();
        for (YeelightDeviceProperty property : YeelightDeviceProperty.values()) {
            tempMap.put(property.value, property);
        }

        ENUM_MAP = Collections.unmodifiableMap(tempMap);
    }

    YeelightDeviceProperty(String stringValue) {
        this.value = stringValue;
    }

    public String getValue() {
        return value;
    }

    public static YeelightDeviceProperty fromString(String value) {
        return ENUM_MAP.get(value);
    }
}
