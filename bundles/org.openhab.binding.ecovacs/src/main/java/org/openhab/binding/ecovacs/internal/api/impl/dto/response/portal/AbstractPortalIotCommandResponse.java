/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class AbstractPortalIotCommandResponse {
    @SerializedName("ret")
    private final String result;

    @SerializedName("errno")
    private final int errorCode;
    @SerializedName("error")
    private final String errorMessage;

    // unused field: 'id' (string)

    public AbstractPortalIotCommandResponse(String result, int errorCode, String errorMessage) {
        this.result = result;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean wasSuccessful() {
        return "ok".equals(result);
    }

    public boolean failedDueToAuthProblem() {
        return "fail".equals(result) && errorMessage != null && errorMessage.toLowerCase().contains("auth error");
    }

    public String getErrorMessage() {
        if (wasSuccessful()) {
            return null;
        }
        return "result=" + result + ", errno=" + errorCode + ", error=" + errorMessage;
    }
}
