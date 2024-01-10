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
package org.openhab.binding.nibeheatpump.internal.message;

/**
 * The {@link ModbusValue} define class for Modbus values of the Nibe communication.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusValue {

    private int coilAddress;
    private int value;

    public ModbusValue(int coilAddress, int value) {
        this.coilAddress = coilAddress;
        this.value = value;
    }

    public int getCoilAddress() {
        return coilAddress;
    }

    public void setCoilAddress(int coilAddress) {
        this.coilAddress = coilAddress;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String str = "{";

        str += "Coil address = " + coilAddress;
        str += ", Value = " + value;
        str += "}";

        return str;
    }
}
