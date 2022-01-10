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
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link BridgeApiInfoVersionDto} class defines the Data Transfer Object (POJO) for the Nuki Bridge API /list
 * endpoint.
 * It is a nested JSON object of {@link BridgeApiInfoDto}.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiInfoVersionDto {

    private String firmwareVersion;
    private String wifiFirmwareVersion;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getWifiFirmwareVersion() {
        return wifiFirmwareVersion;
    }

    public void setWifiFirmwareVersion(String wifiFirmwareVersion) {
        this.wifiFirmwareVersion = wifiFirmwareVersion;
    }
}
