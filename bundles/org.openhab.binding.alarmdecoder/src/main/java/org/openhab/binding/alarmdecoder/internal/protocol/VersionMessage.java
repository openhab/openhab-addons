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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VersionMessage} class represents a parsed VER message.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class VersionMessage extends ADMessage {

    // Example: !VER:ffffffff,V2.2a.8.2,TX;RX;SM;VZ;RF;ZX;RE;AU;3X;CG;DD;MF;LR;KE;MK;CB;DS;ER

    /** Serial number */
    public final String serial;

    /** Firmware version */
    public final String version;

    /** Firmware capabilities */
    public final String capabilities;

    public VersionMessage(String message) throws IllegalArgumentException {
        super(message);

        String[] topLevel = message.split(":");
        if (topLevel.length != 2) {
            throw new IllegalArgumentException("Multiple colons found in VER message");
        }

        List<String> parts = splitMsg(topLevel[1]);

        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid number of parts in VER message");
        }

        serial = parts.get(0);
        version = parts.get(1);
        capabilities = parts.get(2);
    }
}
