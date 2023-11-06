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
package org.openhab.binding.smartmeter.internal;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.binding.smartmeter.internal.iec62056.Iec62056_21MeterReader;
import org.openhab.binding.smartmeter.internal.sml.SmlMeterReader;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * Factory to get the correct device reader for a specific {@link ProtocolMode}
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class MeterDeviceFactory {

    /**
     * Gets a concrete {@link MeterDevice} for given values.
     * 
     * @param serialPortManagerSupplier The Supplier of a {@link SerialPortManager}
     * @param mode The {@link ProtocolMode}.
     * @param deviceId
     * @param serialPort The serial port identifier to connect ot.
     * @param initMessage The message which shall be sent before reading values (or to actually make the meter sent
     *            values).
     * @param baudrate The baudrate to set before communication.
     * @param baudrateChangeDelay The change delay before changing the baudrate (used only for specific protocols).
     * @return The new {@link MeterDevice} or null.
     */
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
