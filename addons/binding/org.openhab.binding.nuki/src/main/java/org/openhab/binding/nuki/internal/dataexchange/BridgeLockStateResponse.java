/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateDto;

/**
 * The {@link BridgeLockStateResponse} class wraps {@link BridgeApiLockStateDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeLockStateResponse extends NukiBaseResponse {

    private BridgeApiLockStateDto bridgeLockState;

    public BridgeLockStateResponse(int status, String message) {
        super(status, message);
    }

    public BridgeLockStateResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public BridgeApiLockStateDto getBridgeLockState() {
        return bridgeLockState;
    }

    public void setBridgeLockState(BridgeApiLockStateDto bridgeLockState) {
        this.bridgeLockState = bridgeLockState;
    }

}
