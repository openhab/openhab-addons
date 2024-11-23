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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.util.Arrays;

/**
 * @author Volker Bier - Initial contribution
 */
public class ResponseUtil {

    public static Response createResponse(byte upperChannelByte, byte lowerChannelByte) {
        return new Response(getChannelIds(upperChannelByte, lowerChannelByte));
    }

    public static Response createResponse(byte upperChannelByte, byte lowerChannelByte, byte responseType) {
        return new Response(ResponseStatus.getFor(responseType), getChannelIds(upperChannelByte, lowerChannelByte));
    }

    /**
     * returns the list of channels (starting with 1)
     */
    public static int[] getChannelIds(byte upperChannelByte, byte lowerChannelByte) {
        int[] result = new int[16];
        int idx = 0;

        byte b = lowerChannelByte;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) > 0) {
                result[idx++] = i + 1;
            }
            b = (byte) (b >> 1);
        }

        b = upperChannelByte;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) > 0) {
                result[idx++] = i + 9;
            }
            b = (byte) (b >> 1);
        }

        return Arrays.copyOfRange(result, 0, idx);
    }
}
