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
package org.openhab.binding.lutron.internal.radiora;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.radiora.protocol.LEDMapFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles decoding message types from RadioRA
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class RS232MessageParser {

    private final Logger logger = LoggerFactory.getLogger(RS232MessageParser.class);

    public @Nullable RadioRAFeedback parse(String msg) {
        String prefix = parsePrefix(msg);

        switch (prefix) {
            case "LMP":
                return new LEDMapFeedback(msg);
            case "LZC":
                return new LocalZoneChangeFeedback(msg);
            case "ZMP":
                return new ZoneMapFeedback(msg);
            case "!":
                // No action to take when this message is received but handle
                // it to prevent the the default log statement from occurring.
                break;

            default:
                logger.debug("Unhandled msg received from RS232 [{}]", msg);
                break;
        }

        return null;
    }

    protected String parsePrefix(String msg) {
        String[] arr = msg.split(",");
        if (arr.length < 1) {
            logger.debug("Unexpected msg received from RS232 [{}]", msg);
            return "";
        }

        return arr[0];
    }
}
