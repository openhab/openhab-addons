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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_CALIBRATION_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.PowerCalibration;

/**
 * Calibrates the power of a relay device (Circle, Circle+, Stealth). This message is answered by a
 * {@link PowerCalibrationResponseMessage} which contains the {@link PowerCalibration} data.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class PowerCalibrationRequestMessage extends Message {

    public PowerCalibrationRequestMessage(MACAddress macAddress) {
        super(POWER_CALIBRATION_REQUEST, macAddress);
    }
}
