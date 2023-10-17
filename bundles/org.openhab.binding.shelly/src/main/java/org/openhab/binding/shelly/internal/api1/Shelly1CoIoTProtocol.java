/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.api1;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.handler.ShellyColorUtils;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Shelly1CoIoTProtocol} implements common functions for the CoIoT implementations
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly1CoIoTProtocol {
    private final Logger logger = LoggerFactory.getLogger(Shelly1CoIoTProtocol.class);
    protected final String thingName;
    protected final ShellyThingInterface thingHandler;
    protected final ShellyDeviceProfile profile;
    protected final ShellyApiInterface api;
    protected final Map<String, CoIotDescrBlk> blkMap;
    protected final Map<String, CoIotDescrSen> sensorMap;
    private final Gson gson = new GsonBuilder().create();

    // Due to the fact that the device reports only the current/last status, but no real events, we need to distinguish
    // between a real update or just a repeated status on periodic updates
    protected int[] lastEventCount = { -1, -1, -1, -1, -1, -1, -1, -1 }; // 4Pro has 4 relays, so 8 should be fine
    protected String[] inputEvent = { "", "", "", "", "", "", "", "" };
    protected String lastWakeup = "";

    public Shelly1CoIoTProtocol(String thingName, ShellyThingInterface thingHandler, Map<String, CoIotDescrBlk> blkMap,
            Map<String, CoIotDescrSen> sensorMap) {
        this.thingName = thingName;
        this.thingHandler = thingHandler;
        this.blkMap = blkMap;
        this.sensorMap = sensorMap;
        this.profile = thingHandler.getProfile();
        this.api = thingHandler.getApi();
    }

    protected boolean handleStatusUpdate(List<CoIotSensor> sensorUpdates, CoIotDescrSen sen, CoIotSensor s,
            Map<String, State> updates, ShellyColorUtils col) {
        // Process status information and convert into channel updates
        int rIndex = getIdFromBlk(sen);
        String rGroup = getProfile().numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                : CHANNEL_GROUP_RELAY_CONTROL + rIndex;

        switch (sen.type.toLowerCase()) {
            case "b": // BatteryLevel +
                updateChannel(updates, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL,
                        toQuantityType(s.value, 0, Units.PERCENT));
                break;
            case "h" /* Humidity */:
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM,
                        toQuantityType(s.value, DIGITS_PERCENT, Units.PERCENT));
                break;
            case "m" /* Motion */:
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION,
                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "l": // Luminosity +
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX,
                        toQuantityType(s.value, DIGITS_LUX, Units.LUX));
                break;
            case "s": // CatchAll
                switch (sen.desc.toLowerCase()) {
                    case "state": // Relay status +
                    case "output":
                        updatePower(profile, updates, rIndex, sen, s, sensorUpdates);
                        break;
                    case "input":
                        handleInput(sen, s, rGroup, updates);
                        break;
                    case "brightness":
                        // already handled by state/output
                        break;
                    case "overtemp": // ++
                        if (s.value == 1) {
                            thingHandler.postEvent(ALARM_TYPE_OVERTEMP, true);
                        }
                        break;
                    case "position":
                        // work around: Roller reports 101% instead max 100
                        double pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min(s.value, SHELLY_MAX_ROLLER_POS));
                        updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL,
                                toQuantityType(SHELLY_MAX_ROLLER_POS - pos, Units.PERCENT));
                        updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS,
                                toQuantityType(pos, Units.PERCENT));
                        break;
                    case "flood":
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD,
                                s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                        break;
                    case "vibration": // DW with FW1.6.5+
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VIBRATION,
                                s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                        if (s.value == 1) {
                            thingHandler.triggerChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ALARM_STATE,
                                    EVENT_TYPE_VIBRATION);
                        }
                        break;
                    case "luminositylevel": // +
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ILLUM, getStringType(s.valueStr));
                        break;
                    case "charger": // Sense
                        updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER,
                                s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                        break;
                    // RGBW2/Bulb
                    case "red":
                        col.setRed((int) s.value);
                        updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED,
                                ShellyColorUtils.toPercent((int) s.value));
                        break;
                    case "green":
                        col.setGreen((int) s.value);
                        updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GREEN,
                                ShellyColorUtils.toPercent((int) s.value));
                        break;
                    case "blue":
                        col.setBlue((int) s.value);
                        updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_BLUE,
                                ShellyColorUtils.toPercent((int) s.value));
                        break;
                    case "white":
                        col.setWhite((int) s.value);
                        updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_WHITE,
                                ShellyColorUtils.toPercent((int) s.value));
                        break;
                    case "gain":
                        col.setGain((int) s.value);
                        updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GAIN,
                                ShellyColorUtils.toPercent((int) s.value, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                        break;
                    case "sensorerror":
                        String sensorError = s.valueStr != null ? getString(s.valueStr) : "" + s.value;
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR, getStringType(sensorError));
                        break;
                    default:
                        // Unknown
                        return false;
                }
                break;

            default:
                // Unknown type
                return false;
        }

        return true;
    }

    public static boolean updateChannel(Map<String, State> updates, String group, String channel, State value) {
        updates.put(mkChannelId(group, channel), value);
        return true;
    }

    protected void handleInput(CoIotDescrSen sen, CoIotSensor s, String rGroup, Map<String, State> updates) {
        int idx = getSensorNumber(sen.desc, sen.id) - 1;
        String iGroup = profile.getInputGroup(idx);
        String iChannel = CHANNEL_INPUT + profile.getInputSuffix(idx);
        updateChannel(updates, iGroup, iChannel, s.value == 0 ? OnOffType.OFF : OnOffType.ON);
    }

    protected void handleInputEvent(CoIotDescrSen sen, String type, int count, int serial, Map<String, State> updates) {
        int idx = getSensorNumber(sen.desc, sen.id) - 1;
        String group = profile.getInputGroup(idx);
        if (count == -1) {
            // event type
            updateChannel(updates, group, CHANNEL_STATUS_EVENTTYPE + profile.getInputSuffix(idx), new StringType(type));
            inputEvent[idx] = type;
        } else {
            // event count
            updateChannel(updates, group, CHANNEL_STATUS_EVENTCOUNT + profile.getInputSuffix(idx), getDecimal(count));
            logger.trace(
                    "{}: Check button[{}] for event trigger (inButtonMode={}, isButton={}, hasBattery={}, serial={}, count={}, lastEventCount[{}]={}",
                    thingName, idx, profile.inButtonMode(idx), profile.isButton, profile.hasBattery, serial, count, idx,
                    lastEventCount[idx]);
            if (profile.inButtonMode(idx) && ((profile.hasBattery && count == 1) || lastEventCount[idx] == -1
                    || count != lastEventCount[idx])) {
                if (!profile.isButton || (profile.isButton && (serial != 0x200))) { // skip duplicate on wake-up
                    logger.debug("{}: Trigger event {}", thingName, inputEvent[idx]);
                    thingHandler.triggerButton(group, idx, inputEvent[idx]);
                }
            }
            lastEventCount[idx] = count;
        }
    }

    /**
     *
     * Handles the combined updated of the brightness channel:
     * brightness$Switch is the OnOffType (power state)
     * brightness&amp;Value is the brightness value
     *
     * @param profile Device profile, required to select the channel group and name
     * @param updates List of updates. updatePower will add brightness$Switch and brightness&amp;Value if changed
     * @param id Sensor id from the update
     * @param sen Sensor description from the update
     * @param s New sensor value
     * @param allUpdates List of updates. This is required, because we need to update both values at the same time
     */
    protected void updatePower(ShellyDeviceProfile profile, Map<String, State> updates, int id, CoIotDescrSen sen,
            CoIotSensor s, List<CoIotSensor> allUpdates) {
        String group = "";
        String channel = CHANNEL_BRIGHTNESS;
        String checkL = ""; // RGBW-white uses 4 different Power, Brightness, VSwitch values
        if (profile.isLight || profile.isDimmer) {
            if (profile.isBulb || profile.inColor) {
                group = CHANNEL_GROUP_LIGHT_CONTROL;
                channel = CHANNEL_LIGHT_POWER;
            } else if (profile.isDuo) {
                group = CHANNEL_GROUP_WHITE_CONTROL;
            } else if (profile.isDimmer) {
                group = CHANNEL_GROUP_RELAY_CONTROL;
            } else if (profile.isRGBW2) {
                checkL = String.valueOf(id); // String.valueOf(id - 1); // id is 1-based, L is 0-based
                group = CHANNEL_GROUP_LIGHT_CHANNEL + id;
                logger.trace("{}: updatePower() for L={}", thingName, checkL);
            }

            // We need to update brightness and on/off state at the same time to avoid "flipping brightness slider" in
            // the UI
            double brightness = -1.0;
            double power = -1.0;
            for (CoIotSensor update : allUpdates) {
                CoIotDescrSen d = fixDescription(sensorMap.get(update.id), blkMap);
                if (!checkL.isEmpty() && !d.links.equals(checkL)) {
                    // continue until we find the correct one
                    continue;
                }
                if ("brightness".equalsIgnoreCase(d.desc)) {
                    brightness = update.value;
                } else if ("output".equalsIgnoreCase(d.desc) || "state".equalsIgnoreCase(d.desc)) {
                    power = update.value;
                }
            }
            if (power != -1) {
                updateChannel(updates, group, channel + "$Switch", power == 1 ? OnOffType.ON : OnOffType.OFF);
            }
            if (brightness != -1) {
                updateChannel(updates, group, channel + "$Value",
                        toQuantityType(power == 1 ? brightness : 0, DIGITS_NONE, Units.PERCENT));
            }
        } else if (profile.hasRelays) {
            group = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + id;
            updateChannel(updates, group, CHANNEL_OUTPUT, s.value == 1 ? OnOffType.ON : OnOffType.OFF);
        } else if (profile.isSensor) {
            // Sensor state
            if (profile.isDW) { // Door Window has item type Contact
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                        s.value != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            } else {
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    /**
     * Find index of Input id, which is required to map to channel name
     *
     * @param sensorDesc D field from sensor update
     * @param sensorId The id from the sensor update
     * @return Index of found entry (+1 will be the suffix for the channel name) or null if sensorId is not found
     */
    protected int getSensorNumber(String sensorDesc, String sensorId) {
        int idx = 0;
        for (Map.Entry<String, CoIotDescrSen> se : sensorMap.entrySet()) {
            CoIotDescrSen sen = se.getValue();
            if (sen.desc.equalsIgnoreCase(sensorDesc)) {
                idx++; // iterate from input1..2..n
            }
            if (sen.id.equalsIgnoreCase(sensorId) && blkMap.containsKey(sen.links)) {
                int id = getIdFromBlk(sen);
                if (id != -1) {
                    return id;
                }
            }
            if (sen.id.equalsIgnoreCase(sensorId)) {
                return idx;
            }
        }
        logger.debug("{}: sensorId {} not found in sensorMap!", thingName, sensorId);
        return -1;
    }

    protected int getIdFromBlk(CoIotDescrSen sen) {
        int idx = -1;
        CoIotDescrBlk blk = blkMap.get(sen.links);
        if (blk != null) {
            String desc = blk.desc.toLowerCase();
            if (desc.startsWith(SHELLY_CLASS_RELAY) || desc.startsWith(SHELLY_CLASS_ROLLER)
                    || desc.startsWith(SHELLY_CLASS_LIGHT) || desc.startsWith(SHELLY_CLASS_EMETER)) {
                if (desc.contains("_")) { // CoAP v2
                    idx = Integer.parseInt(substringAfter(desc, "_"));
                } else { // CoAP v1
                    if (desc.substring(0, 5).equalsIgnoreCase(SHELLY_CLASS_RELAY)) {
                        idx = Integer.parseInt(substringAfter(desc, SHELLY_CLASS_RELAY));
                    }
                    if (desc.substring(0, 6).equalsIgnoreCase(SHELLY_CLASS_ROLLER)) {
                        idx = Integer.parseInt(substringAfter(desc, SHELLY_CLASS_ROLLER));
                    }
                    if (desc.substring(0, SHELLY_CLASS_EMETER.length()).equalsIgnoreCase(SHELLY_CLASS_EMETER)) {
                        idx = Integer.parseInt(substringAfter(desc, SHELLY_CLASS_EMETER));
                    }
                }
                idx = idx + 1; // make it 1-based (sen.L is 0-based)
            }
        }
        return idx;
    }

    /**
     *
     * Get matching sensorId for updates on "External Temperature" - there might be more than 1 sensor.
     *
     * @param sensorId sensorId to map into a channel index
     * @return Index of the corresponding channel (e.g. 0 build temperature1, 1->temperagture2...)
     */
    protected int getExtTempId(String sensorId) {
        int idx = 0;
        for (Map.Entry<String, CoIotDescrSen> se : sensorMap.entrySet()) {
            CoIotDescrSen sen = se.getValue();
            if ("external_temperature".equalsIgnoreCase(sen.desc) || "external temperature c".equalsIgnoreCase(sen.desc)
                    || ("extTemp".equalsIgnoreCase(sen.desc) && !sen.unit.equalsIgnoreCase(SHELLY_TEMP_FAHRENHEIT))) {
                idx++; // iterate from temperature1..2..n
            }
            if (sen.id.equalsIgnoreCase(sensorId)) {
                return idx;
            }
        }
        logger.debug("{}: sensorId {} not found in sensorMap!", thingName, sensorId);
        return -1;
    }

    protected ShellyDeviceProfile getProfile() {
        return profile;
    }

    public CoIotDescrSen fixDescription(@Nullable CoIotDescrSen sen, Map<String, CoIotDescrBlk> blkMap) {
        return sen != null ? sen : new CoIotDescrSen();
    }

    public void completeMissingSensorDefinition(Map<String, CoIotDescrSen> sensorMap) {
    }

    protected void addSensor(Map<String, CoIotDescrSen> sensorMap, String key, String json) {
        try {
            if (!sensorMap.containsKey(key)) {
                CoIotDescrSen sen = gson.fromJson(json, CoIotDescrSen.class);
                if (sen != null) {
                    sensorMap.put(key, sen);
                }
            }
        } catch (JsonSyntaxException e) {
            // should never happen
            logger.trace("Unable to parse sensor definition: {}", json, e);
        }
    }

    public String getLastWakeup() {
        return lastWakeup;
    }
}
