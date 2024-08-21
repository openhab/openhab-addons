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
package org.openhab.binding.pioneeravr.internal.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the display status message send by the Pioneer AV receiver
 * (response to "?FL" request)
 *
 * @author Rainer Ostendorf - Initial contribution
 */
public class DisplayInformation {

    private final Logger logger = LoggerFactory.getLogger(DisplayInformation.class);

    private Boolean volumeDisplay; // 1-light, 0-OFF
    private Boolean guidIcon; // 1-light, 0-OFF
    private String infoText = ""; // the actual display text

    /**
     * parse the display status text send from the receiver
     *
     * @param responsePayload the responses payload, that is without the leading "FL"
     */
    public DisplayInformation(String responsePayload) {
        volumeDisplay = false;
        guidIcon = false;
        infoText = "";

        // Example from Pioneer docs: When " [)(]DIGITAL EX " is displayed, response command is:
        // FL000005064449474954414C00455800<CR+LF>

        // first byte holds the two special flags
        byte firstByte = (byte) Integer.parseInt(responsePayload.substring(0, 1), 16);

        if ((firstByte & (1 << 0)) == (1 << 0)) {
            guidIcon = true;
        }
        if ((firstByte & (1 << 1)) == (1 << 1)) {
            volumeDisplay = true;
        }

        // convert the ascii values back to string
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < responsePayload.length() - 1; i += 2) {
            String hexAsciiValue = responsePayload.substring(i, i + 2);
            try {
                sb.append((char) Integer.parseInt(hexAsciiValue, 16));
            } catch (Exception e) {
                logger.error("parsing string failed '{}'", responsePayload, e);
            }
        }
        infoText = sb.toString();
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public Boolean getVolumeDisplay() {
        return volumeDisplay;
    }

    public Boolean getGuidIcon() {
        return guidIcon;
    }
}
