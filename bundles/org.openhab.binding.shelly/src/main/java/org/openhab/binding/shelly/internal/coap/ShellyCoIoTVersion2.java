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
/**
 * The {@link ShellyCoIoTVersion1} implements the parsing for CoIoT version 1
 *
 * @author Markus Michels - Initial contribution
 */
package org.openhab.binding.shelly.internal.coap;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyCoIoTVersion1} implements the parsing for CoIoT version 2
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyCoIoTVersion2 extends ShellyCoIoTProtocol implements ShellyCoIoTInterface {
    private final Logger logger = LoggerFactory.getLogger(ShellyCoIoTVersion2.class);

    public ShellyCoIoTVersion2(String thingName, ShellyBaseHandler thingHandler, Map<String, CoIotDescrBlk> blkMap,
            Map<String, CoIotDescrSen> sensorMap) {
        super(thingName, thingHandler, blkMap, sensorMap);
    }

    @Override
    public int getVersion() {
        return ShellyCoapJSonDTO.COIOT_VERSION_2;
    }

    /**
     * Process CoIoT status update message. If a status update is received, but the device description has not been
     * received yet a GET is send to query device description.
     *
     * @param devId device id included in the status packet
     * @param payload CoAP payload (Json format), example: {"G":[[0,112,0]]}
     * @param serial Serial for this request. If this the the same as last serial
     *            the update was already sent and processed so this one gets
     *            ignored.
     */
    @Override
    public boolean handleStatusUpdate(List<CoIotSensor> sensorUpdates, CoIotDescrSen sen, CoIotSensor s,
            Map<String, State> updates) {
        // first check the base implementation
        if (super.handleStatusUpdate(sensorUpdates, sen, s, updates)) {
            // process by the base class
            return true;
        }

        // Process status information and convert into channel updates
        // Integer rIndex = Integer.parseInt(sen.links) + 1;
        int rIndex = getIdFromBlk(sen);
        String rGroup = getProfile().numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                : CHANNEL_GROUP_RELAY_CONTROL + rIndex;
        String mGroup = profile.numMeters == 1 ? CHANNEL_GROUP_METER
                : CHANNEL_GROUP_METER + (profile.isEMeter ? getIdFromBlk(sen) : rIndex);

        boolean processed = true;
        double value = getDouble(s.value);
        String reason = "";
        switch (sen.id) {
            case "3103": // H, humidity, 0-100 percent, unknown 999
            case "3106": // L, luminosity, lux, U32, -1
            case "3109": // S, tilt, 0-180deg, -1
            case "3110": // S, luminosityLevel, dark/twilight/bright, "unknown"=unknown
            case "3111": // B, battery, 0-100%, unknown -1
            case "3112": // S, charger, 0/1
            case "3115": // S, sensorError, 0/1
            case "5101": // S, brightness, 1-100%
                // processed by base handler
                break;
            case "6109": // P, overpowerValue, W, U32
            case "9101":
                // Relay: S, mode, relay/roller or
                // Dimmer: S, mode, color/white
                // skip, could check against thing mode...
                break;

            case "1101": // S, output, 0/1
                updatePower(profile, updates, rIndex, sen, s, sensorUpdates);
                break;
            case "1102": // roler_0: S, roller, open/close/stop -> roller state
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_STATE, getStringType(s.valueStr));
                break;
            case "1103": // roller_0: S, rollerPos, 0-100, unknown -1
                int pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min((int) value, SHELLY_MAX_ROLLER_POS));
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL,
                        toQuantityType(new Double(SHELLY_MAX_ROLLER_POS - pos), SmartHomeUnits.PERCENT));
                break;
            case "1105": // S, valvle, closed/opened/not_connected/failure/closing/opening/checking or unbknown
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VALVE, getStringType(s.valueStr));
                break;

            case "2101": // Input_0: S, input, 0/1
            case "2201": // Input_1: S, input, 0/1
            case "2301": // Input_2: S, input, 0/1
            case "2401": // Input_3: S, input, 0/1
                handleInput(sen, s, rGroup, updates);
                break;
            case "2102": // Input_0: EV, inputEvent, S/SS/SSS/L
            case "2202": // Input_1: EV, inputEvent
            case "2302": // Input_2: EV, inputEvent
            case "2402": // Input_3: EV, inputEvent
                handleInputEvent(sen, getString(s.valueStr), -1, updates);
                break;
            case "2103": // EVC, inputEventCnt, U16
            case "2203": // EVC, inputEventCnt, U16
            case "2303": // EVC, inputEventCnt, U16
            case "2403": // EVC, inputEventCnt, U16
                handleInputEvent(sen, "", getInteger((int) s.value), updates);
                break;
            case "3101": // sensor_0: T, extTemp, C, -55/125; unknown 999
            case "3201": // sensor_1: T, extTemp, C, -55/125; unknown 999
            case "3301": // sensor_2: T, extTemp, C, -55/125; unknown 999
                int idx = getExtTempId(sen.id);
                if (idx >= 0) {
                    // H&T, Fllod, DW only have 1 channel, 1/1PM with Addon have up to to 3 sensors
                    String channel = profile.isSensor ? CHANNEL_SENSOR_TEMP : CHANNEL_SENSOR_TEMP + idx;
                    updateChannel(updates, CHANNEL_GROUP_SENSOR, channel,
                            toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                } else {
                    logger.debug("{}: Unable to get extSensorId {} from {}/{}", thingName, sen.id, sen.type, sen.desc);
                }
                break;
            case "3104": // T, deviceTemp, Celsius -40/300; 999=unknown
                updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                        toQuantityType(value, DIGITS_NONE, SIUnits.CELSIUS));
                break;
            case "3102": // sensor_0: T, extTemp, F, -67/257, unknown 999
            case "3202": // sensor_1: T, extTemp, F, -67/257, unknown 999
            case "3302": // sensor_2: T, extTemp, F, -67/257, unknown 999
            case "3105": // T, deviceTemp, Fahrenheit -40/572
                // skip, we use only C
                break;

            case "3107": // C, Gas concentration, U16
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PPM, getDecimal(s.value));
                break;
            case "3108": // DW: S, dwIsOpened, 0/1, -1=unknown
                if (value != -1) {
                    updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CONTACT,
                            value != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                } else {
                    logger.debug("{}: Sensor error reported, check device, battery and installation", thingName);
                }
                break;
            case "3113": // S, sensorOp, warmup/normal/fault
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_SSTATE, getStringType(s.valueStr));
                break;
            case "3114": // S, selfTest, not_completed/completed/running/pending
                updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SELFTTEST, getStringType(s.valueStr));
                break;
            case "3117": // S, extInput, 0/1
                handleInput(sen, s, rGroup, updates);
                break;

            case "4101": // relay_0: P, power, W
            case "4201": // relay_1: P, power, W
            case "4301": // relay_2: P, power, W
            case "4401": // relay_3: P, power, W
            case "4105": // emeter_0: P, power, W
            case "4205": // emeter_1: P, power, W
            case "4305": // emeter_2: P, power, W
            case "4102": // roller_0: P, rollerPower, W, 0-2300, unknown -1
            case "4202": // roller_1: P, rollerPower, W, 0-2300, unknown -1
                updateChannel(updates, mGroup, CHANNEL_METER_CURRENTWATTS,
                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                updateChannel(updates, mGroup, CHANNEL_LAST_UPDATE, getTimestamp());
                break;

            case "4103": // relay_0: E, energy, Wmin, U32
            case "4203": // relay_1: E, energy, Wmin, U32
            case "4303": // relay_2: E, energy, Wmin, U32
            case "4403": // relay_3: E, energy, Wmin, U32
            case "4104": // roller_0: E, rollerEnergy, Wmin, U32, -1
            case "4204": // roller_0: E, rollerEnergy, Wmin, U32, -1
            case "4106": // emeter_0: E, energy, Wh, U32
            case "4206": // emeter_1: E, energy, Wh, U32
            case "4306": // emeter_2: E, energy, Wh, U32
                double total = profile.isEMeter ? s.value / 1000 : s.value / 60 / 1000;
                updateChannel(updates, mGroup, CHANNEL_METER_TOTALKWH,
                        toQuantityType(total, DIGITS_KWH, SmartHomeUnits.KILOWATT_HOUR));
                break;

            case "4107": // emeter_0: E, energyReturned, Wh, U32, -1
            case "4207": // emeter_1: E, energyReturned, Wh, U32, -1
            case "4307": // emeter_2: E, energyReturned, Wh, U32, -1
                updateChannel(updates, mGroup, CHANNEL_EMETER_TOTALRET,
                        toQuantityType(getDouble(s.value) / 1000, DIGITS_KWH, SmartHomeUnits.KILOWATT_HOUR));
                break;

            case "4108": // emeter_0: V, voltage, 0-265V, U32, -1
            case "4208": // emeter_1: V, voltage, 0-265V, U32, -1
            case "4308": // emeter_2: V, voltage, 0-265V, U32, -1
                updateChannel(updates, mGroup, CHANNEL_EMETER_VOLTAGE,
                        toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.VOLT));
                break;

            case "4109": // emeter_0: A, current, 0/120A, -1
            case "4209": // emeter_1: A, current, 0/120A, -1
            case "4309": // emeter_2: A, current, 0/120A, -1
                updateChannel(updates, rGroup, CHANNEL_EMETER_CURRENT,
                        toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.AMPERE));
                break;

            case "4110": // emeter_0: S, powerFactor, 0/1, -1
            case "4210": // emeter_1: S, powerFactor, 0/1, -1
            case "4310": // emeter_2: S, powerFactor, 0/1, -1
                updateChannel(updates, rGroup, CHANNEL_EMETER_PFACTOR, getDecimal(s.value));
                break;

            case "6101": // A, overtemp, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_OVERTEMP, true);
                }
                break;
            case "6102": // relay_0: A, overpower, 0/1
            case "6202": // relay_1: A, overpower, 0/1
            case "6302": // relay_2: A, overpower, 0/1
            case "6402": // relay_3: A, overpower, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_OVERPOWER, true);
                }
                break;
            case "6104": // relay_0: A, loadError, 0/1
            case "6204": // relay_1: A, loadError, 0/1
            case "6304": // relay_2: A, loadError, 0/1
            case "6404": // relay_3: A, loadError, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_LOADERR, true);
                }
                break;
            case "6103": // roller_0: A, rollerStopReason, normal/safety_switch/obstacle/overpower
                reason = getString(s.valueStr);
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_STOPR, getStringType(reason));
                if (!reason.isEmpty() && !reason.equalsIgnoreCase(SHELLY_API_STOPR_NORMAL)) {
                    thingHandler.postEvent("ROLLER_" + reason.toUpperCase(), true);
                }
            case "6106": // A, flood, 0/1, -1
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD,
                        value == 1 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "6108": // A, gas, none/mild/heavy/test or unknown
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ALARM_STATE, getStringType(s.valueStr));
                break;
            case "6110": // A, vibration, 0/1, -1=unknown
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VIBRATION,
                        value == 1 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "9102": // EV, wakeupEvent, battery/button/periodic/poweron/sensor/ext_power, "unknown"=unknown
                thingHandler.updateWakeupReason(s.valueArray);
                break;
            case "9103": // EVC, cfgChanged, U16
                if ((lastCfgCount != -1) && (lastCfgCount != s.value)) {
                    thingHandler.requestUpdates(1, true); // refresh config
                }
                lastCfgCount = (int) s.value;
                break;

            default:
                processed = false;
        }
        return processed;
    }

    @Override
    public CoIotDescrSen fixDescription(CoIotDescrSen sen, Map<String, CoIotDescrBlk> blkMap) {
        return sen;
    }
}
