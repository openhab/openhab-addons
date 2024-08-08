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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for parsers for data sent to an HD PowerView Generation 3 Shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface ShadeDataWriter {

    /**
     * Convert the given primary position percentage to a byte array containing the values to write to the shade.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public byte[] primaryPositionToBytes(double percent);

    /**
     * Convert the given secondary position percentage to a byte array containing the values to write to the shade.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public byte[] secondaryPositionToBytes(double percent);

    /**
     * Convert the given tilt position percent to a byte array containing the values to write to the shade. . Reverse
     * engineered from observing Bluetooth traffic between the HD App and a shade.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public byte[] tiltPositionToBytes(double percent);

    /**
     * Return the byte array.
     */
    public byte[] getBytes();

    /**
     * Overwrite the primary position command bytes.
     */
    public ShadeDataWriter withPrimary(double percent);

    /**
     * Overwrite the secondary position command bytes.
     */
    public ShadeDataWriter withSecondary(double percent);

    /**
     * Overwrite the sequence number command byte.
     */
    public ShadeDataWriter withSequence(byte sequence);

    /**
     * Overwrite the tilt position command bytes.
     */
    public ShadeDataWriter withTilt(double percent);
}
