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
package org.openhab.binding.nibeheatpump.internal.config;

/**
 * Configuration class for NibeHeatPump device.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpConfiguration {
    public String hostName;
    public int port;
    public int readCommandsPort;
    public int writeCommandsPort;
    public String serialPort;
    public int refreshInterval;
    public boolean enableReadCommands;
    public boolean enableWriteCommands;
    public boolean sendAckToMODBUS40;
    public boolean sendAckToRMU40;
    public boolean sendAckToSMS40;
    public String enableWriteCommandsToRegisters;
    public int throttleTime;

    @Override
    public String toString() {
        String str = "";

        str += "hostName = " + hostName;
        str += ", port = " + port;
        str += ", readCommandsPort = " + readCommandsPort;
        str += ", writeCommandsPort = " + writeCommandsPort;
        str += ", serialPort = " + serialPort;
        str += ", refreshInterval = " + refreshInterval;
        str += ", enableReadCommands = " + enableReadCommands;
        str += ", enableWriteCommands = " + enableWriteCommands;
        str += ", sendAckToMODBUS40 = " + sendAckToMODBUS40;
        str += ", sendAckToRMU40 = " + sendAckToRMU40;
        str += ", sendAckToSMS40 = " + sendAckToSMS40;
        str += ", enableWriteCommandsToRegisters = " + enableWriteCommandsToRegisters;
        str += ", throttleTime = " + throttleTime;

        return str;
    }
}
