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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsGlobal;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
    private static final Pattern VERSION_PATTERN = Pattern.compile("v\\d+\\.\\d+\\.\\d+(-[a-z0-9]*)?");

    public boolean initialized = false; // true when initialized

    public String thingName = "";
    public String deviceType = "";
    public boolean extFeatures = false;

    public String settingsJson = "";
    public ShellySettingsGlobal settings = new ShellySettingsGlobal();
    public ShellySettingsStatus status = new ShellySettingsStatus();

    public String hostname = "";
    public String name = "";
    public String model = "";
    public String mode = "";
    public boolean discoverable = true;
    public boolean auth = false;
    public boolean alwaysOn = true;
    public boolean isGen2 = false;

    public String hwRev = "";
    public String hwBatchId = "";
    public String mac = "";
    public String fwVersion = "";
    public String fwDate = "";

    public boolean hasRelays = false; // true if it has at least 1 power meter
    public int numRelays = 0; // number of relays/outputs
    public int numRollers = 0; // number of Rollers, usually 1
    public boolean isRoller = false; // true for Shelly2 in roller mode
    public boolean isDimmer = false; // true for a Shelly Dimmer (SHDM-1)
    public int numInputs = 0; // number of inputs

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
    public boolean isMotion = false; // true if thing is a Shelly Sense
    public boolean isHT = false; // true for H&T
    public boolean isDW = false; // true for Door Window sensor
    public boolean isButton = false; // true for a Shelly Button 1
    public boolean isIX = false; // true for a Shelly IX
    public boolean isTRV = false; // true for a Shelly TRV

    public int minTemp = 0; // Bulb/Duo: Min Light Temp
    public int maxTemp = 0; // Bulb/Duo: Max Light Temp

    public int updatePeriod = 2 * UPDATE_SETTINGS_INTERVAL_SECONDS + 10;

    public String coiotEndpoint = "";

    public Map<String, String> irCodes = new HashMap<>(); // Sense: list of stored IR codes

    public ShellyDeviceProfile() {
    }

    public ShellyDeviceProfile initialize(String thingType, String json) throws ShellyApiException {
        Gson gson = new Gson();

        initialized = false;

        initFromThingType(thingType);
        settingsJson = json;
        ShellySettingsGlobal gs = fromJson(gson, json, ShellySettingsGlobal.class);
        settings = gs; // only update when no exception

        // General settings
        name = getString(settings.name);
        deviceType = getString(settings.device.type);
        mac = getString(settings.device.mac);
        hostname = settings.device.hostname != null && !settings.device.hostname.isEmpty()
                ? settings.device.hostname.toLowerCase()
                : "shelly-" + mac.toUpperCase().substring(6, 11);
        mode = getString(settings.mode).toLowerCase();
        hwRev = settings.hwinfo != null ? getString(settings.hwinfo.hwRevision) : "";
        hwBatchId = settings.hwinfo != null ? getString(settings.hwinfo.batchId.toString()) : "";
        fwDate = substringBefore(settings.fw, "/");
        fwVersion = extractFwVersion(settings.fw);
        ShellyVersionDTO version = new ShellyVersionDTO();
        extFeatures = version.compare(fwVersion, SHELLY_API_FW_110) >= 0;
        discoverable = (settings.discoverable == null) || settings.discoverable;

        isRoller = mode.equalsIgnoreCase(SHELLY_MODE_ROLLER);
        inColor = isLight && mode.equalsIgnoreCase(SHELLY_MODE_COLOR);

        numRelays = !isLight ? getInteger(settings.device.numOutputs) : 0;
        if ((numRelays > 0) && (settings.relays == null)) {
            numRelays = 0;
        }
        hasRelays = (numRelays > 0) || isDimmer;
        numRollers = getInteger(settings.device.numRollers);
        numInputs = settings.inputs != null ? settings.inputs.size() : hasRelays ? isRoller ? 2 : 1 : 0;

        isEMeter = settings.emeters != null;
        numMeters = !isEMeter ? getInteger(settings.device.numMeters) : getInteger(settings.device.numEMeters);
        if ((numMeters == 0) && isLight) {
            // RGBW2 doesn't report, but has one
            numMeters = inColor ? 1 : getInteger(settings.device.numOutputs);
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

        isDimmer = deviceType.equalsIgnoreCase(SHELLYDT_DIMMER) || deviceType.equalsIgnoreCase(SHELLYDT_DIMMER2);
        isBulb = thingType.equals(THING_TYPE_SHELLYBULB_STR);
        isDuo = thingType.equals(THING_TYPE_SHELLYDUO_STR) || thingType.equals(THING_TYPE_SHELLYVINTAGE_STR)
                || thingType.equals(THING_TYPE_SHELLYDUORGBW_STR);
        isRGBW2 = thingType.startsWith(THING_TYPE_SHELLYRGBW2_PREFIX);
        isLight = isBulb || isDuo || isRGBW2;
        if (isLight) {
            minTemp = isBulb ? MIN_COLOR_TEMP_BULB : MIN_COLOR_TEMP_DUO;
            maxTemp = isBulb ? MAX_COLOR_TEMP_BULB : MAX_COLOR_TEMP_DUO;
        }

        boolean isFlood = thingType.equals(THING_TYPE_SHELLYFLOOD_STR);
        boolean isSmoke = thingType.equals(THING_TYPE_SHELLYSMOKE_STR);
        boolean isGas = thingType.equals(THING_TYPE_SHELLYGAS_STR);
        boolean isUNI = thingType.equals(THING_TYPE_SHELLYUNI_STR);
        isHT = thingType.equals(THING_TYPE_SHELLYHT_STR) || thingType.equals(THING_TYPE_SHELLYPLUSHT_STR);
        isDW = thingType.equals(THING_TYPE_SHELLYDOORWIN_STR) || thingType.equals(THING_TYPE_SHELLYDOORWIN2_STR);
        isMotion = thingType.startsWith(THING_TYPE_SHELLYMOTION_STR);
        isSense = thingType.equals(THING_TYPE_SHELLYSENSE_STR);
        isIX = thingType.equals(THING_TYPE_SHELLYIX3_STR) || thingType.equals(THING_TYPE_SHELLYPLUSI4_STR)
                || thingType.equals(THING_TYPE_SHELLYPLUSI4DC_STR);
        isButton = thingType.equals(THING_TYPE_SHELLYBUTTON1_STR) || thingType.equals(THING_TYPE_SHELLYBUTTON2_STR);
        isSensor = isHT || isFlood || isDW || isSmoke || isGas || isButton || isUNI || isMotion || isSense || isTRV;
        hasBattery = isHT || isFlood || isDW || isSmoke || isButton || isMotion || isTRV;
        isTRV = thingType.equals(THING_TYPE_SHELLYTRV_STR);

        alwaysOn = !hasBattery || isMotion || isSense; // true means: device is reachable all the time (no sleep mode)
    }

    public void updateFromStatus(ShellySettingsStatus status) {
        if (hasRelays) {
            // Dimmer-2 doesn't report inputs under /settings, only on /status, we need to update that info after init
            if (status.inputs != null) {
                numInputs = status.inputs.size();
            }
        } else if (status.input != null) {
            // RGBW2
            numInputs = 1;
        }
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
            return numRollers <= 1 ? CHANNEL_GROUP_ROL_CONTROL : CHANNEL_GROUP_ROL_CONTROL + idx;
        } else if (isDimmer) {
            return CHANNEL_GROUP_RELAY_CONTROL;
        } else if (hasRelays) {
            return numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + idx;
        } else if (isRGBW2) {
            return settings.lights == null || settings.lights.size() <= 1 ? CHANNEL_GROUP_LIGHT_CONTROL
                    : CHANNEL_GROUP_LIGHT_CHANNEL + idx;
        } else if (isLight) {
            return CHANNEL_GROUP_LIGHT_CONTROL;
        } else if (isButton) {
            return CHANNEL_GROUP_STATUS;
        } else if (isSensor) {
            return CHANNEL_GROUP_SENSOR;
        }

        // e.g. ix3
        return numRelays == 1 ? CHANNEL_GROUP_STATUS : CHANNEL_GROUP_STATUS + idx;
    }

    public String getMeterGroup(int idx) {
        return numMeters > 1 ? CHANNEL_GROUP_METER + (idx + 1) : CHANNEL_GROUP_METER;
    }

    public String getInputGroup(int i) {
        int idx = i + 1; // group names are 1-based
        if (isRGBW2) {
            return CHANNEL_GROUP_LIGHT_CONTROL;
        } else if (isIX) {
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

    public String getInputSuffix(int i) {
        int idx = i + 1; // channel names are 1-based
        if (isRGBW2 || isIX) {
            return ""; // RGBW2 has only 1 channel
        } else if (isRoller || isDimmer) {
            // Roller has 2 relays, but it will be mapped to 1 roller with 2 inputs
            return String.valueOf(idx);
        } else if (hasRelays) {
            return numRelays == 1 && numInputs >= 2 ? String.valueOf(idx) : "";
        }
        return "";
    }

    public boolean inButtonMode(int idx) {
        if (idx < 0) {
            logger.debug("{}: Invalid index {} for inButtonMode()", thingName, idx);
            return false;
        }
        String btnType = "";
        if (isButton) {
            return true;
        } else if (isIX && settings.inputs != null && idx < settings.inputs.size()) {
            ShellySettingsInput input = settings.inputs.get(idx);
            btnType = getString(input.btnType);
        } else if (isDimmer) {
            if (settings.dimmers != null) {
                ShellySettingsDimmer dimmer = settings.dimmers.get(0);
                btnType = dimmer.btnType;
            }
        } else if (settings.relays != null) {
            if (numRelays == 1) {
                ShellySettingsRelay relay = settings.relays.get(0);
                if (relay.btnType != null) {
                    btnType = getString(relay.btnType);
                } else {
                    // Shelly 1L has 2 inputs
                    btnType = idx == 0 ? getString(relay.btnType1) : getString(relay.btnType2);
                }
            } else if (idx < settings.relays.size()) {
                // only one input channel
                ShellySettingsRelay relay = settings.relays.get(idx);
                btnType = getString(relay.btnType);
            }
        } else if (isRGBW2 && (settings.lights != null) && (idx < settings.lights.size())) {
            ShellySettingsRgbwLight light = settings.lights.get(idx);
            btnType = light.btnType;
        }

        return btnType.equalsIgnoreCase(SHELLY_BTNT_MOMENTARY) || btnType.equalsIgnoreCase(SHELLY_BTNT_MOM_ON_RELEASE)
                || btnType.equalsIgnoreCase(SHELLY_BTNT_ONE_BUTTON) || btnType.equalsIgnoreCase(SHELLY_BTNT_TWO_BUTTON)
                || btnType.equalsIgnoreCase(SHELLY_BTNT_DETACHED);
    }

    public int getRollerFav(int id) {
        if ((id >= 0) && getBool(settings.favoritesEnabled) && (settings.favorites != null)
                && (id < settings.favorites.size())) {
            return settings.favorites.get(id).pos;
        }
        return -1;
    }

    public String[] getValveProfileList(int valveId) {
        if (isTRV && settings.thermostats != null && valveId <= settings.thermostats.size()) {
            ShellyThermnostat t = settings.thermostats.get(valveId);
            return t.profileNames;
        }
        return new String[0];
    }

    public String getValueProfile(int valveId, int profileId) {
        int id = profileId;
        if (id <= 0 && settings.thermostats != null) {
            id = settings.thermostats.get(0).profile;
        }
        return "" + id;
    }

    public static String extractFwVersion(@Nullable String version) {
        if (version != null) {
            // fix version e.g. 20210319-122304/v.1.10-Dimmer1-gfd4cc10 (with v.1. instead of v1.)
            String vers = version.replace("/v.1.10-", "/v1.10.0-");

            // Extract version from string, e.g. 20210226-091047/v1.10.0-rc2-89-g623b41ec0-master
            Matcher matcher = VERSION_PATTERN.matcher(vers);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return "";
    }

    public boolean coiotEnabled() {
        if ((settings.coiot != null) && (settings.coiot.enabled != null)) {
            return settings.coiot.enabled;
        }

        // If device is not yet intialized or the enabled property is missing we assume that CoIoT is enabled
        return true;
    }
}
