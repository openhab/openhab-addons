/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import org.openhab.binding.nuki.internal.dto.BridgeApiLockActionDto;

/**
 * The {@link BridgeLockActionResponse} class wraps {@link BridgeApiLockActionDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeLockActionResponse extends NukiBaseResponse {

    private BridgeApiLockActionDto bridgeLockAction;

    public BridgeLockActionResponse(int status, String message) {
        super(status, message);
    }

    public BridgeLockActionResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public BridgeApiLockActionDto getBridgeLockAction() {
        return bridgeLockAction;
    }

    public void setBridgeLockAction(BridgeApiLockActionDto bridgeLockAction) {
        this.bridgeLockAction = bridgeLockAction;
    }

}
