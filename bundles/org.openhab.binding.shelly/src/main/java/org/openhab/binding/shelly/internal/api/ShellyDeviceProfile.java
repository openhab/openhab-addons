/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsGlobal;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.discovery.ShellyThingCreator;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
import org.openhab.core.thing.ThingTypeUID;
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
    private static final Pattern GEN1_VERSION_PATTERN = Pattern.compile("v\\d+\\.\\d+\\.\\d+(-[a-z0-9]*)?");
    private static final Pattern GEN2_VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+(-[a-fh-z0-9]*)?");

    public boolean initialized = false; // true when initialized

    public String thingName = "";
    public boolean extFeatures = false;

    public String settingsJson = "";
    public ShellySettingsDevice device = new ShellySettingsDevice();
    public ShellySettingsGlobal settings = new ShellySettingsGlobal();
    public ShellySettingsStatus status = new ShellySettingsStatus();

    public String name = "";
    public boolean discoverable = true;
    public boolean alwaysOn = true;
    public boolean isGen2 = false;
    public boolean isBlu = false;
    public String gateway = "";

    public String hwRev = "";
    public String hwBatchId = "";
    public String fwVersion = "";
    public String fwDate = "";

    public boolean hasRelays = false; // true if it has at least 1 power meter
    public int numRelays = 0; // number of relays/outputs
    public int numRollers = 0; // number of Rollers, usually 1
    public boolean isRoller = false; // true for Shelly2 in roller mode
    public boolean isDimmer = false; // true for a Shelly Dimmer
    public int numInputs = 0; // number of inputs

    public int numMeters = 0;
    public boolean isEMeter = false; // true for ShellyEM/3EM
    public boolean isCB = false; // true for Shelly Pro CB

    public boolean isLight = false; // true if it is a Shelly Bulb/RGBW2
    public boolean isBulb = false; // true only if it is a Bulb
    public boolean isDuo = false; // true only if it is a Duo
    public boolean isRGBW2 = false; // true only if it a RGBW2
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
    public boolean isSmoke = false; // true for Shelly Smoke
    public boolean isWall = false; // true: Shelly Wall Display
    public boolean is3EM = false; // true for Shelly 3EM and Pro 3EM
    public boolean isEM50 = false; // true for Shelly Pro EM50

    public int minTemp = 0; // Bulb/Duo: Min Light Temp
    public int maxTemp = 0; // Bulb/Duo: Max Light Temp

    public int updatePeriod = 2 * UPDATE_SETTINGS_INTERVAL_SECONDS + 10;

    public String coiotEndpoint = "";

    public Map<String, String> irCodes = new HashMap<>(); // Sense: list of stored IR codes

    public ShellyDeviceProfile() {
    }

    public ShellyDeviceProfile initialize(ThingTypeUID thingTypeUID, String jsonIn, ShellySettingsDevice device)
            throws ShellyApiException {
        Gson gson = new Gson();
        initialized = false;
        this.device = device;

        initFromThingType(thingTypeUID);

        String json = jsonIn;
        // It is not guaranteed, that the array entries are in order. Check all
        // possible variants. See openhab#15514.
        if (json.contains("\"ext_temperature\":{\"0\":[{") || json.contains("\"ext_temperature\":{\"1\":[{")
                || json.contains("\"ext_temperature\":{\"2\":[{")) {
            // Shelly UNI uses ext_temperature array, reformat to avoid GSON exception
            json = json.replace("ext_temperature", "ext_temperature_array");
        }
        if (json.contains("\"ext_humidity\":{\"0\":[{")) {
            // Shelly UNI uses ext_humidity array, reformat to avoid GSON exception
            json = json.replace("ext_humidity", "ext_humidity_array");
        }
        settingsJson = json;
        settings = fromJson(gson, json, ShellySettingsGlobal.class);

        // General settings
        if (getString(device.hostname).isEmpty() && !getString(device.mac).isEmpty()) {
            device.hostname = device.mac.length() >= 12 ? "shelly-" + device.mac.toUpperCase().substring(6, 11)
                    : "unknown";
        }
        device.mode = getString(settings.mode).toLowerCase();
        name = getString(settings.name);
        hwRev = settings.hwinfo != null ? getString(settings.hwinfo.hwRevision) : "";
        hwBatchId = settings.hwinfo != null ? getString(settings.hwinfo.batchId.toString()) : "";
        fwDate = substringBefore(device.fw, "-");
        fwVersion = extractFwVersion(device.fw);
        ShellyVersionDTO version = new ShellyVersionDTO();
        extFeatures = version.compare(fwVersion, SHELLY_API_FW_110) >= 0;
        discoverable = (settings.discoverable == null) || settings.discoverable;

        String mode = getString(device.mode);
        isRoller = mode.equalsIgnoreCase(SHELLY_MODE_ROLLER);
        inColor = isLight && mode.equalsIgnoreCase(SHELLY_MODE_COLOR);
        numRelays = !isLight ? getInteger(device.numOutputs) : 0;
        if ((numRelays > 0) && (settings.relays == null)) {
            numRelays = 0;
        }
        hasRelays = (numRelays > 0) || isDimmer;
        numRollers = getInteger(device.numRollers);
        List<ShellySettingsInput> inputs = settings.inputs;
        numInputs = inputs != null ? inputs.size() : hasRelays ? isRoller ? 2 : 1 : 0;

        isEMeter = settings.emeters != null;
        numMeters = !isEMeter ? getInteger(device.numMeters) : getInteger(device.numEMeters);
        if ((numMeters == 0) && isLight) {
            // RGBW2 doesn't report, but has one
            numMeters = inColor ? 1 : getInteger(device.numOutputs);
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

    public void initFromThingType(ThingTypeUID thingTypeUID) {
        isBlu = isBluSeries(thingTypeUID); // e.g. SBBT for BLU Button
        isGen2 = isGeneration2(thingTypeUID);

        isDimmer = GROUP_DIMMER_THING_TYPES.contains(thingTypeUID);
        isBulb = THING_TYPE_SHELLYBULB.equals(thingTypeUID);
        isDuo = GROUP_DUO_THING_TYPES.contains(thingTypeUID);
        isRGBW2 = GROUP_RGBW2_THING_TYPES.contains(thingTypeUID);
        isLight = GROUP_LIGHT_THING_TYPES.contains(thingTypeUID);
        if (isLight) {
            minTemp = isBulb ? MIN_COLOR_TEMP_BULB : MIN_COLOR_TEMP_DUO;
            maxTemp = isBulb ? MAX_COLOR_TEMP_BULB : MAX_COLOR_TEMP_DUO;
        }

        boolean isFlood = GROUP_FLOOD_THING_TYPES.contains(thingTypeUID);
        boolean isGas = GROUP_GAS_THING_TYPES.contains(thingTypeUID);
        boolean isUNI = GROUP_UNI_THING_TYPES.contains(thingTypeUID);
        isSmoke = GROUP_SMOKE_THING_TYPES.contains(thingTypeUID);
        isHT = GROUP_HT_THING_TYPES.contains(thingTypeUID);
        isDW = GROUP_DOORWINDOW_THING_TYPES.contains(thingTypeUID);
        isMotion = GROUP_MOTION_THING_TYPES.contains(thingTypeUID);
        isSense = THING_TYPE_SHELLYSENSE.equals(thingTypeUID);
        isIX = GROUP_IX_THING_TYPES.contains(thingTypeUID);
        isButton = GROUP_BUTTON_THING_TYPES.contains(thingTypeUID);
        isTRV = THING_TYPE_SHELLYTRV.equals(thingTypeUID);
        isWall = GROUP_WALLDISPLAY_THING_TYPES.contains(thingTypeUID);
        is3EM = GROUP_3EM_THING_TYPES.contains(thingTypeUID);
        isEM50 = THING_TYPE_SHELLYPROEM50.equals(thingTypeUID);

        isSensor = isHT || isFlood || isDW || isSmoke || isGas || isButton || isUNI || isMotion || isSense || isTRV
                || isWall;
        hasBattery = isHT || isFlood || isDW || isSmoke || isButton || isMotion || isTRV || isBlu;
        alwaysOn = !hasBattery || (isMotion && !isBlu) || isSense; // true means: device is reachable all the time (no
                                                                   // sleep mode)
    }

    public void initializeInputs(ThingTypeUID thingTypeUID, String btnType) {
        Integer predefinedNumInputs = THING_TYPE_CAP_NUM_INPUTS.get(thingTypeUID);
        if (numInputs == 0 && predefinedNumInputs != null) {
            numInputs = predefinedNumInputs;
        }
        if (numInputs > 0) {
            ShellySettingsInput inputSetting = btnType.isEmpty() ? //
                    new ShellySettingsInput() : new ShellySettingsInput(btnType);
            status.inputs = new ArrayList<>();
            ArrayList<@Nullable ShellySettingsInput> inputs = new ArrayList<>();
            for (int i = 0; i < numInputs; i++) {
                inputs.add(inputSetting);
                status.inputs.add(new ShellyInputState(i));
            }
            settings.inputs = inputs;
        }
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
        } else if (hasRelays) {
            return numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + idx;
        } else if (isRGBW2) {
            List<ShellySettingsRgbwLight> lights = settings.lights;
            return lights == null || lights.size() <= 1 ? CHANNEL_GROUP_LIGHT_CONTROL
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
            // Dimmer has up to 2 inputs to control light
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
        List<ShellySettingsInput> inputs = settings.inputs;
        List<ShellySettingsDimmer> dimmers = settings.dimmers;
        List<ShellySettingsRelay> relays = settings.relays;
        List<ShellySettingsRgbwLight> lights = settings.lights;
        if (isButton) {
            return true;
        } else if (isIX && inputs != null && idx < inputs.size()) {
            ShellySettingsInput input = inputs.get(idx);
            btnType = getString(input.btnType);
        } else if (isDimmer) {
            if (dimmers != null) {
                ShellySettingsDimmer dimmer = dimmers.get(0);
                btnType = getString(dimmer.btnType);
            }
        } else if (relays != null) {
            if (numRelays == 1) {
                ShellySettingsRelay relay = relays.get(0);
                if (relay.btnType != null) {
                    btnType = getString(relay.btnType);
                } else {
                    // Shelly 1L has 2 inputs
                    btnType = idx == 0 ? getString(relay.btnType1) : getString(relay.btnType2);
                }
            } else if (idx < relays.size()) {
                // only one input channel
                ShellySettingsRelay relay = relays.get(idx);
                btnType = getString(relay.btnType);
            }
        } else if (isRGBW2 && lights != null && idx < lights.size()) {
            ShellySettingsRgbwLight light = lights.get(idx);
            btnType = getString(light.btnType);
        }

        return btnType.equalsIgnoreCase(SHELLY_BTNT_MOMENTARY) || btnType.equalsIgnoreCase(SHELLY_BTNT_MOM_ON_RELEASE)
                || btnType.equalsIgnoreCase(SHELLY_BTNT_ONE_BUTTON) || btnType.equalsIgnoreCase(SHELLY_BTNT_TWO_BUTTON)
                || btnType.equalsIgnoreCase(SHELLY_BTNT_DETACHED);
    }

    public int getRollerFav(int id) {
        if (id >= 0 && getBool(settings.favoritesEnabled) && settings.favorites != null
                && id < settings.favorites.size()) {
            return settings.favorites.get(id).pos;
        }
        return -1;
    }

    public String[] getValveProfileList(int valveId) {
        List<ShellyThermnostat> thermostats = settings.thermostats;
        if (isTRV && thermostats != null) {
            int sz = thermostats.size();
            if (valveId <= sz) {
                ShellyThermnostat t = thermostats.get(valveId);
                return t.profileNames;
            }
        }
        return new String[0];
    }

    public String getValueProfile(int valveId, int profileId) {
        int id = profileId;
        List<ShellyThermnostat> thermostats = settings.thermostats;
        if (id <= 0 && thermostats != null) {
            id = thermostats.get(0).profile;
        }
        return "" + id;
    }

    public static String extractFwVersion(@Nullable String version) {
        if (version != null) {
            // fix version e.g.
            // 20210319-122304/v.1.10-Dimmer1-gfd4cc10 (with v.1. instead of v1.)
            // 20220809-125346/v1.12-g99f7e0b (.0 in 1.12.0 missing)
            String vers = version.replace("/v.1.10-", "/v1.10.0-") //
                    .replace("/v1.12-", "/v1.12.0");

            // Extract version from string, e.g. 20210226-091047/v1.10.0-rc2-89-g623b41ec0-master
            Matcher matcher = version.startsWith("v") ? GEN1_VERSION_PATTERN.matcher(vers)
                    : GEN2_VERSION_PATTERN.matcher(vers);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return "";
    }

    public static boolean isGeneration2(String serviceName) {
        return (serviceName.startsWith("shelly") && (serviceName.contains("g3") || serviceName.contains("g4")))
                || isGeneration2(ShellyThingCreator.getThingTypeUID(serviceName));
    }

    public static boolean isGeneration2(ThingTypeUID thingTypeUID) {
        String thingTypeID = thingTypeUID.getId();
        return thingTypeID.startsWith(THING_TYPE_SHELLYPLUS_PREFIX)
                || thingTypeID.startsWith(THING_TYPE_SHELLYPRO_PREFIX) || GROUP_MINI_THING_TYPES.contains(thingTypeUID)
                || GROUP_WALLDISPLAY_THING_TYPES.contains(thingTypeUID) || isBluSeries(thingTypeUID)
                || THING_TYPE_SHELLYPLUSBLUGW.equals(thingTypeUID);
    }

    public static boolean isBluSeries(ThingTypeUID thingTypeUID) {
        return GROUP_BLU_THING_TYPES.contains(thingTypeUID);
    }

    public boolean coiotEnabled() {
        if ((settings.coiot != null) && (settings.coiot.enabled != null)) {
            return settings.coiot.enabled;
        }

        // If device is not yet intialized or the enabled property is missing we assume that CoIoT is enabled
        return true;
    }
}
