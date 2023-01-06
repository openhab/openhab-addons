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
package org.openhab.binding.nuki.internal.dto;

import java.util.List;

/**
 * The {@link BridgeApiCallbackListDto} class defines the Data Transfer Object (POJO)
 * for the Nuki Bridge API /callback/list endpoint.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiCallbackListDto {

    private List<BridgeApiCallbackListCallbackDto> callbacks;

    public List<BridgeApiCallbackListCallbackDto> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<BridgeApiCallbackListCallbackDto> callbacks) {
        this.callbacks = callbacks;
    }
}
