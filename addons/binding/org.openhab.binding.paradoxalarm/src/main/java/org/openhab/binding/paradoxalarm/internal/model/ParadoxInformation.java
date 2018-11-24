/** Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.Arrays;

import org.openhab.binding.paradoxalarm.internal.communication.EvoCommunicator;
import org.openhab.binding.paradoxalarm.internal.parsers.IParadoxParser;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParadoxInformation {

    private static Logger logger = LoggerFactory.getLogger(EvoCommunicator.class);

    private PanelType panelType;
    private String serialNumber;
    private Version applicationVersion;
    private Version hardwareVersion;
    private Version bootloaderVersion;

    public ParadoxInformation(byte[] panelInfoBytes, IParadoxParser parser) {
        panelType = parsePanelType(panelInfoBytes);

        applicationVersion = parser.parseApplicationVersion(panelInfoBytes);
        hardwareVersion = parser.parseHardwareVersion(panelInfoBytes);
        bootloaderVersion = parser.parseBootloaderVersion(panelInfoBytes);

        byte[] serialNumberBytes = Arrays.copyOfRange(panelInfoBytes, 12, 16);
        serialNumber = ParadoxUtil.byteArrayAsString(serialNumberBytes);
    }

    public PanelType getPanelType() {
        return panelType;
    }

    public Version getApplicationVersion() {
        return applicationVersion;
    }

    public Version getHardwareVersion() {
        return hardwareVersion;
    }

    public Version getBootLoaderVersion() {
        return bootloaderVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    private PanelType parsePanelType(byte[] infoPacket) {
        if (infoPacket == null || infoPacket.length != 37) {
            return PanelType.UNKNOWN;
        }
        byte[] panelTypeBytes = Arrays.copyOfRange(infoPacket, 6, 8);
        String key = "0x" + ParadoxUtil.byteArrayAsString(panelTypeBytes);

        return ParadoxInformationConstants.panelTypes.getOrDefault(key, PanelType.UNKNOWN);
    }

    @Override
    public String toString() {
        return "ParadoxInformation [panelType=" + panelType + ", serialNumber=" + serialNumber + ", applicationVersion="
                + applicationVersion + ", hardwareVersion=" + hardwareVersion + ", bootloaderVersion="
                + bootloaderVersion + "]";
    }

}
