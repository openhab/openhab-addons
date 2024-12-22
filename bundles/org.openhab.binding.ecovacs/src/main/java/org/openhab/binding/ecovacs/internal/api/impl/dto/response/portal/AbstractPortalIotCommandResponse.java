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
    private final Object errorObject; // might be a string or a JSON object

    public AbstractPortalIotCommandResponse(String result, int errorCode, Object errorObject) {
        this.result = result;
        this.errorCode = errorCode;
        this.errorObject = errorObject;
    }

    public boolean wasSuccessful() {
        return "ok".equals(result);
    }

    public boolean failedDueToAuthProblem() {
        if (!"fail".equals(result)) {
            return false;
        }
        if (errorCode == 3) {
            // Error 3 is 'OAuth error'
            return true;
        }
        String errorMessage = errorObject != null ? errorObject.toString().toLowerCase() : "";
        return errorMessage.contains("auth error") || errorMessage.contains("token error");
    }

    public String getErrorMessage() {
        if (wasSuccessful()) {
            return null;
        }
        String errorMessage = errorObject != null ? errorObject.toString() : null;
        return "result=" + result + ", errno=" + errorCode + ", error=" + errorMessage;
    }
}
