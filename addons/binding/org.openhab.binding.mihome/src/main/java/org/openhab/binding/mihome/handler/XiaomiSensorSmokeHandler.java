/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * @author Dieter Schmidt
 */
public class XiaomiSensorSmokeHandler extends XiaomiSensorBaseAlarmHandler {

    static {
        ALARM_STATUS_MAP.put(0, "Release alarm");
        ALARM_STATUS_MAP.put(1, "Fire alarm");
        ALARM_STATUS_MAP.put(2, "Analog alarm");
        ALARM_STATUS_MAP.put(64, "Sensitivity fault alarm");
        ALARM_STATUS_MAP.put(32768, "I2C communication failure");
    }

    public XiaomiSensorSmokeHandler(Thing thing) {
        super(thing);
    }
}
