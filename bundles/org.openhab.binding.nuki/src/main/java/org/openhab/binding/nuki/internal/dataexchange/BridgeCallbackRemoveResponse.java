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

import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackRemoveDto;

/**
 * The {@link BridgeCallbackRemoveResponse} class wraps {@link BridgeApiCallbackRemoveDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeCallbackRemoveResponse extends NukiBaseResponse {

    public BridgeCallbackRemoveResponse(int status, String message,
            BridgeApiCallbackRemoveDto bridgeApiCallbackRemoveDto) {
        super(status, message);
        if (bridgeApiCallbackRemoveDto != null) {
            this.setSuccess(bridgeApiCallbackRemoveDto.isSuccess());
            if (bridgeApiCallbackRemoveDto.getMessage() != null) {
                this.setMessage(bridgeApiCallbackRemoveDto.getMessage());
            }
        }
    }

    public BridgeCallbackRemoveResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }
}
