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
package org.openhab.binding.elroconnects.internal.handler;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.MOTION_ALARM;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link ElroConnectsMotionSensorHandler} represents the thing handler for an ELRO Connects motion sensor device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsMotionSensorHandler extends ElroConnectsDeviceHandler {

    public ElroConnectsMotionSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void triggerAlarm() {
        triggerChannel(MOTION_ALARM);
    }
}
