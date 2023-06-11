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
package org.openhab.binding.mihome.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.thing.Thing;

/**
 * Handles the Xiaomi gas sensor device
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiSensorGasHandler extends XiaomiSensorBaseAlarmHandler {

    private static final Map<Integer, String> ALARM_STATUS_MAP = new HashMap<>();

    static {
        ALARM_STATUS_MAP.put(0, "OK");
        ALARM_STATUS_MAP.put(1, "Gas alarm");
        ALARM_STATUS_MAP.put(2, "Analog alarm");
        ALARM_STATUS_MAP.put(64, "Sensitivity fault alarm");
        ALARM_STATUS_MAP.put(32768, "I2C communication failure");
    }

    public XiaomiSensorGasHandler(Thing thing) {
        super(thing);
    }
}
