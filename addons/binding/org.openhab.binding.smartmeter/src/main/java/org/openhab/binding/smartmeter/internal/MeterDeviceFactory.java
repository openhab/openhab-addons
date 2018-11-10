/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.binding.smartmeter.internal.iec62056.Iec62056_21MeterReader;
import org.openhab.binding.smartmeter.internal.sml.SmlMeterReader;

/**
 * Factory to get the correct device reader for a specific {@link ProtocolMode}
 *
 * @author MatthiasS
 *
 */
@NonNullByDefault
public class MeterDeviceFactory {

    public static @Nullable MeterDevice<?> getDevice(Supplier<SerialPortManager> serialPortManagerSupplier, String mode,
            String deviceId, String serialPort, byte @Nullable [] initMessage, int baudrate, int baudrateChangeDelay) {
        ProtocolMode protocolMode = ProtocolMode.valueOf(mode.toUpperCase());
        switch (protocolMode) {
            case D:
            case ABC:
                return new Iec62056_21MeterReader(serialPortManagerSupplier, deviceId, serialPort, initMessage,
                        baudrate, baudrateChangeDelay, protocolMode);
            case SML:
                return SmlMeterReader.createInstance(serialPortManagerSupplier, deviceId, serialPort, initMessage,
                        baudrate, baudrateChangeDelay);
            default:
                return null;
        }
    }
}
