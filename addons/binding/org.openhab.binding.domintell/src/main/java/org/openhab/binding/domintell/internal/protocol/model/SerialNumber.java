/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.protocol.model;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.domintell.internal.protocol.exception.AddressException;

import java.util.Objects;

/**
* The {@link SerialNumber} is Domintell module serial number
*
* @author Gabor Bicskei - Initial contribution
*/
public class SerialNumber {
    /**
     * Address in decimal format
     */
    private int address;

    /**
     * Constructor.
     *
     * @param address Hex address to parse.
     */
    public SerialNumber(@NonNull String address) {
        if (address.startsWith("0x")) {
            if (address.length()>2) {
                this.address = Integer.parseInt(address.trim().substring(2), 16);
            } else {
                throw new AddressException("Invalid module address: " + address);
            }
        } else {
            this.address = Integer.parseInt(address.trim());
        }
    }

    public SerialNumber(int address) {
        this.address = address;
    }

    public String getAddressHex() {
        return Integer.toString(address, 16).toUpperCase();
    }

    public Integer getAddressInt() {
        return address;
    }

    public String toLabel() {
        return address + "/0x" + getAddressHex();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerialNumber that = (SerialNumber) o;
        return address == that.address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "SerialNumber{" +
                "address=" + address +
                '}';
    }

    /**
     * Writing the address into 6 char long hes format.
     *
     * @return Address string.
     */
    String toStringFix6() {
        String str = "      " + Integer.toString(address, 16).toUpperCase();
        return str.length() <= 6 ? str : str.substring(str.length() - 6);
    }
}
