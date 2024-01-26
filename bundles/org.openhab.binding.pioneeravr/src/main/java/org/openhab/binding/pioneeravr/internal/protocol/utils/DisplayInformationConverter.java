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
package org.openhab.binding.pioneeravr.internal.protocol.utils;

import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnectionException;

/**
 *
 * @author Antoine Besnard - Initial contribution
 */
public class DisplayInformationConverter {

    /**
     * Convert an IpControl information message payload to a readable String.
     *
     * @param responsePayload
     * @return
     * @throws AvrConnectionException
     */
    public static String convertMessageFromIpControl(String responsePayload) throws AvrConnectionException {
        // Example from Pioneer docs: When " [)(]DIGITAL EX " is displayed,
        // response command is:
        // FL000005064449474954414C00455800<CR+LF>

        // First byte holds the two special flags. Do not use it to parse the
        // message.
        // Convert the ASCII values back to string
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < responsePayload.length() - 1; i += 2) {
            String hexAsciiValue = responsePayload.substring(i, i + 2);
            try {
                sb.append((char) Integer.parseInt(hexAsciiValue, 16));
            } catch (Exception e) {
                throw new AvrConnectionException(
                        "Failed to parse the reponsePayload as an IpControl information message.", e);
            }
        }
        return sb.toString();
    }
}
