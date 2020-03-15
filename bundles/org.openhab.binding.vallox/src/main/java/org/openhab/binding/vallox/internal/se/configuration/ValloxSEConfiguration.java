/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vallox.internal.se.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;

/**
 * Configuration class for Vallox SE models.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ValloxSEConfiguration {

    public String tcpHost = "";
    public int tcpPort;
    public String serialPort = "";
    public int panelNumber;

    /**
     * Get panel number as byte.
     *
     * @return byte the panel number as byte
     */
    public byte getPanelAsByte() {
        return ValloxSEConstants.ADDRESS_PANEL_MAPPING[panelNumber - 1];
    }

    /**
     * Get configuration as string.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Host=" + tcpHost + ", Port=" + tcpPort + ", Serial port=" + serialPort + ", Panel number="
                + panelNumber;
    }
}
