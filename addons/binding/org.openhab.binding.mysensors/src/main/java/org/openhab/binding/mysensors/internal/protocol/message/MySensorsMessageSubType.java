/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.internal.protocol.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum of MessageSubTypes and the corresponding id
 *
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageSubType {

    S_DOOR(MySensorsMessageSubTypes.S, 0),
    S_MOTION(MySensorsMessageSubTypes.S, 1),
    S_SMOKE(MySensorsMessageSubTypes.S, 2),
    S_BINARY(MySensorsMessageSubTypes.S, 3),
    S_DIMMER(MySensorsMessageSubTypes.S, 4),
    S_COVER(MySensorsMessageSubTypes.S, 5),
    S_TEMP(MySensorsMessageSubTypes.S, 6),
    S_HUM(MySensorsMessageSubTypes.S, 7),
    S_BARO(MySensorsMessageSubTypes.S, 8),
    S_WIND(MySensorsMessageSubTypes.S, 9),
    S_RAIN(MySensorsMessageSubTypes.S, 10),
    S_UV(MySensorsMessageSubTypes.S, 11),
    S_WEIGHT(MySensorsMessageSubTypes.S, 12),
    S_POWER(MySensorsMessageSubTypes.S, 13),
    S_HEATER(MySensorsMessageSubTypes.S, 14),
    S_DISTANCE(MySensorsMessageSubTypes.S, 15),
    S_LIGHT_LEVEL(MySensorsMessageSubTypes.S, 16),
    S_ARDUINO_NODE(MySensorsMessageSubTypes.S, 17),
    S_ARDUINO_REPEATER_NODE(MySensorsMessageSubTypes.S, 18),
    S_LOCK(MySensorsMessageSubTypes.S, 19),
    S_IR(MySensorsMessageSubTypes.S, 20),
    S_WATER(MySensorsMessageSubTypes.S, 21),
    S_AIR_QUALITY(MySensorsMessageSubTypes.S, 22),
    S_CUSTOM(MySensorsMessageSubTypes.S, 23),
    S_DUST(MySensorsMessageSubTypes.S, 24),
    S_SCENE_CONTROLLER(MySensorsMessageSubTypes.S, 25),
    S_RGB_LIGHT(MySensorsMessageSubTypes.S, 26),
    S_RGBW_LIGHT(MySensorsMessageSubTypes.S, 27),
    S_COLOR_SENSOR(MySensorsMessageSubTypes.S, 28),
    S_HVAC(MySensorsMessageSubTypes.S, 29),
    S_MULTIMETER(MySensorsMessageSubTypes.S, 30),
    S_SPRINKLER(MySensorsMessageSubTypes.S, 31),
    S_WATER_LEAK(MySensorsMessageSubTypes.S, 32),
    S_SOUND(MySensorsMessageSubTypes.S, 33),
    S_VIBRATION(MySensorsMessageSubTypes.S, 34),
    S_MOISTURE(MySensorsMessageSubTypes.S, 35),
    S_INFO(MySensorsMessageSubTypes.S, 36),
    S_GAS(MySensorsMessageSubTypes.S, 37),
    S_GPS(MySensorsMessageSubTypes.S, 38),
    S_WATER_QUALITY(MySensorsMessageSubTypes.S, 39),

    V_TEMP(MySensorsMessageSubTypes.V, 0),
    V_HUM(MySensorsMessageSubTypes.V, 1),
    V_STATUS(MySensorsMessageSubTypes.V, 2),
    V_PERCENTAGE(MySensorsMessageSubTypes.V, 3),
    V_PRESSURE(MySensorsMessageSubTypes.V, 4),
    V_FORECAST(MySensorsMessageSubTypes.V, 5),
    V_RAIN(MySensorsMessageSubTypes.V, 6),
    V_RAINRATE(MySensorsMessageSubTypes.V, 7),
    V_WIND(MySensorsMessageSubTypes.V, 8),
    V_GUST(MySensorsMessageSubTypes.V, 9),
    V_DIRECTION(MySensorsMessageSubTypes.V, 10),
    V_UV(MySensorsMessageSubTypes.V, 11),
    V_WEIGHT(MySensorsMessageSubTypes.V, 12),
    V_DISTANCE(MySensorsMessageSubTypes.V, 13),
    V_IMPEDANCE(MySensorsMessageSubTypes.V, 14),
    V_ARMED(MySensorsMessageSubTypes.V, 15),
    V_TRIPPED(MySensorsMessageSubTypes.V, 16),
    V_WATT(MySensorsMessageSubTypes.V, 17),
    V_KWH(MySensorsMessageSubTypes.V, 18),
    V_SCENE_ON(MySensorsMessageSubTypes.V, 19),
    V_SCENE_OFF(MySensorsMessageSubTypes.V, 20),
    V_HVAC_FLOW_STATE(MySensorsMessageSubTypes.V, 21),
    V_HVAC_SPEED(MySensorsMessageSubTypes.V, 22),
    V_LIGHT_LEVEL(MySensorsMessageSubTypes.V, 23),
    V_VAR1(MySensorsMessageSubTypes.V, 24),
    V_VAR2(MySensorsMessageSubTypes.V, 25),
    V_VAR3(MySensorsMessageSubTypes.V, 26),
    V_VAR4(MySensorsMessageSubTypes.V, 27),
    V_VAR5(MySensorsMessageSubTypes.V, 28),
    V_UP(MySensorsMessageSubTypes.V, 29),
    V_DOWN(MySensorsMessageSubTypes.V, 30),
    V_STOP(MySensorsMessageSubTypes.V, 31),
    V_IR_SEND(MySensorsMessageSubTypes.V, 32),
    V_IR_RECEIVE(MySensorsMessageSubTypes.V, 33),
    V_FLOW(MySensorsMessageSubTypes.V, 34),
    V_VOLUME(MySensorsMessageSubTypes.V, 35),
    V_LOCK_STATUS(MySensorsMessageSubTypes.V, 36),
    V_LEVEL(MySensorsMessageSubTypes.V, 37),
    V_VOLTAGE(MySensorsMessageSubTypes.V, 38),
    V_CURRENT(MySensorsMessageSubTypes.V, 39),
    V_RGB(MySensorsMessageSubTypes.V, 40),
    V_RGBW(MySensorsMessageSubTypes.V, 41),
    V_ID(MySensorsMessageSubTypes.V, 42),
    V_UNIT_PREFIX(MySensorsMessageSubTypes.V, 43),
    V_HVAC_SETPOINT_COOL(MySensorsMessageSubTypes.V, 44),
    V_HVAC_SETPOINT_HEAT(MySensorsMessageSubTypes.V, 45),
    V_HVAC_FLOW_MODE(MySensorsMessageSubTypes.V, 46),
    V_TEXT(MySensorsMessageSubTypes.V, 47),
    V_CUSTOM(MySensorsMessageSubTypes.V, 48),
    V_POSITION(MySensorsMessageSubTypes.V, 49),
    V_IR_RECORD(MySensorsMessageSubTypes.V, 50),
    V_PH(MySensorsMessageSubTypes.V, 51),
    V_ORP(MySensorsMessageSubTypes.V, 52),
    V_EC(MySensorsMessageSubTypes.V, 53),
    V_VAR(MySensorsMessageSubTypes.V, 54),
    V_VA(MySensorsMessageSubTypes.V, 55),
    V_POWER_FACTOR(MySensorsMessageSubTypes.V, 56),

    I_BATTERY_LEVEL(MySensorsMessageSubTypes.I, 0),
    I_TIME(MySensorsMessageSubTypes.I, 1),
    I_VERSION(MySensorsMessageSubTypes.I, 2),
    I_ID_REQUEST(MySensorsMessageSubTypes.I, 3),
    I_ID_RESPONSE(MySensorsMessageSubTypes.I, 4),
    I_INCLUSION_MODE(MySensorsMessageSubTypes.I, 5),
    I_CONFIG(MySensorsMessageSubTypes.I, 6),
    I_FIND_PARENT(MySensorsMessageSubTypes.I, 7),
    I_FIND_PARENT_RESPONSE(MySensorsMessageSubTypes.I, 8),
    I_LOG_MESSAGE(MySensorsMessageSubTypes.I, 9),
    I_CHILDREN(MySensorsMessageSubTypes.I, 10),
    I_SKETCH_NAME(MySensorsMessageSubTypes.I, 11),
    I_SKETCH_VERSION(MySensorsMessageSubTypes.I, 12),
    I_REBOOT(MySensorsMessageSubTypes.I, 13),
    I_GATEWAY_READY(MySensorsMessageSubTypes.I, 14),
    I_REQUEST_SIGNING(MySensorsMessageSubTypes.I, 15),
    I_GET_NONCE(MySensorsMessageSubTypes.I, 16),
    I_GET_NONCE_RESONSE(MySensorsMessageSubTypes.I, 17),
    I_HEARTBEAT_REQUEST(MySensorsMessageSubTypes.I, 18),
    I_PRESENTATION(MySensorsMessageSubTypes.I, 19),
    I_DISCOVER(MySensorsMessageSubTypes.I, 20),
    I_DISCOVER_RESPONSE(MySensorsMessageSubTypes.I, 21),
    I_HEARTBEAT_RESPONSE(MySensorsMessageSubTypes.I, 22),
    I_LOCKED(MySensorsMessageSubTypes.I, 23),
    I_PING(MySensorsMessageSubTypes.I, 24),
    I_PONG(MySensorsMessageSubTypes.I, 25),
    I_REGISTRATION_REQUEST(MySensorsMessageSubTypes.I, 26),
    I_REGISTRATION_RESPONSE(MySensorsMessageSubTypes.I, 27),
    I_DEBUG(MySensorsMessageSubTypes.I, 28),
    I_SIGNAL_REPORT_REQUEST(MySensorsMessageSubTypes.I, 29),
    I_SIGNAL_REPORT_REVERSE(MySensorsMessageSubTypes.I, 30),
    I_SIGNAL_REPORT_RESPONSE(MySensorsMessageSubTypes.I, 31),
    I_PRE_SLEEP_NOTIFICATION(MySensorsMessageSubTypes.I, 32),
    I_POST_SLEEP_NOTIFICATION(MySensorsMessageSubTypes.I, 33);

    private final int id;
    private final MySensorsMessageSubTypes subType;

    private MySensorsMessageSubType(MySensorsMessageSubTypes subType, int id) {
        this.id = id;
        this.subType = subType;
    }

    public int getId() {
        return id;
    }

    private static final Map<Integer, MySensorsMessageSubType> PRESENTATION_MESSAGE_BY_ID = new HashMap<Integer, MySensorsMessageSubType>();
    private static final Map<Integer, MySensorsMessageSubType> SET_REQ_MESSAGE_BY_ID = new HashMap<Integer, MySensorsMessageSubType>();
    private static final Map<Integer, MySensorsMessageSubType> INTERNAL_MESSAGE_BY_ID = new HashMap<Integer, MySensorsMessageSubType>();

    static {
        for (MySensorsMessageSubType e : MySensorsMessageSubType.values()) {
            if (e.subType == MySensorsMessageSubTypes.V) {
                if (SET_REQ_MESSAGE_BY_ID.put(e.getId(), e) != null) {
                    throw new IllegalArgumentException("duplicate id: " + e.getId());
                }
            } else if (e.subType == MySensorsMessageSubTypes.S) {
                if (PRESENTATION_MESSAGE_BY_ID.put(e.getId(), e) != null) {
                    throw new IllegalArgumentException("duplicate id: " + e.getId());
                }
            } else if (e.subType == MySensorsMessageSubTypes.I) {
                if (INTERNAL_MESSAGE_BY_ID.put(e.getId(), e) != null) {
                    throw new IllegalArgumentException("duplicate id: " + e.getId());
                }
            }
        }
    }

    public static MySensorsMessageSubType getSetReqById(int id) {
        return SET_REQ_MESSAGE_BY_ID.get(id);
    }

    public static MySensorsMessageSubType getPresentationById(int id) {
        return PRESENTATION_MESSAGE_BY_ID.get(id);
    }

    public static MySensorsMessageSubType getInternalById(int id) {
        return INTERNAL_MESSAGE_BY_ID.get(id);
    }

}
