/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

/*
 * This class has utilities related to the X10 protocol.
 *
 * @author Bernd Pfrommer
 * @since 1.7.0
 */
public class X10Address {
    byte houseCode;
    byte unitCode;

    public X10Address(byte houseCode, byte unitCode) {
        this.houseCode = houseCode;
        this.unitCode = unitCode;
    }

    /**
     * converts house code to clear text
     *
     * @param c house code as per X10 spec
     * @return clear text house code, i.e letter A-P
     */
    @Override
    public String toString() {
        String s = String.format("%c.%d", this.houseCode + 'A', this.unitCode);
        return s;
    }

    public byte getHouseCode() {
        return houseCode;
    }

    public void setHouseCode(byte houseCode) {
        this.houseCode = houseCode;
    }

    public byte getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(byte unitCode) {
        this.unitCode = unitCode;
    }

    /**
     * Test if string has valid X10 address of form "H.U", e.g. A.10
     *
     * @param s string to test
     * @return true if is valid X10 address
     */
    public static boolean s_isValidAddress(String s) {
        String[] parts = s.split("\\.");
        if (parts.length != 2) {
            return false;
        }
        return parts[0].matches("[A-P]") && parts[1].matches("\\d{1,2}");
    }

    /**
     * Turn clear text address ("A.10") to byte code
     *
     * @param addr clear text address
     * @return byte that encodes house + unit code
     */
    public X10Address(String addr) {
        String[] parts = addr.split("\\.");
        int ih = parts[0].charAt(0) - 'A';
        int iu = Integer.valueOf(parts[1]);
        this.houseCode = (byte) ih;
        this.unitCode = (byte) iu;
    }
}
