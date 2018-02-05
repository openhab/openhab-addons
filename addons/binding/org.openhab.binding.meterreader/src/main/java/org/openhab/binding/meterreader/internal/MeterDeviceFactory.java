/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import org.openhab.binding.meterreader.internal.helper.ProtocolMode;
import org.openhab.binding.meterreader.internal.iec62056.Iec62056_21MeterReader;
import org.openhab.binding.meterreader.internal.sml.SmlMeterReader;

/**
 * Factory to get the correct device reader for a specific {@link ProtocolMode}
 * 
 * @author MatthiasS
 *
 */
public class MeterDeviceFactory {

    public static MeterDevice<?> getDevice(String mode, String deviceId, String serialPort, byte[] initMessage,
            int baudrate, int baudrateChangeDelay) {
        ProtocolMode protocolMode = ProtocolMode.valueOf(mode.toUpperCase());
        switch (protocolMode) {
            case D:
            case ABC:
                return new Iec62056_21MeterReader(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay,
                        protocolMode);
            case SML:
                return SmlMeterReader.createInstance(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay);
            default:
                return null;
        }
    }
}
