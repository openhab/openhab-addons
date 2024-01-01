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
package org.openhab.binding.bluetooth.blukii.internal.data;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blukii data decoding.
 *
 * @author Markus Rathgeb - Initial contribution (migrate from handler)
 * @author Markus Rathgeb - Fixed temperature decoding
 */
@NonNullByDefault
public class BlukiiDataDecoder {

    private final Logger logger = LoggerFactory.getLogger(BlukiiDataDecoder.class);

    public @Nullable BlukiiData decode(final byte[] data) {
        if (data.length < 22) {
            logger.debug("Blukii data length to short (skip decoding): {}", HexUtils.bytesToHex(data, " "));
            return null;
        }

        if (data[0] != 0x4F) {
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Decode Blukii data: {}", HexUtils.bytesToHex(data, " "));
        }

        final int battery = data[12] & 0x7F;
        final Optional<Magnetometer> magnetometer;
        final Optional<Environment> environment;
        final Optional<Accelerometer> accelerometer;

        if ((data[14] & 0x30) == 0x30) {
            magnetometer = Optional.of(processMagnetometerData(data));
            environment = Optional.empty();
            accelerometer = Optional.empty();
        } else if ((data[14] & 0x10) == 0x10) {
            magnetometer = Optional.empty();
            environment = Optional.of(processEnvironmentData(data));
            accelerometer = Optional.empty();
        } else if ((data[14] & 0x20) == 0x20) {
            magnetometer = Optional.empty();
            environment = Optional.empty();
            accelerometer = Optional.of(processAccelerometerData(data));
        } else {
            magnetometer = Optional.empty();
            environment = Optional.empty();
            accelerometer = Optional.empty();
        }

        return new BlukiiData(battery, magnetometer, environment, accelerometer);
    }

    private static Environment processEnvironmentData(byte[] data) {
        double pressure = doubleByteToInt(data[15], data[16]) / 10;
        int luminance = doubleByteToInt(data[17], data[18]);
        int humidity = data[19] & 0xFF;
        double temperature = (data[20] << 8 | data[21] & 0xFF) / 256d;

        return new Environment(pressure, luminance, humidity, temperature);
    }

    private static Accelerometer processAccelerometerData(byte[] data) {
        int range = data[15] & 0xFF;
        int x = (short) doubleByteToInt(data[16], data[17]) * (range / 2);
        int y = (short) doubleByteToInt(data[18], data[19]) * (range / 2);
        int z = (short) doubleByteToInt(data[20], data[21]) * (range / 2);

        double tiltX = 180 * Math.acos(x / Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))) / Math.PI;
        double tiltY = 180 * Math.acos(y / Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))) / Math.PI;
        double tiltZ = 180 * Math.acos(z / Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))) / Math.PI;

        return new Accelerometer(tiltX, tiltY, tiltZ);
    }

    private static Magnetometer processMagnetometerData(byte[] data) {
        int x = (short) doubleByteToInt(data[16], data[17]);
        int y = (short) doubleByteToInt(data[18], data[19]);
        int z = (short) doubleByteToInt(data[20], data[21]);

        return new Magnetometer(x, y, z);
    }

    private static int doubleByteToInt(byte b1, byte b2) {
        int i1 = b1 & 0xFF;
        int i2 = b2 & 0xFF;
        return (i1 * 0x100) + i2;
    }
}
