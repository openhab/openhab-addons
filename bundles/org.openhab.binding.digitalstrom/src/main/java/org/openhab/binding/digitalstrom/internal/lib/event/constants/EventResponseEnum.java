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
package org.openhab.binding.digitalstrom.internal.lib.event.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link EventResponseEnum} contains digitalSTROM-Event properties of the events at {@link EventNames}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public enum EventResponseEnum {

    // general
    NAME("Name"),
    PROPERTIES("properties"),
    SOURCE("source"),
    SET("set"),
    DSID("dsid"),
    ZONEID("zoneID"),
    GROUPID("groupID"),
    IS_APARTMENT("isApartment"),
    IS_GROUP("isGroup"),
    IS_DEVICE("isDevice"),
    IS_SERVICE("isService"),

    // scene event
    FORCED("forced"),
    ORIGEN_TOKEN("originToken"),
    CALL_ORIGEN("callOrigin"),
    ORIGEN_DSUID("originDSUID"),
    SCENEID("sceneID"),
    ORIGIN_DEVICEID("originDeviceID"),

    // device/zone sensor value
    SENSOR_VALUE_FLOAT("sensorValueFloat"),
    SENSOR_TYPE("sensorType"),
    SENSOR_VALUE("sensorValue"),
    SENSOR_INDEX("sensorIndex"),

    // state changed
    OLD_VALUE("oldvalue"),
    STATE_NAME("statename"),
    STATE("state"),
    VALUE("value"),

    // operation mode
    ACTIONS("actions"),
    OPERATION_MODE("operationMode"),
    FORCED_UPDATE("forceUpdate"),

    // binary input
    INPUT_TYPE("inputType"),
    INPUT_STATE("inputState"),
    INPUT_INDEX("inputIndex");

    private final String id;
    static final Map<String, EventResponseEnum> EVENT_RESPONSE_FIELDS = new HashMap<>();

    static {
        for (EventResponseEnum ev : EventResponseEnum.values()) {
            EVENT_RESPONSE_FIELDS.put(ev.getId(), ev);
        }
    }

    /**
     * Returns true, if the given property exists at the event properties, otherwise false.
     *
     * @param property to check
     * @return contains property (true = yes | false = no)
     */
    public static boolean containsId(String property) {
        return EVENT_RESPONSE_FIELDS.keySet().contains(property);
    }

    /**
     * Returns the {@link EventResponseEnum} to the given property.
     *
     * @param property to get
     * @return EventPropertyEnum
     */
    public static EventResponseEnum getProperty(String property) {
        return EVENT_RESPONSE_FIELDS.get(property);
    }

    private EventResponseEnum(String id) {
        this.id = id;
    }

    /**
     * Returns the id of this {@link EventResponseEnum}.
     *
     * @return id of this {@link EventResponseEnum}
     */
    public String getId() {
        return id;
    }
}
