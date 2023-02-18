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

/**
 * The {@link BridgeApiListDeviceLastKnownState} class defines the Data Transfer Object (POJO) for the Nuki Bridge API
 * lastKnownState of {@link BridgeApiListDeviceDto}.
 *
 * @author Jan Vybíral - Initial contribution
 */
public class BridgeApiListDeviceLastKnownState extends BridgeApiLockStateDto {

    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
