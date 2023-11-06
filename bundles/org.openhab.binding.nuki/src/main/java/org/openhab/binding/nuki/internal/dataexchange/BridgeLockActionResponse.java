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

import org.openhab.binding.nuki.internal.dto.BridgeApiLockActionDto;

/**
 * The {@link BridgeLockActionResponse} class wraps {@link BridgeApiLockActionDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeLockActionResponse extends NukiBaseResponse {

    private boolean batteryCritical;

    public BridgeLockActionResponse(int status, String message, BridgeApiLockActionDto bridgeApiLockActionDto) {
        super(status, message);
        if (bridgeApiLockActionDto != null) {
            this.setSuccess(bridgeApiLockActionDto.isSuccess());
            this.setBatteryCritical(bridgeApiLockActionDto.isBatteryCritical());
        }
    }

    public BridgeLockActionResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }
}
