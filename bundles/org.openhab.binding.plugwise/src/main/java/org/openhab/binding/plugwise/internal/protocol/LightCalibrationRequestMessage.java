/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.LIGHT_CALIBRATION_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Calibrates the daylight override boundary of a Scan. The best time to do this is at night when lights are on.
 *
 * @author Wouter Born - Initial contribution
 */
public class LightCalibrationRequestMessage extends Message {

    public LightCalibrationRequestMessage(MACAddress macAddress) {
        super(LIGHT_CALIBRATION_REQUEST, macAddress);
    }
}
