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

import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackAddDto;

/**
 * The {@link BridgeCallbackAddResponse} class wraps {@link BridgeApiCallbackAddDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeCallbackAddResponse extends NukiBaseResponse {

    public BridgeCallbackAddResponse(int status, String message, BridgeApiCallbackAddDto bridgeApiCallbackAddDto) {
        super(status, message);
        if (bridgeApiCallbackAddDto != null) {
            this.setSuccess(bridgeApiCallbackAddDto.isSuccess());
            if (bridgeApiCallbackAddDto.getMessage() != null) {
                this.setMessage(bridgeApiCallbackAddDto.getMessage());
            }
        }
    }

    public BridgeCallbackAddResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }
}
