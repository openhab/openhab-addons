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
package org.openhab.binding.max.internal.command;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.ThermostatModeType;

/**
 * {@link SCommand} for setting MAX! thermostat temperature & mode.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 * @author Marcel Verpaalen - OH2 update + simplification
 */
@NonNullByDefault
public class SCommand extends CubeCommand {

    private static final String BASE_STRING_S = "000040000000"; // for single devices
    private static final String BASE_STRING_G = "000440000000"; // for group/room devices

    private final boolean[] bits;
    private final String rfAddress;
    private final int roomId;

    /**
     * Creates a new instance of the MAX! protocol S command.
     *
     * @param rfAddress
     *            the RF address the command is for
     * @param roomId
     *            the room ID the RF address is mapped to
     * @param setpointTemperature
     *            the desired setpoint temperature for the device.
     */
    public SCommand(String rfAddress, int roomId, ThermostatModeType mode, double setpointTemperature) {
        this.rfAddress = rfAddress;
        this.roomId = roomId;

        // Temperature setpoint, Temp uses 6 bits (bit 0:5),
        // 20 deg C = bits 101000 = dec 40/2 = 20 deg C,
        // you need 8 bits to send so add the 2 bits below (sample 10101000 = hex A8)
        // bit 0,1 = 00 = Auto weekprog (no temp is needed)

        int setpointValue = (int) (setpointTemperature * 2);
        bits = Utils.getBits(setpointValue);

        // default to perm setting
        // AB => bit mapping
        // 01 = Permanent
        // 10 = Temporarily
        // 11 = Boost

        switch (mode) {
            case MANUAL:
                bits[7] = false; // A (MSB)
                bits[6] = true; // B
                break;
            case AUTOMATIC:
                bits[7] = false; // A (MSB)
                bits[6] = false; // B
                break;
            case BOOST:
                bits[7] = true; // A (MSB)
                bits[6] = true; // B
                break;
            case VACATION:
                // not implemented needs time
            default:
                // no further modes supported
        }
    }

    /**
     * Returns the Base64 encoded command string to be sent via the MAX!
     * protocol.
     *
     * @return the string representing the command
     */
    @Override
    public String getCommandString() {
        final String baseString;
        if (roomId == 0) {
            baseString = BASE_STRING_S;
        } else {
            baseString = BASE_STRING_G;
        }

        final String commandString = baseString + rfAddress + Utils.toHex(roomId) + Utils.toHex(bits);

        final String encodedString = Base64.getEncoder().encodeToString(Utils.hexStringToByteArray(commandString));

        return "s:" + encodedString + "\r\n";
    }

    @Override
    public String getReturnStrings() {
        return "S:";
    }
}
