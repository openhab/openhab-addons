/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.enums;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This enum represents the possible scene modes.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
public enum WizLightingModuleType {
    fullColorWifi("ESP01_SHRGB1C_31", THING_TYPE_WIZ_COLOR_BULB),
    tunableWhiteWifi("TBD", THING_TYPE_WIZ_TUNABLE_BULB),
    dimmableWifi("TBD", THING_TYPE_WIZ_DIMMABLE_BULB),
    smartPlug("TBD", THING_TYPE_WIZ_SMART_PLUG);

    private String moduleName;
    private ThingTypeUID thingTypeUID;

    private WizLightingModuleType(final String moduleName, final ThingTypeUID thingTypeUID) {
        this.moduleName = moduleName;
        this.thingTypeUID = thingTypeUID;
    }

    /**
     * Gets the colorMode name for request colorMode
     *
     * @return the colorMode name
     */
    public String getModuleName() {
        return moduleName;
    }

    private static final Map<String, ThingTypeUID> moduleNameMap;
    static {
        moduleNameMap = new HashMap<String, ThingTypeUID>();
        for (WizLightingModuleType v : WizLightingModuleType.values()) {
            moduleNameMap.put(v.moduleName, v.thingTypeUID);
        }
    }

    public static ThingTypeUID getThingTypeUIDFromModuleName(String moduleName) {
        return moduleNameMap.get(moduleName);
    }
}
