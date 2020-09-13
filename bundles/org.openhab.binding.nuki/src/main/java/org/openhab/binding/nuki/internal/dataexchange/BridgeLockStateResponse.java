/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
 */
public class BridgeLockStateResponse extends NukiBaseResponse {

    private int state;
    private String stateName;
    private boolean batteryCritical;

    public BridgeLockStateResponse(int status, String message, BridgeApiLockStateDto bridgeApiLockStateDto) {
        super(status, message);
        if (bridgeApiLockStateDto != null) {
            this.setSuccess(bridgeApiLockStateDto.isSuccess());
            this.setState(bridgeApiLockStateDto.getState());
            this.setStateName(bridgeApiLockStateDto.getStateName());
            this.setBatteryCritical(bridgeApiLockStateDto.isBatteryCritical());
        }
    }

    public BridgeLockStateResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }
}
