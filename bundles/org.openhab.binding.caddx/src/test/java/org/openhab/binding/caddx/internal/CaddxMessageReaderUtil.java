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
package org.openhab.binding.caddx.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Util class to read test input messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public final class CaddxMessageReaderUtil {
    private static final String MESSAGE_EXT = ".msg";

    private CaddxMessageReaderUtil() {
        // Util class
    }

    /**
     * Reads the raw bytes of the message given the file relative to this package and returns the objects.
     *
     * @param messageName name of the telegram file to read
     * @return The raw bytes of a telegram
     */
    public static byte[] readRawMessage(String messageName) {
        try (InputStream is = CaddxMessageReaderUtil.class.getResourceAsStream(messageName + MESSAGE_EXT);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {
            String hexString = br.lines().collect(Collectors.joining("\n"));

            return HexUtils.hexToBytes(hexString, " ");
        } catch (IOException e) {
            throw new AssertionError("IOException reading message data: ", e);
        }
    }

    /**
     * Reads a message given the file relative to this package and returns the object.
     *
     * @param messageName name of the message file to read
     * @return a CaddxMessage object
     */
    public static CaddxMessage readCaddxMessage(String messageName) {
        byte[] bytes = readRawMessage(messageName);
        return new CaddxMessage(CaddxMessageContext.NONE, bytes, true);
    }
}
