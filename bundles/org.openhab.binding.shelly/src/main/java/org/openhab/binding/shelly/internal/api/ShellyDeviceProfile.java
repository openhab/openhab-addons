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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsGlobal;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ShellyDeviceProfile} creates a device profile based on the settings returned from the API's /settings
 * call. This is used to be more dynamic in controlling the device, but also to overcome some issues in the API (e.g.
 * RGBW2 returns "no meter" even it has one)
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyDeviceProfile {
    private final Logger logger = LoggerFactory.getLogger(ShellyDeviceProfile.class);

    public boolean initialized = false; // true when initialized

    public String thingName = "";
    public String deviceType = "";

    public String settingsJson = "";
    public ShellySettingsGlobal settings = new ShellySettingsGlobal();
    public ShellySettingsStatus status = new ShellySettingsStatus();

    public String hostname = "";
    public String mode = "";
    public boolean discoverable = true;

    public String hwRev = "";
    public String hwBatchId = "";
    public String mac = "";
    public String fwId = "";
    public String fwVersion = "";
    public String fwDate = "";

    public boolean hasRelays = false; // true if it has at least 1 power meter
    public int numRelays = 0; // number of relays/outputs
    public int numRollers = 0; // number of Rollers, usually 1
    public boolean isRoller = false; // true for Shelly2 in roller mode
    public boolean isDimmer = false; // true for a Shelly Dimmer (SHDM-1)

    public int numMeters = 0;
    public boolean isEMeter = false; // true for ShellyEM/3EM

    public boolean isLight = false; // true if it is a Shelly Bulb/RGBW2
    public boolean isBulb = false; // true only if it is a Bulb
    public boolean isDuo = false; // true only if it is a Duo
    public boolean isRGBW2 = false; // true only if it a a RGBW2
    public boolean inColor = false; // true if bulb/rgbw2 is in color mode

    public boolean isSensor = false; // true for HT & Smoke
    public boolean hasBattery = false; // true if battery device
    public boolean isSense = false; // true if thing is a Shelly Sense
    public boolean isHT = false; // true for H&T
    public boolean isDW = false; // true for Door Window sensor
    public boolean isButton = false; // true for a Shelly Button 1
    public boolean isIX3 = false; // true for a Shelly IX

    public int minTemp = 0; // Bulb/Duo: Min Light Temp
    public int maxTemp = 0; // Bulb/Duo: Max Light Temp

    public int updatePeriod = 2 * UPDATE_SETTINGS_INTERVAL_SECONDS + 10;

    public Map<String, String> irCodes = new HashMap<>(); // Sense: list of stored IR codes

    public ShellyDeviceProfile() {
    }

    public ShellyDeviceProfile initialize(String thingType, String json) throws ShellyApiException {
        Gson gson = new Gson();

        initialized = false;

        try {
            initFromThingType(thingType);
            settingsJson = json;
            settings = gson.fromJson(json, ShellySettingsGlobal.class);
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            throw new ShellyApiException(
                    thingName + ": Unable to transform settings JSON " + e.toString() + ", json='" + json + "'", e);
        }

        // General settings
        deviceType = getString(settings.device.type);
        mac = getString(settings.device.mac);
        hostname = settings.device.hostname != null && !settings.device.hostname.isEmpty()
                ? settings.device.hostname.toLowerCase()
                : "shelly-" + mac.toUpperCase().substring(6, 11);
        mode = !getString(settings.mode).isEmpty() ? getString(settings.mode).toLowerCase() : "";
        hwRev = settings.hwinfo != null ? getString(settings.hwinfo.hwRevision) : "";
        hwBatchId = settings.hwinfo != null ? getString(settings.hwinfo.batchId.toString()) : "";
        fwDate = substringBefore(settings.fw, "/");
        fwVersion = substringBetween(settings.fw, "/", "@");
        fwId = substringAfter(settings.fw, "@");
        discoverable = (settings.discoverable == null) || settings.discoverable;

        inColor = isLight && mode.equalsIgnoreCase(SHELLY_MODE_COLOR);

        numRelays = !isLight ? getInteger(settings.device.numOutputs) : 0;
        if ((numRelays > 0) && (settings.relays == null)) {
            numRelays = 0;
        }
        isDimmer = deviceType.equalsIgnoreCase(SHELLYDT_DIMMER) || deviceType.equalsIgnoreCase(SHELLYDT_DIMMER2);
        hasRelays = (numRelays > 0) || isDimmer;
        numRollers = getInteger(settings.device.numRollers);

        isEMeter = settings.emeters != null;
        numMeters = !isEMeter ? getInteger(settings.device.numMeters) : getInteger(settings.device.numEMeters);
        if ((numMeters == 0) && isLight) {
            // RGBW2 doesn't report, but has one
            numMeters = inColor ? 1 : getInteger(settings.device.numOutputs);
        }
        isRoller = mode.equalsIgnoreCase(SHELLY_MODE_ROLLER);

        if (settings.sleepMode != null) {
            // Sensor, usally 12h
            updatePeriod = getString(settings.sleepMode.unit).equalsIgnoreCase("m") ? settings.sleepMode.period * 60 // minutes
                    : settings.sleepMode.period * 3600; // hours
            updatePeriod += 600; // give 10min extra
        } else if ((settings.coiot != null) && (settings.coiot.updatePeriod != null)) {
            // Derive from CoAP update interval, usually 2*15+5s=50sec -> 70sec
            updatePeriod = Math.max(UPDATE_SETTINGS_INTERVAL_SECONDS, 3 * getInteger(settings.coiot.updatePeriod)) + 10;
        } else {
            updatePeriod = 2 * UPDATE_SETTINGS_INTERVAL_SECONDS + 10;
        }

        initialized = true;
        return this;
    }

    public boolean containsEventUrl(String eventType) {
        return containsEventUrl(settingsJson, eventType);
    }

    public boolean containsEventUrl(String json, String eventType) {
        String settings = json.toLowerCase();
        return settings.contains((eventType + SHELLY_EVENTURL_SUFFIX).toLowerCase());
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initFromThingType(String name) {
        String thingType = (name.contains("-") ? substringBefore(name, "-") : name).toLowerCase().trim();
        if (thingType.isEmpty()) {
            return;
        }

        isBulb = thingType.equals(THING_TYPE_SHELLYBULB_STR);
        isDuo = thingType.equals(THING_TYPE_SHELLYDUO_STR) || thingType.equals(THING_TYPE_SHELLYVINTAGE_STR);
        isRGBW2 = thingType.startsWith(THING_TYPE_SHELLYRGBW2_PREFIX);
        isLight = isBulb || isDuo || isRGBW2;
        if (isLight) {
            minTemp = isBulb ? MIN_COLOR_TEMP_BULB : MIN_COLOR_TEMP_DUO;
            maxTemp = isBulb ? MAX_COLOR_TEMP_BULB : MAX_COLOR_TEMP_DUO;
        }

        boolean isFlood = thingType.equals(THING_TYPE_SHELLYFLOOD_STR);
        boolean isSmoke = thingType.equals(THING_TYPE_SHELLYSMOKE_STR);
        boolean isGas = thingType.equals(THING_TYPE_SHELLYGAS_STR);
        isHT = thingType.equals(THING_TYPE_SHELLYHT_STR);
        isDW = thingType.equals(THING_TYPE_SHELLYDOORWIN_STR) || thingType.equals(THING_TYPE_SHELLYDOORWIN2_STR);
        isSense = thingType.equals(THING_TYPE_SHELLYSENSE_STR);
        isIX3 = thingType.equals(THING_TYPE_SHELLYIX3_STR);
        isButton = thingType.equals(THING_TYPE_SHELLYBUTTON1_STR);
        isSensor = isHT || isFlood || isDW || isSmoke || isGas || isButton || isSense;
        hasBattery = isHT || isFlood || isDW || isSmoke || isButton; // we assume that Sense is connected to // the
                                                                     // charger
    }

    public String getControlGroup(int i) {
        if (i < 0) {
            logger.debug("{}: Invalid index {} for getControlGroup()", thingName, i);
            return "";
        }
        int idx = i + 1;
        if (isDimmer) {
            return CHANNEL_GROUP_DIMMER_CONTROL;
        } else if (isRoller) {
            return numRollers == 1 ? CHANNEL_GROUP_ROL_CONTROL : CHANNEL_GROUP_ROL_CONTROL + idx;
        } else if (hasRelays) {
            return numRelays == 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + idx;
        } else if (isLight) {
            return numRelays == 1 ? CHANNEL_GROUP_LIGHT_CONTROL : CHANNEL_GROUP_LIGHT_CONTROL + idx;
        } else if (isButton) {
            return CHANNEL_GROUP_STATUS;
        } else if (isSensor) {
            return CHANNEL_GROUP_SENSOR;
        }

        // e.g. ix3
        return numRelays == 1 ? CHANNEL_GROUP_STATUS : CHANNEL_GROUP_STATUS + idx;
    }

    public String getInputGroup(int i) {
        int idx = i + 1; // group names are 1-based
        if (isRGBW2) {
            return CHANNEL_GROUP_LIGHT_CONTROL;
        } else if (isIX3) {
            return CHANNEL_GROUP_STATUS + idx;
        } else if (isButton) {
            return CHANNEL_GROUP_STATUS;
        } else if (isRoller) {
            return numRelays <= 2 ? CHANNEL_GROUP_ROL_CONTROL : CHANNEL_GROUP_ROL_CONTROL + idx;
        } else {
            // Device has 1 input per relay: 0=off, 1+2 depend on switch mode
            return numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + idx;
        }
    }

    public String getInputChannel(int i) {
        int idx = i + 1; // channel names are 1-based
        if (isRGBW2 || isIX3) {
            return CHANNEL_INPUT; // RGBW2 has only 1 channel
        } else if (hasRelays) {
            return CHANNEL_INPUT + idx;
        }
        return CHANNEL_INPUT;
    }

    public boolean inButtonMode(int idx) {
        if (idx < 0) {
            logger.debug("{}: Invalid index {} for inButtonMode()", thingName, idx);
            return false;
        }
        String btnType = "";
        if (isButton) {
            return true;
        } else if (isIX3) {
            if ((settings.inputs != null) && (idx >= 0) && (idx < settings.inputs.size())) {
                ShellySettingsInput input = settings.inputs.get(idx);
                btnType = input.btnType;
            }
        } else if (isDimmer) {
            if ((settings.dimmers != null) && (idx >= 0) && (idx < settings.dimmers.size())) {
                ShellySettingsDimmer dimmer = settings.dimmers.get(idx);
                btnType = dimmer.btnType;
            }
        } else if ((settings.relays != null) && (idx >= 0) && (idx < settings.relays.size())) {
            ShellySettingsRelay relay = settings.relays.get(idx);
            btnType = relay.btnType;
        }

        if (btnType.equals(SHELLY_BTNT_MOMENTARY) || btnType.equals(SHELLY_BTNT_MOM_ON_RELEASE)
                || btnType.equals(SHELLY_BTNT_DETACHED) || btnType.equals(SHELLY_BTNT_ONE_BUTTON)) {
            return true;
        }
        return false;
    }
}
