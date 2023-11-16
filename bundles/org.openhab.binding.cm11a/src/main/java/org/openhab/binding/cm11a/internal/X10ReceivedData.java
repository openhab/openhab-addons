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
package org.openhab.binding.cm11a.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for data received from the powerline by the cm11a interface. When data is received one of more of these
 * objects is created and then passed to interested objects.
 *
 * @author Bob Raker - Initial contribution
 */

public class X10ReceivedData {

    /**
     * All of the possible X10 commands
     *
     */
    public enum X10COMMAND {
        ALL_UNITS_OFF,
        ALL_LIGHTS_ON,
        ON,
        OFF,
        DIM,
        BRIGHT,
        ALL_LIGHTS_OFF,
        EXTENDED_CODE,
        HAIL_REQ,
        HAIL_ACK,
        PRESET_DIM_1,
        PRESET_DIM_2,
        EXTD_DATA_XFER,
        STATUS_ON,
        STATUS_OFF,
        STATUS_REQ,
        UNDEF // If no match, which shouldn't happen
    }

    /**
     * Used to decode the function bits received from the cm11a into an X10 function code
     *
     */
    protected static final Map<Integer, X10COMMAND> COMMAND_MAP;
    static {
        Map<Integer, X10COMMAND> tempMap = new HashMap<>();
        tempMap.put(0, X10COMMAND.ALL_UNITS_OFF);
        tempMap.put(1, X10COMMAND.ALL_LIGHTS_ON);
        tempMap.put(2, X10COMMAND.ON);
        tempMap.put(3, X10COMMAND.OFF);
        tempMap.put(4, X10COMMAND.DIM);
        tempMap.put(5, X10COMMAND.BRIGHT);
        tempMap.put(6, X10COMMAND.ALL_LIGHTS_OFF);
        tempMap.put(7, X10COMMAND.EXTENDED_CODE);
        tempMap.put(8, X10COMMAND.HAIL_REQ);
        tempMap.put(9, X10COMMAND.HAIL_ACK);
        tempMap.put(10, X10COMMAND.PRESET_DIM_1);
        tempMap.put(11, X10COMMAND.PRESET_DIM_2);
        tempMap.put(12, X10COMMAND.EXTD_DATA_XFER);
        tempMap.put(13, X10COMMAND.STATUS_ON);
        tempMap.put(14, X10COMMAND.STATUS_OFF);
        tempMap.put(15, X10COMMAND.STATUS_REQ);
        COMMAND_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Lookup table to convert House code received from the cm11a into an X10 house code
     */
    public static final char[] HOUSE_CODE = new char[] { 'M', 'E', 'C', 'K', 'O', 'G', 'A', 'I', 'N', 'F', 'D', 'L',
            'P', 'H', 'B', 'J' };

    /**
     * Lookup table to convert Unit code received from the cm11a into an X10 unit code
     */
    public static final byte[] UNIT_CODE = new byte[] { 13, 5, 3, 11, 15, 7, 1, 9, 14, 6, 4, 12, 16, 8, 2, 10 };

    private String[] addr;
    private X10COMMAND cmd;
    private int dims;

    /**
     * Constructor
     */
    public X10ReceivedData(String[] addr, X10COMMAND cmd, int dims) {
        this.addr = addr;
        this.cmd = cmd;
        this.dims = dims;
    }

    public String[] getAddr() {
        return addr;
    }

    public X10COMMAND getCmd() {
        return cmd;
    }

    public int getDims() {
        return dims;
    }

    @Override
    public String toString() {
        return "X10ReceivedData [addr=" + Arrays.toString(addr) + ", cmd=" + cmd + ", dims=" + dims + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(addr);
        result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
        result = prime * result + dims;
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        X10ReceivedData other = (X10ReceivedData) obj;
        if (!Arrays.equals(addr, other.addr)) {
            return false;
        }
        if (cmd != other.cmd) {
            return false;
        }
        if (dims != other.dims) {
            return false;
        }
        return true;
    }
}
