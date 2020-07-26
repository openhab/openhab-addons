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
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link ShellyCoIoTVersion1} implements the parsing for CoIoT version 2
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyCoIoTVersion2 extends ShellyCoIoTProtocol implements ShellyCoIoTInterface {
    private final Logger logger = LoggerFactory.getLogger(ShellyCoIoTVersion2.class);

    public ShellyCoIoTVersion2(String thingId, ShellyBaseHandler thingHandler, Map<String, CoIotDescrBlk> blkMap,
            Map<String, CoIotDescrSen> sensorMap) {
        super(thingId, thingHandler, blkMap, sensorMap);
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
        Integer rIndex = Integer.parseInt(sen.links) + 1;
        String rGroup = getProfile().numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                : CHANNEL_GROUP_RELAY_CONTROL + rIndex;

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
            case "1102": // roler_0: S, roller, open/close/stop
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_DIR, getStringType(s.valueStr));
                break;
            case "1103": // roller_0: S, rollerPos, 0-100, unknown -1
                int pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min((int) value, SHELLY_MAX_ROLLER_POS));
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL,
                        toQuantityType(new Double(SHELLY_MAX_ROLLER_POS - pos), SmartHomeUnits.PERCENT));
                break;
            case "2101": // S, input, 0/1
            case "2201": // S, input, 0/1
                handleInput(sen, s, rGroup, updates);
                break;
            case "2102": // EV, inputEvent, S/SS/SSS/L
            case "2202": // EV, inputEvent
                handleInputEvent(sen, getString(s.valueStr), 0, updates);
                break;
            case "2103": // EVC, inputEventCnt, U16
            case "2203": // EVC, inputEventCnt, U16
                handleInputEvent(sen, "", getInteger((int) s.value), updates);
                break;
            case "3101": // Sensor_0: T, extTemp, C, -55/125; unknown 999
            case "3201": // Sensor_1: T, extTemp, C, -55/125; unknown 999
            case "3301": // Sensor_2:T, extTemp, C, -55/125; unknown 999
                int idx = getExtTempId(sen.id);
                if (idx >= 0) {
                    updateChannel(updates, CHANNEL_GROUP_SENSOR,
                            profile.numTempSensors <= 1 ? CHANNEL_SENSOR_TEMP : CHANNEL_SENSOR_TEMP + sen.links,
                            toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                } else {
                    logger.debug("{}: Unable to get extSensorId {} from {}/{}", thingId, sen.id, sen.type, sen.desc);
                }
                break;
            case "3104": // T, deviceTemp, Celsius -40/300
                if (value == 999) {
                    logger.debug("{}: Reported temperature for external sensor is invalid, check sensor cabling!",
                            thingId);
                }
                updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                        toQuantityType(value, DIGITS_NONE, SIUnits.CELSIUS));
                break;
            case "3102": // T, extTemp, F, -67/257, unknown 999
            case "3105": // T, deviceTemp, Fahrenheit -40/572
                // skip, we use only C
                break;
            case "3108": // S, dwIsOpened, 0/1, -1 ++
                if (value == -1) {
                    logger.debug("{}: Sensor errpr reported, check device, battery, installation", thingId);
                }
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CONTACT,
                        value != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                break;

            case "3117": // S, extInput, 0/1
                handleInput(sen, s, rGroup, updates);
                break;

            case "4101": // relay_0: P, power, W
            case "4201": // relay_1: P, power, W
            case "4102": // roller_0: P, rollerPower, W, 0-2300, unknown -1
                // 3EM uses 1-based meter IDs, other 0-based
                String mGroup = profile.numMeters == 1 ? CHANNEL_GROUP_METER
                        : CHANNEL_GROUP_METER + (profile.isEMeter ? sen.links : rIndex);
                updateChannel(updates, mGroup, CHANNEL_METER_CURRENTWATTS,
                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                updateChannel(updates, mGroup, CHANNEL_LAST_UPDATE, getTimestamp());
                break;

            // TODO
            // case "4103": // relay_0: E, energy, Wmin, U32
            // case "4203": // relay_1: E, energy, Wmin, U32
            // case "4104": // roller_0: E, rollerEnergy, Wmin, U32, -1

            case "6101": // A, overtemp, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_OVERTEMP, true);
                }
                break;
            case "6102": // A, overpower, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_OVERPOWER, true);
                }
                break;
            case "6104": // A, loadError, 0/1
                if (s.value == 1) {
                    thingHandler.postEvent(ALARM_TYPE_LOADERR, true);
                }
                break;
            case "6103": // roller_0: A, rollerStopReason, normal/safety_switch/obstacle/overpower
                reason = getString(s.valueStr);
                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_STOPR, getStringType(reason));
                if (!reason.isEmpty() && !reason.equalsIgnoreCase(SHELLY_API_STOPR_NORMAL)) {
                    thingHandler.postEvent(reason, true);
                }
            case "6106": // A, flood, 0/1, -1
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD,
                        value == 1 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "6110": // A, vibration, 0/1, -1 ++
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VIBRATION,
                        value == 1 ? OnOffType.ON : OnOffType.OFF);
                break;
            case "9102": // EV, wakeupEvent, battery/button/periodic/poweron/sensor/ext_power, "unknown"=unknown
                // TODO merge with REST
                if ((s.valueArray != null) && (s.valueArray.size() > 0)) {
                    reason = getString((String) s.valueArray.get(0));
                    boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP,
                            getStringType(reason));
                    if (changed) {
                        thingHandler.postEvent(reason.toUpperCase(), true);
                    }
                }
                break;
            case "9103": // EVC, cfgChanged, U16
                thingHandler.requestUpdates(1, true); // refresh config
                break;

            default:
                processed = false;
        }
        if (processed) {
            return true;
        }

        switch (sen.type.toLowerCase()) {
            case "t" /* Temperature */:
                switch (sen.desc.toLowerCase()) {
                    case "devicetemp": // Device Temü in C
                        // Device temperature
                        if (isUnit(sen, SHELLY_TEMP_CELSIUS)) {
                            updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                                    toQuantityType(value, DIGITS_NONE, SIUnits.CELSIUS));
                        }
                        break;
                    case "exttemp":
                        if (isUnit(sen, SHELLY_TEMP_CELSIUS)) {
                            int idx = getExtTempId(sen.id);
                            if (idx > 0) {
                                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP + idx,
                                        toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                            }
                        }
                        break;

                    case "temperature": // Sensor Temp
                        if (getString(getProfile().settings.temperatureUnits)
                                .equalsIgnoreCase(SHELLY_TEMP_FAHRENHEIT)) {
                            value = ImperialUnits.FAHRENHEIT.getConverterTo(Units.CELSIUS).convert(getDouble(s.value))
                                    .doubleValue();
                        }
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                                toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                        break;
                    default:
                        logger.debug("{}: Unknown temperatur type: {}", thingId, sen.desc);
                }
                break;
            case "a":
                switch (sen.desc.toLowerCase()) {
                    case "overpower":
                        if (s.value == 1) {
                            thingHandler.postEvent(ALARM_TYPE_OVERPOWER, false);
                        }
                        break;
                    case "overtemp":
                        if (s.value == 1) {
                            thingHandler.postEvent(ALARM_TYPE_OVERTEMP, true);
                        }
                        break;
                    case "loaderror":
                        if (s.value == 1) {
                            thingHandler.postEvent(ALARM_TYPE_LOADERR, true);
                        }
                        break;
                    case "rollerstopreason":
                        // TODO Merge with REST
                        reason = getString(s.valueStr);
                        updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_STOPR,
                                getStringType(reason));
                        if (!reason.isEmpty() && !reason.equalsIgnoreCase(SHELLY_API_STOPR_NORMAL)) {
                            thingHandler.postEvent(reason, true);
                        }
                        break;
                }
                break;

            // TODO: Needs processing
            // case "e": // CoIoT v2: Energy
            // break;

            case "p": // Power/Watt
                // 3EM uses 1-based meter IDs, other 0-based
                String mGroup = profile.numMeters == 1 ? CHANNEL_GROUP_METER
                        : CHANNEL_GROUP_METER + (profile.isEMeter ? sen.links : rIndex);
                updateChannel(updates, mGroup, CHANNEL_METER_CURRENTWATTS,
                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                updateChannel(updates, mGroup, CHANNEL_LAST_UPDATE, getTimestamp());
                break;

            case "i": // CoIoT v2: Current
                if (profile.isEMeter) {
                    updateChannel(updates, rGroup, CHANNEL_EMETER_CURRENT,
                            toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.AMPERE));
                }
                break;

            case "v": // Voltage
                if (profile.isEMeter) {
                    updateChannel(updates, rGroup, CHANNEL_EMETER_VOLTAGE,
                            toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.VOLT));
                }
                break;
            case "c": // CoAP v2: Concentration
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PPM, getDecimal(s.value));
                break;
            case "ev": // CoAP v2: Event
                switch (getString(sen.desc)) {
                    case "wakeupEvent":
                        // TODO merge with REST
                        if ((s.valueArray != null) && (s.valueArray.size() > 0)) {
                            reason = getString((String) s.valueArray.get(0));
                            boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP,
                                    getStringType(reason));
                            if (changed) {
                                thingHandler.postEvent(reason.toUpperCase(), true);
                            }
                        }
                        break;
                    case "cfgChanged":
                        thingHandler.requestUpdates(1, true); // refresh config
                        break;
                    case "inputEvent":
                        handleInputEvent(sen, getString(s.valueStr), 0, updates);
                        break;
                    default:
                        logger.debug("{}: Unknown event of type {}/{}, value={}/{}", thingId, sen.type, sen.desc,
                                s.value, getString(s.valueStr));
                        return false;
                }
                break;
            case "evc": // CoIoT v2: Event count
                switch (sen.desc.toLowerCase()) {
                    case "inputeventcnt":
                        handleInputEvent(sen, "", getInteger((int) s.value), updates);
                        break;
                    case "cfgchanged":
                        thingHandler.requestUpdates(1, true); // refresh config
                        break;
                    default:
                        logger.debug("{}: Unknown event of type {}/{}, value={}/{}", thingId, sen.type, sen.desc,
                                s.value, getString(s.valueStr));
                        return false;
                }
            case "s" /* CatchAll */:
                switch (sen.desc.toLowerCase()) {
                    case "dwisopened":
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CONTACT,
                                s.value != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                        break;
                    case "extinput":
                        handleInput(sen, s, rGroup, updates);
                        break;
                    case "rollerpos":
                        int pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min((int) value, SHELLY_MAX_ROLLER_POS));
                        updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL,
                                toQuantityType(new Double(SHELLY_MAX_ROLLER_POS - pos), SmartHomeUnits.PERCENT));
                        break;
                    default:
                        // Unknown
                        logger.debug("{}: Unknown event of type {}/{}, value={}/{}", thingId, sen.type, sen.desc,
                                s.value, getString(s.valueStr));
                        return false;
                }
                break;

            default:
                // Unknown type
                logger.debug("{}: Unknown event of type {}/{}, value={}/{}", thingId, sen.type, sen.desc, s.value,
                        getString(s.valueStr));
                return false;
        }

        // Value processed
        return true;
    }

    /**
     *
     * Depending on the device type and firmware release there might be bugs or incosistencies in the CoIoT
     * Device Description returned by the discovery request.
     *
     * @param sen Sensor description received from device
     * @return fixed Sensor description (sen)
     */
    @Override
    public CoIotDescrSen fixDescription(CoIotDescrSen sen, Map<String, CoIotDescrBlk> blkMap) {
        return sen;
    }

    private boolean isUnit(CoIotDescrSen sen, String unit) {
        return getString(sen.unit).equalsIgnoreCase(unit);
    }
}
