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

    protected final Logger log = LoggerFactory.getLogger(Mac.class);
    private final byte[] mac;

    /**
     * Creates an instance representing the mac address
     *
     * @param macBytes The 6-byte mac address in byte array
     *            mac is an empty array, if @param macBytes is not a valid mac address
     */
    public Mac(byte[] macBytes) {
        if (isMACValid(macBytes)) {
            mac = macBytes;
        } else {
            log.debug("Not a valid mac address: {}", bytesToMacStr(macBytes));
            mac = new byte[0];
        }
    }

    /**
     * Creates an instance representing the MAC address
     *
     * @param macStr mac address represented in String seperated by colons (
     *            <code>:</code>) (e.g. 00:00:00:00:00:00)
     * 
     */
    public Mac(String macStr) {
        mac = macStrToBytes(macStr);
    }

    /**
     * Returns the mac address in bytes array
     *
     * @return mac address in bytes array
     */
    public byte[] getMac() {
        return mac;
    }

    /**
     * Returns the mac address represented in String
     *
     * @return mac address in String or "" if not a valid mac address
     */
    public String getMacString() {
        return bytesToMacStr(mac);
    }

    /**
     * Converts mac address String into bytes
     *
     * @param macStr The 6-byte mac address (00:00:00:00:00:00) in String separated
     *            by colons (<code>:</code>)
     * @return Converted mac address in bytes or empty byte array
     * 
     */
    public byte[] macStrToBytes(String macStr) {
        if (macStr == null) {
            log.debug("macStrToBytes param macStr is null.");
            return new byte[0];
        }

        String[] macs = macStr.split(":");
        if (macs.length != 6) {
            log.debug("macStrToBytes failed on {}. Length of mac address invalid.", macStr);
            return new byte[0];
        }

        byte[] bout = new byte[6];
        for (int i = 0; i < macs.length; i++) {
            try {
                Integer hex = Integer.parseInt(macs[i], 16);
                bout[i] = hex.byteValue();
            } catch (NumberFormatException e) {
                log.debug("macStrToBytes failed on {}. No valid mac address.", macStr);
                return new byte[0];
            }
        }
        return bout;
    }

    /**
     * Returns whether the specified mac bytes array is valid with the following
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
     * Converts mac address bytes into String
     *
     * @param macBytes The 6-byte mac address in byte array
     * @return A mac address String converted from the byte array;
     * @return or "", if @param macBytes does not contains a valid mac address
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
