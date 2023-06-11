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
package org.openhab.binding.boschindego.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschindego.internal.dto.request.SetStateRequest;

/**
 * Commands supported by the device.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum DeviceCommand {

    MOW(SetStateRequest.STATE_MOW),
    PAUSE(SetStateRequest.STATE_PAUSE),
    RETURN(SetStateRequest.STATE_RETURN);

    private String actionCode;

    DeviceCommand(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionCode() {
        return actionCode;
    }
}
