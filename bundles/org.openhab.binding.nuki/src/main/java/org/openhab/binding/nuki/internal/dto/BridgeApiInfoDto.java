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
package org.openhab.binding.nuki.internal.dto;

import java.util.List;

/**
 * The {@link BridgeApiInfoDto} class defines the Data Transfer Object (POJO) for the Nuki Bridge API /info endpoint.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiInfoDto {

    private int bridgeType;
    private BridgeApiInfoIdDto ids;
    private BridgeApiInfoVersionDto versions;
    private int uptime;
    private String currentTime;
    private boolean serverConnected;
    private List<BridgeApiInfoScanResultDto> scanResults;

    public int getBridgeType() {
        return bridgeType;
    }

    public void setBridgeType(int bridgeType) {
        this.bridgeType = bridgeType;
    }

    public BridgeApiInfoIdDto getIds() {
        return ids;
    }

    public void setIds(BridgeApiInfoIdDto ids) {
        this.ids = ids;
    }

    public BridgeApiInfoVersionDto getVersions() {
        return versions;
    }

    public void setVersions(BridgeApiInfoVersionDto versions) {
        this.versions = versions;
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
