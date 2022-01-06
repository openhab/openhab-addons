/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.paradoxalarm.internal.parsers.IParadoxParser;
import org.openhab.core.util.HexUtils;

/**
 * The {@link ParadoxInformation} Class that provides the basic panel
 * information (serial number, panel type, application, hardware and bootloader
 * versions. It's the object representation of 37 bytes 0x72 serial response.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxInformation {

    private PanelType panelType;
    private String serialNumber;
    private Version applicationVersion;
    private Version hardwareVersion;
    private Version bootloaderVersion;

    public ParadoxInformation(byte[] panelInfoBytes, IParadoxParser parser) {
        panelType = ParadoxInformationConstants.parsePanelType(panelInfoBytes);

        applicationVersion = parser.parseApplicationVersion(panelInfoBytes);
        hardwareVersion = parser.parseHardwareVersion(panelInfoBytes);
        bootloaderVersion = parser.parseBootloaderVersion(panelInfoBytes);

        byte[] serialNumberBytes = Arrays.copyOfRange(panelInfoBytes, 12, 16);
        serialNumber = HexUtils.bytesToHex(serialNumberBytes);
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

    @Override
    public String toString() {
        return "ParadoxInformation [panelType=" + panelType + ", serialNumber=" + serialNumber + ", applicationVersion="
                + applicationVersion + ", hardwareVersion=" + hardwareVersion + ", bootloaderVersion="
                + bootloaderVersion + "]";
    }
}
