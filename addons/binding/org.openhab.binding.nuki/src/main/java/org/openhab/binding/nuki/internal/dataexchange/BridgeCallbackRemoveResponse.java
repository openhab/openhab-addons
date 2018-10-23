/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackRemoveDto;

/**
 * The {@link BridgeCallbackRemoveResponse} class wraps {@link BridgeApiCallbackRemoveDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeCallbackRemoveResponse extends NukiBaseResponse {

    private BridgeApiCallbackRemoveDto bridgeCallbackRemove;

    public BridgeCallbackRemoveResponse(int status, String message) {
        super(status, message);
    }

    public BridgeCallbackRemoveResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public BridgeApiCallbackRemoveDto getBridgeCallbackRemove() {
        return bridgeCallbackRemove;
    }

    public void setBridgeCallbackRemove(BridgeApiCallbackRemoveDto bridgeCallbackRemove) {
        this.bridgeCallbackRemove = bridgeCallbackRemove;
    }

}
