/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora;

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
public class RS232MessageParser {

    private Logger logger = LoggerFactory.getLogger(RS232MessageParser.class);

    public RadioRAFeedback parse(String msg) {
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
