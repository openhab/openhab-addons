/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SbusService} defines the interface for handling Sbus communication.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public interface SbusService {

    /**
     * Reads temperature values from a device.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @param temperatureUnit The unit of measurement (FAHRENHEIT or CELSIUS)
     * @return array of temperature values in Celsius
     * @throws Exception if reading fails
     */
    float[] readTemperatures(int subnetId, int id, TemperatureUnit temperatureUnit) throws Exception;

    /**
     * Reads RGBW values from a device channel.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @param channelNumber the channel number to read
     * @return array of RGBW values [R, G, B, W]
     * @throws Exception if reading fails
     */
    int[] readRgbw(int subnetId, int id, int channelNumber) throws Exception;

    /**
     * Reads status values from device channels.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @return array of channel status values
     * @throws Exception if reading fails
     */
    int[] readStatusChannels(int subnetId, int id) throws Exception;

    /**
     * Reads contact status values from device channels.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @return array of contact status values (true for open, false for closed)
     * @throws Exception if reading fails
     */
    boolean[] readContactStatusChannels(int subnetId, int id) throws Exception;

    /**
     * Writes RGBW values to a device channel.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @param channelNumber the channel number to write to
     * @param color An array of 4 integers representing RGBW values (0-255 each)
     * @throws Exception if writing fails
     */
    void writeRgbw(int subnetId, int id, int channelNumber, int[] color) throws Exception;

    /**
     * Writes a value to a single channel.
     *
     * @param subnetId the subnet ID of the device
     * @param id the device ID
     * @param channelNumber the channel number to write to
     * @param value the value to write
     * @param timer timer value (-1 for no timer)
     * @throws Exception if writing fails
     */
    void writeSingleChannel(int subnetId, int id, int channelNumber, int value, int timer) throws Exception;

    /**
     * Closes the service and releases resources.
     */
    void close();

    void initialize(String host, int port) throws Exception;
}
