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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link ShellyThingCreator} maps the device id into the thing type id
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingCreator {
    private static final Map<String, String> thingTypeMapping = new LinkedHashMap<>();
    static {
        // mapping by thing type
        thingTypeMapping.put(SHELLYDT_1PM, THING_TYPE_SHELLY1PM_STR);
        thingTypeMapping.put(SHELLYDT_1, THING_TYPE_SHELLY1_STR);
        thingTypeMapping.put(SHELLYDT_EM3, THING_TYPE_SHELLYEM3_STR);
        thingTypeMapping.put(SHELLYDT_EM, THING_TYPE_SHELLYEM_STR);
        thingTypeMapping.put(SHELLYDT_GAS, THING_TYPE_SHELLYGAS_STR);
        thingTypeMapping.put(SHELLYDT_DUO, THING_TYPE_SHELLYDUO_STR);
        thingTypeMapping.put(SHELLYDT_BULB, THING_TYPE_SHELLYBULB_STR);
        thingTypeMapping.put(SHELLYDT_VINTAGE, THING_TYPE_SHELLYVINTAGE_STR);
        thingTypeMapping.put(SHELLYDT_DIMMER, THING_TYPE_SHELLYDIMMER_STR);
        thingTypeMapping.put(SHELLYDT_DIMMER2, THING_TYPE_SHELLYDIMMER2_STR);
        thingTypeMapping.put(SHELLYDT_IX3, THING_TYPE_SHELLYIX3_STR);
        thingTypeMapping.put(SHELLYDT_BUTTON1, THING_TYPE_SHELLYBUTTON1_STR);

        // mapping by thing type
        thingTypeMapping.put(THING_TYPE_SHELLY1_STR, THING_TYPE_SHELLY1_STR);
        thingTypeMapping.put(THING_TYPE_SHELLY1PM_STR, THING_TYPE_SHELLY1PM_STR);
        thingTypeMapping.put(THING_TYPE_SHELLY4PRO_STR, THING_TYPE_SHELLY4PRO_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYDIMMER2_STR, THING_TYPE_SHELLYDIMMER2_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYDIMMER_STR, THING_TYPE_SHELLYDIMMER_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYIX3_STR, THING_TYPE_SHELLYIX3_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYEM3_STR, THING_TYPE_SHELLYEM3_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYEM_STR, THING_TYPE_SHELLYEM_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYDUO_STR, THING_TYPE_SHELLYDUO_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYBULB_STR, THING_TYPE_SHELLYBULB_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYDUO_STR, THING_TYPE_SHELLYDUO_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYHT_STR, THING_TYPE_SHELLYHT_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYSMOKE_STR, THING_TYPE_SHELLYSMOKE_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYGAS_STR, THING_TYPE_SHELLYGAS_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYFLOOD_STR, THING_TYPE_SHELLYFLOOD_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYDOORWIN_STR, THING_TYPE_SHELLYDOORWIN_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYSENSE_STR, THING_TYPE_SHELLYSENSE_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYEYE_STR, THING_TYPE_SHELLYEYE_STR);
        thingTypeMapping.put(THING_TYPE_SHELLYBUTTON1_STR, THING_TYPE_SHELLYBUTTON1_STR);

        thingTypeMapping.put(THING_TYPE_SHELLYPROTECTED_STR, THING_TYPE_SHELLYPROTECTED_STR);
    }

    public static ThingUID getThingUID(String serviceName, String mode, boolean unknown) {
        String devid = StringUtils.substringAfterLast(serviceName, "-");
        return new ThingUID(!unknown ? getThingTypeUID(serviceName, mode)
                : getThingTypeUID(THING_TYPE_SHELLYPROTECTED_STR + "-" + devid, mode), devid);
    }

    public static ThingTypeUID getThingTypeUID(String serviceName, String mode) {
        return new ThingTypeUID(BINDING_ID, getThingType(serviceName, mode));
    }

    public static ThingTypeUID getUnknownTTUID() {
        return new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPROTECTED_STR);
    }

    public static String getThingType(String hostname, String mode) {
        String name = hostname.toLowerCase();
        String type = StringUtils.substringBefore(name, "-").toLowerCase();
        String devid = StringUtils.substringAfterLast(name, "-");
        if ((devid == null) || (type == null)) {
            throw new IllegalArgumentException("Invalid device name format: " + hostname);
        }

        // First check for special handling
        if (name.startsWith(THING_TYPE_SHELLY25_PREFIX)) { // Shelly v2.5
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY25_RELAY_STR : THING_TYPE_SHELLY25_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY2_PREFIX)) { // Shelly v2
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY2_RELAY_STR : THING_TYPE_SHELLY2_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYPLUG_STR)) {
            // shellyplug-s needs to be mapped to shellyplugs to follow the schema
            // for the thing types: <thing type>-<mode>
            if (name.startsWith(THING_TYPE_SHELLYPLUGS_STR) || name.contains("-s")) {
                return THING_TYPE_SHELLYPLUGS_STR;
            }
            return THING_TYPE_SHELLYPLUG_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYRGBW2_PREFIX)) {
            return mode.equals(SHELLY_MODE_COLOR) ? THING_TYPE_SHELLYRGBW2_COLOR_STR : THING_TYPE_SHELLYRGBW2_WHITE_STR;
        }

        // Check general mapping
        if (thingTypeMapping.containsKey(type)) {
            return thingTypeMapping.get(type);
        }
        return THING_TYPE_SHELLYUNKNOWN_STR;
    }
}
