/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;

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
