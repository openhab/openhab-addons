/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackAddDto;

/**
 * The {@link BridgeCallbackAddResponse} class wraps {@link BridgeApiCallbackAddDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeCallbackAddResponse extends NukiBaseResponse {

    private BridgeApiCallbackAddDto bridgeCallbackAdd;

    public BridgeCallbackAddResponse(int status, String message) {
        super(status, message);
    }

    public BridgeCallbackAddResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public BridgeApiCallbackAddDto getBridgeCallbackAdd() {
        return bridgeCallbackAdd;
    }

    public void setBridgeCallbackAdd(BridgeApiCallbackAddDto bridgeCallbackAdd) {
        this.bridgeCallbackAdd = bridgeCallbackAdd;
    }

}
