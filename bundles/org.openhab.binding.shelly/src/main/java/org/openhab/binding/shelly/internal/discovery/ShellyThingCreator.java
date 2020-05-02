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
        String devid = StringUtils.substringAfterLast(name, "-");
        if (devid == null) {
            throw new IllegalArgumentException("Invalid device name format: " + hostname);
        }

        if (name.startsWith(THING_TYPE_SHELLY1PN_STR)) {
            return THING_TYPE_SHELLY1PN_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYEM3_STR)) {
            return THING_TYPE_SHELLYEM3_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYEM_STR)) {
            return THING_TYPE_SHELLYEM_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY1_STR)) {
            return THING_TYPE_SHELLY1_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY25_PREFIX)) { // Shelly v2.5
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY25_RELAY_STR : THING_TYPE_SHELLY25_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY2_PREFIX)) { // Shelly v2
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY2_RELAY_STR : THING_TYPE_SHELLY2_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY4PRO_STR)) {
            return THING_TYPE_SHELLY4PRO_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYPLUG_STR)) {
            // shellyplug-s needs to be mapped to shellyplugs to follow the schema
            // for the thing types: <thing type>-<mode>
            if (name.startsWith(THING_TYPE_SHELLYPLUGS_STR) || name.contains("-s")) {
                return THING_TYPE_SHELLYPLUGS_STR;
            }
            return THING_TYPE_SHELLYPLUG_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYDIMMER_STR)) {
            return THING_TYPE_SHELLYDIMMER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYDUO_STR)) {
            return THING_TYPE_SHELLYDUO_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYBULB_STR)) {
            return THING_TYPE_SHELLYBULB_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYRGBW2_PREFIX)) {
            return mode.equals(SHELLY_MODE_COLOR) ? THING_TYPE_SHELLYRGBW2_COLOR_STR : THING_TYPE_SHELLYRGBW2_WHITE_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYHT_STR)) {
            return THING_TYPE_SHELLYHT_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYSMOKE_STR)) {
            return THING_TYPE_SHELLYSMOKE_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYFLOOD_STR)) {
            return THING_TYPE_SHELLYFLOOD_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYDOORWIN_STR)) {
            return THING_TYPE_SHELLYDOORWIN_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYSENSE_STR)) {
            return THING_TYPE_SHELLYSENSE_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYEYE_STR)) {
            return THING_TYPE_SHELLYEYE_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYPROTECTED_STR)) {
            return THING_TYPE_SHELLYPROTECTED_STR;
        }

        return THING_TYPE_UNKNOWN_STR;
    }
}
