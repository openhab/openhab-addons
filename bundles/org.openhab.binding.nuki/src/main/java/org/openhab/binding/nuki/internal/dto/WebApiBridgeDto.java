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
package org.openhab.binding.nuki.internal.dto;

import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link WebApiBridgeDiscoveryDto} class defines the Data Transfer Object (POJO) for bridge object
 * the https://api.nuki.io/discover/bridges Web API.
 *
 * @author Jan Vybíral - Initial contribution
 */
public class WebApiBridgeDto {
    private String bridgeId;
    private String ip;
    private int port;
    private String dateUpdated;

    public String getBridgeId() {
        return bridgeId;
    }

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public String toString() {
        return "WebApiBridgeDto{" + "bridgeId='" + bridgeId + '\'' + ", ip='" + ip + '\'' + ", port=" + port
                + ", dateUpdated='" + dateUpdated + '\'' + '}';
    }

    public ThingUID getThingUid() {
        return new ThingUID(NukiBindingConstants.THING_TYPE_BRIDGE, getBridgeId());
    }
}
