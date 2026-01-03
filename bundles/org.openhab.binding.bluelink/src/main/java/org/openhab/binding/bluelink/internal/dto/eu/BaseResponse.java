/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto.eu;

import com.google.gson.annotations.SerializedName;

/**
 * The base response from the EU API.
 * 
 * @param <T> the type of the result message
 * @author Florian Hotze - Initial contribution
 */
public record BaseResponse<T> (@SerializedName("retCode") String returnCode,
        @SerializedName("resCode") String responseCode, @SerializedName("resMsg") T resultMessage,
        @SerializedName("msgId") String messageId // Used in control actions
) {
}
