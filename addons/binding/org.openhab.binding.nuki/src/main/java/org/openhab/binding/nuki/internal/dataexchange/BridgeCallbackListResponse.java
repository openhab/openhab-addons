/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import java.util.List;

import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListDto;

/**
 * The {@link BridgeCallbackListResponse} class wraps {@link BridgeApiCallbackListDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeCallbackListResponse extends NukiBaseResponse {

    private List<BridgeApiCallbackListCallbackDto> callbacks;

    public BridgeCallbackListResponse(int status, String message, BridgeApiCallbackListDto bridgeApiCallbackListDto) {
        super(status, message);
        if (bridgeApiCallbackListDto != null) {
            this.setSuccess(true);
            this.callbacks = bridgeApiCallbackListDto.getCallbacks();
        }
    }

    public BridgeCallbackListResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public List<BridgeApiCallbackListCallbackDto> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<BridgeApiCallbackListCallbackDto> callbacks) {
        this.callbacks = callbacks;
    }

}
