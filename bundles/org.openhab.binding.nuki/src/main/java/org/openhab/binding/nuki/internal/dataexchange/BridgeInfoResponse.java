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
package org.openhab.binding.nuki.internal.dataexchange;

import java.util.List;

import org.openhab.binding.nuki.internal.dto.BridgeApiInfoDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiInfoScanResultDto;

/**
 * The {@link BridgeInfoResponse} class wraps {@link BridgeApiInfoDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeInfoResponse extends NukiBaseResponse {

    private int bridgeType;
    private int hardwareId;
    private int serverId;
    private String firmwareVersion;
    private String wifiFirmwareVersion;
    private int uptime;
    private String currentTime;
    private boolean serverConnected;
    private List<BridgeApiInfoScanResultDto> scanResults;

    public BridgeInfoResponse(int status, String message, BridgeApiInfoDto bridgeApiInfoDto) {
        super(status, message);
        if (bridgeApiInfoDto != null) {
            this.setSuccess(true);
            this.setBridgeType(bridgeApiInfoDto.getBridgeType());
            this.setHardwareId(bridgeApiInfoDto.getIds().getHardwareId());
            this.setServerId(bridgeApiInfoDto.getIds().getServerId());
            this.setFirmwareVersion(bridgeApiInfoDto.getVersions().getFirmwareVersion());
            this.setWifiFirmwareVersion(bridgeApiInfoDto.getVersions().getWifiFirmwareVersion());
            this.setUptime(bridgeApiInfoDto.getUptime());
            this.setCurrentTime(bridgeApiInfoDto.getCurrentTime());
            this.setScanResults(bridgeApiInfoDto.getScanResults());
        }
    }

    public BridgeInfoResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public int getBridgeType() {
        return bridgeType;
    }

    public void setBridgeType(int bridgeType) {
        this.bridgeType = bridgeType;
    }

    public int getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(int hardwareId) {
        this.hardwareId = hardwareId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

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

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public boolean isServerConnected() {
        return serverConnected;
    }

    public void setServerConnected(boolean serverConnected) {
        this.serverConnected = serverConnected;
    }

    public List<BridgeApiInfoScanResultDto> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<BridgeApiInfoScanResultDto> scanResults) {
        this.scanResults = scanResults;
    }
}
