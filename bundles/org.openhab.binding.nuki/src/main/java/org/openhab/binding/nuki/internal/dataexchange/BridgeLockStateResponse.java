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
package org.openhab.binding.nuki.internal.dataexchange;

import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateDto;

/**
 * The {@link BridgeLockStateResponse} class wraps {@link BridgeApiLockStateDto} class.
 *
 * @author Markus Katter - Initial contribution
 * @author Christian Hoefler - Door sensor integration
 */
public class BridgeLockStateResponse extends NukiBaseResponse {

    private final BridgeApiLockStateDto bridgeApiLockStateDto;

    public BridgeLockStateResponse(int status, String message, BridgeApiLockStateDto bridgeApiLockStateDto) {
        super(status, message);
        this.bridgeApiLockStateDto = bridgeApiLockStateDto;
        if (bridgeApiLockStateDto != null) {
            this.setSuccess(bridgeApiLockStateDto.isSuccess());
        }
    }

    public BridgeLockStateResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
        this.bridgeApiLockStateDto = null;
    }

    public BridgeApiLockStateDto getBridgeApiLockStateDto() {
        return bridgeApiLockStateDto;
    }
}
