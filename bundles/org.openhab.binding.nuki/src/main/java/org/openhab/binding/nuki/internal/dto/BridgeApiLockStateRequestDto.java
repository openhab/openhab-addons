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

/**
 * The {@link BridgeApiLockStateRequestDto} class defines the Data Transfer Object (POJO) which is send from the Nuki
 * Bridge to the openHAB Server.
 *
 * @author Markus Katter - Initial contribution
 * @author Christian Hoefler - Door sensor integration
 */
public class BridgeApiLockStateRequestDto extends BridgeApiDeviceStateDto {

    private Integer nukiId;

    public Integer getNukiId() {
        return nukiId;
    }

    public void setNukiId(Integer nukiId) {
        this.nukiId = nukiId;
    }
}
