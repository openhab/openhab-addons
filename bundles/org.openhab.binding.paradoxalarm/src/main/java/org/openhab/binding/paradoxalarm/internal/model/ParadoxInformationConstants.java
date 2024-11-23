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
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.util.HexUtils;

/**
 * The {@link ParadoxInformation} Class that provides the basic panel
 * information (serial number, panel type, application, hardware and bootloader
 * versions. It's the object representation of 37 bytes 0x72 serial response.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxInformationConstants {
    public static Map<String, PanelType> panelTypes = new HashMap<>();
    static {
        panelTypes.put("0xA122", PanelType.EVO48);
        panelTypes.put("0xA133", PanelType.EVO48);
        panelTypes.put("0xA159", PanelType.EVO48);
        panelTypes.put("0xA123", PanelType.EVO192);
        panelTypes.put("0xA15A", PanelType.EVO192);
        panelTypes.put("0xA16D", PanelType.EVOHD);
        panelTypes.put("0xA41E", PanelType.SP5500);
        panelTypes.put("0xA450", PanelType.SP5500);
        panelTypes.put("0xA41F", PanelType.SP6000);
        panelTypes.put("0xA451", PanelType.SP6000);
        panelTypes.put("0xA420", PanelType.SP7000);
        panelTypes.put("0xA452", PanelType.SP7000);
        panelTypes.put("0xA524", PanelType.MG5000);
        panelTypes.put("0xA526", PanelType.MG5050);
        panelTypes.put("0xAE53", PanelType.SP4000);
        panelTypes.put("0xAE54", PanelType.SP65);
    }

    public static PanelType parsePanelType(byte[] infoPacket) {
        if (infoPacket == null || infoPacket.length != 37) {
            return PanelType.UNKNOWN;
        }
        byte[] panelTypeBytes = Arrays.copyOfRange(infoPacket, 6, 8);
        String key = "0x" + HexUtils.bytesToHex(panelTypeBytes);

        return ParadoxInformationConstants.panelTypes.getOrDefault(key, PanelType.UNKNOWN);
    }
}
