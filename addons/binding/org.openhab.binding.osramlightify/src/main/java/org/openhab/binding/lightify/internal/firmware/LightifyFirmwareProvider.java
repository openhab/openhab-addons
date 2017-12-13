/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.firmware;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.firmware.FirmwareProvider;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GATEWAY;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GROUP;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_POWER;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_LIGHT_TUNABLE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_LIGHT_RGBW;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_MOTION_SENSOR;

/**
 * FirmwareProvider for OSRAM/Sylvania Lightify/SMART+ devices.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyFirmwareProvider implements FirmwareProvider {

    public static final Set<Firmware> THING_TYPE_LIGHTIFY_GATEWAY_FIRMWARE = ImmutableSortedSet.of(
        new Firmware.Builder(new FirmwareUID(THING_TYPE_LIGHTIFY_GATEWAY, "1.2.2.0"))
            .withVendor("OSRAM/Sylvania")
            .withDescription("Does not allow probing of white temperature range. Always gives 0-65535.")
            .build(),

        new Firmware.Builder(new FirmwareUID(THING_TYPE_LIGHTIFY_GATEWAY, "1.1.3.53"))
            .withVendor("OSRAM/Sylvania")
            .withDescription("Fixes to overlapped transitions.")
            .build()
    );

    public static final Set<Firmware> THING_TYPE_LIGHTIFY_MOTION_SENSOR_FIRMWARE = ImmutableSortedSet.of(
        new Firmware.Builder(new FirmwareUID(THING_TYPE_LIGHTIFY_MOTION_SENSOR, "1E005310"))
            .withVendor("OSRAM/Sylvania")
            .withDescription("First tested version.")
            .build()
    );

    public static final Set<Firmware> THING_TYPE_LIGHTIFY_LIGHT_RGBW_FIRMWARE = ImmutableSortedSet.of(
        new Firmware.Builder(new FirmwareUID(THING_TYPE_LIGHTIFY_LIGHT_RGBW, "01020510"))
            .withVendor("OSRAM/Sylvania")
            .withDescription("Extends white temperature range to 1501-8000K with RGBW lights.")
            .build(),

        new Firmware.Builder(new FirmwareUID(THING_TYPE_LIGHTIFY_LIGHT_RGBW, "01020412"))
            .withVendor("OSRAM/Sylvania")
            .withDescription("First tested version. Probed white temperature range is 1801-6622K (compared with 1800-6500K in the advertised specification.")
            .build()
    );

    public static final Map<ThingTypeUID, Set<Firmware>> TYPE_TO_FIRMWARE = ImmutableMap
        .<ThingTypeUID, Set<Firmware>>builder()
        .put(THING_TYPE_LIGHTIFY_GATEWAY, THING_TYPE_LIGHTIFY_GATEWAY_FIRMWARE)
        .put(THING_TYPE_LIGHTIFY_MOTION_SENSOR, THING_TYPE_LIGHTIFY_MOTION_SENSOR_FIRMWARE)
        .put(THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_RGBW_FIRMWARE)
        .put(THING_TYPE_LIGHTIFY_LIGHT_TUNABLE, THING_TYPE_LIGHTIFY_LIGHT_RGBW_FIRMWARE)
        .put(THING_TYPE_LIGHTIFY_LIGHT_RGBW, THING_TYPE_LIGHTIFY_LIGHT_RGBW_FIRMWARE)
        .build();

     /**
      * Returns the firmware for the given UID.
      *
      * @param firmwareUID the firmware UID (not null)
      *
      * @return the corresponding firmware or null if no firmware was found
      */
     public Firmware getFirmware(FirmwareUID firmwareUID) {
         return getFirmware(firmwareUID, null);
     }

     /**
      * Returns the firmware for the given UID and the given locale.
      *
      * @param firmwareUID the firmware UID (not null)
      * @param locale the locale to be used (if null then the default locale is to be used)
      *
      * @return the corresponding firmware for the given locale or null if no firmware was found
      */
     public Firmware getFirmware(FirmwareUID firmwareUID, Locale locale) {
        for (Firmware fw : TYPE_TO_FIRMWARE.get(firmwareUID.getThingTypeUID())) {
            if (fw.getUID().equals(firmwareUID)) {
                return fw;
            }
        }

        return null;
     }

     /**
      * Returns the set of available firmwares for the given thing type UID.
      *
      * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
      *
      * @return the set of available firmwares for the given thing type UID (can be null)
      */
    public Set<Firmware> getFirmwares(ThingTypeUID thingTypeUID) {
        return getFirmwares(thingTypeUID, null);
    }

    /**
     * Returns the set of available firmwares for the given thing type UID and the given locale.
     *
     * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
     * @param locale the locale to be used (if null then the default locale is to be used)
     *
     * @return the set of available firmwares for the given thing type UID (can be null)
     */
    public Set<Firmware> getFirmwares(ThingTypeUID thingTypeUID, Locale locale) {
        return TYPE_TO_FIRMWARE.get(thingTypeUID);
    }
}
