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
package org.openhab.binding.tapocontrol.internal.structures;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@TapoSubRequest} holds data sent to device in order to act on a child
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record TapoSubRequest(String method, Object params) {
    private record ChildRequest(String device_id, @SerializedName("requestData") TapoSubRequest requestData) {
    }

    private record SubMultiple(List<TapoSubRequest> requests) {

        private SubMultiple(String method, TapoChild params) {
            this(List.of(new TapoSubRequest(method, params)));
        }
    }

    public TapoSubRequest(String deviceId, String method, TapoChild params) {
        this(DEVICE_CMD_CONTROL_CHILD, new ChildRequest(deviceId,
                new TapoSubRequest(DEVICE_CMD_MULTIPLE_REQ, new SubMultiple(method, params))));
    }
}
