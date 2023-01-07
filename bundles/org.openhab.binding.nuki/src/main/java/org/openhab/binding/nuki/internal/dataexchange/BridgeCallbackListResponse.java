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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListDto;

/**
 * The {@link BridgeCallbackListResponse} class wraps {@link BridgeApiCallbackListDto} class.
 *
 * @author Markus Katter - Initial contribution
 */
@NonNullByDefault
public class BridgeCallbackListResponse extends NukiBaseResponse {

    private List<BridgeApiCallbackListCallbackDto> callbacks = Collections.emptyList();

    public BridgeCallbackListResponse(int status, String message,
            @Nullable BridgeApiCallbackListDto bridgeApiCallbackListDto) {
        super(status, message);
        if (bridgeApiCallbackListDto != null) {
            this.setSuccess(true);
            if (bridgeApiCallbackListDto.getCallbacks() != null) {
                this.callbacks = bridgeApiCallbackListDto.getCallbacks();
            }
        }
    }

    public BridgeCallbackListResponse(NukiBaseResponse nukiBaseResponse) {
        super(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage());
    }

    public List<BridgeApiCallbackListCallbackDto> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(@Nullable List<BridgeApiCallbackListCallbackDto> callbacks) {
        if (callbacks == null) {
            this.callbacks = new ArrayList<>();
        } else {
            this.callbacks = callbacks;
        }
    }
}
