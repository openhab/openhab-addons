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

/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016, 2017 Anthony Law
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Contributors:
 *      - Anthony Law (mob41) - Initial API Implementation
 *      - bwssytems
 *      - Christian Fischer (computerlyrik)
 *      - modified by Joerg Dokupil
 *******************************************************************************/
//package com.github.mob41.blapi.mac;

package org.openhab.binding.ws980wifi.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that handles a MAC address in String and bytes array format
 *
 * @author Anthony - Initial contribution
 */
@NonNullByDefault
public class Mac {

    protected static final Logger log = LoggerFactory.getLogger(Mac.class);
    private final byte[] mac;

    /**
     * Creates an instance representing the MAC address
     *
     * @param macBytes The 6-byte MAC address in byte array
     *            mac is an empty array, if @param macBytes is not a valid MAC Address
     */
    public Mac(byte[] macBytes) {
        if (isMACValid(macBytes)) {
            mac = macBytes;
        } else {
            log.debug("Fehlerhafte Mac-Adresse: {}", bytesToMacStr(macBytes));
            mac = new byte[0];
        }
    }

    /**
     * Creates an instance representing the MAC address
     *
     * @param macStr MAC address represented in String seperated by cottons (
     *            <code>:</code>) (e.g. 00:00:00:00:00:00)
     * 
     */
    public Mac(String macStr) {
        mac = macStrToBytes(macStr);
    }

    /**
     * Returns the MAC address in bytes array
     *
     * @return MAC address in bytes array
     */
    public byte[] getMac() {
        return mac;
    }

    /**
     * Returns the MAC address represented in String
     *
     * @return MAC address in String or "" if not a valid MAC-Address
     */
    public String getMacString() {
        return bytesToMacStr(mac);
    }

    /**
     * Converts MAC address String into bytes
     *
     * @param macStr The 6-byte MAC Address (00:00:00:00:00:00) in String separated
     *            by cottons (<code>:</code>)
     * @return Converted MAC Address in bytes or empty byte array
     * 
     */
    public static byte[] macStrToBytes(String macStr) {
        if (macStr == null) {
            log.debug("macStrToBytes param macStr is null.");
            return new byte[0];
        }

        String[] macs = macStr.split(":");
        if (macs.length != 6) {
            log.debug("macStrToBytes failed on {}. Length of MAC-Address invalid.", macStr);
            return new byte[0];
        }

        byte[] bout = new byte[6];
        for (int i = 0; i < macs.length; i++) {
            try {
                Integer hex = Integer.parseInt(macs[i], 16);
                bout[i] = hex.byteValue();
            } catch (NumberFormatException e) {
                log.debug("macStrToBytes failed on {}. No valid MAC-Address.", macStr);
                return new byte[0];
            }
        }
        return bout;
    }

    /**
     * Returns whether the specified MAC bytes array is valid with the following
     * conditions:<br>
     * <br>
     * 1. <code>macBytes</code> not <code>null</code><br>
     * 2. <code>macBytes</code>'s length is equal to 6
     *
     * @param macBytes The byte array to be validated
     * @return The validation result
     */
    public static boolean isMACValid(byte[] macBytes) {
        return macBytes != null && macBytes.length == 6;
    }

    /**
     * Converts MAC address bytes into String
     *
     * @param macBytes The 6-byte MAC Address in byte array
     * @return A MAC address String converted from the byte array;
     * @return or "", if @param macBytes does not contains a valid MAC Address
     */
    public static String bytesToMacStr(byte[] macBytes) {
        String str = "";

        if (isMACValid(macBytes)) {
            for (int i = 0; i < macBytes.length; i++) {
                String hexStr = String.format("%02x", macBytes[i]);
                str += hexStr;
                if (i != macBytes.length - 1) {
                    str += ':';
                }
            }
        }
        return str;
    }
}
