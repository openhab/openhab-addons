/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
