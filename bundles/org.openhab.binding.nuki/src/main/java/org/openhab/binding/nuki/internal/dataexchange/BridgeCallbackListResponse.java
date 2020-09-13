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
