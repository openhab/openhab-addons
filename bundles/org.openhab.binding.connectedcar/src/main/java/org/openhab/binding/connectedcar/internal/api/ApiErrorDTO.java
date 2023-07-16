/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_STATUS_CLASS_SECURUTY;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApi;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNApiError1;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNApiError2;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNApiError2.CNErrorMessage2;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNErrorMessage2Details;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPErrorResponse;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCActionResponse.WCApiError;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCActionResponse.WCApiError2;

/**
 * The {@link CarNetApi} implements the http based API access to CarNet
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class ApiErrorDTO {
    /*
     * {
     * "error":"invalid_request",
     * "error_description": "Missing Username"
     * }
     */
    public String error = "";
    public String code = "";
    public String description = "";
    public @Nullable CNErrorMessage2Details details = new CNErrorMessage2Details();

    public ApiErrorDTO() {
    }

    public ApiErrorDTO(CNApiError1 format1) {
        error = getString(format1.error);
        code = getString(format1.code);
        description = getString(format1.description);
    }

    public ApiErrorDTO(CNApiError2 format2) {
        if (format2 != null) {
            CNErrorMessage2 error2 = format2.error;
            if (error2 != null) {
                error = getString(error2.error);
                code = getString(error2.code);
                description = getString(error2.description);
                details = error2.details;
            }
        }
    }

    public ApiErrorDTO(WCApiError wcerror) {
        if (wcerror != null && wcerror.error != null) {
            code = getInteger(wcerror.error.code).toString();
            error = getString(wcerror.error.message);
            description = getString(wcerror.error.info) + ", retry=" + (wcerror.error.retry);
        }
    }

    public ApiErrorDTO(WCApiError2 wcerror) {
        if (wcerror != null) {
            code = getString(wcerror.status);
            error = getString(wcerror.message);
            description = getString(wcerror.uri);
        }
    }

    public ApiErrorDTO(FPErrorResponse fperror) {
        if (fperror != null) {
            code = getString(fperror.response.status);
            error = getString(fperror.response.error.message);
            description = "";
        }
    }

    public boolean isValid() {
        return !code.isEmpty() || !error.isEmpty() || !description.isEmpty();
    }

    public boolean isError() {
        return !code.isEmpty() || !error.isEmpty();
    }

    public boolean isApiThrottle() {
        // {"error":{"errorCode":"VSR.technical.9025","description":"TSS responded: 429 -"}}
        return code.contains("technical.9025");
    }

    public boolean isTechValidationError() {
        return code.contains("technical.9026");
    }

    public boolean isOpAlreadyInProgress() {
        return code.contains("business.1003");
    }

    public boolean isSecurityClass() {
        return description.contains(API_STATUS_CLASS_SECURUTY);
    }

    @Override
    public String toString() {
        return description + " (" + code + " " + error + ")";
    }

    private String getString(@Nullable String s) {
        return s != null ? s : "";
    }
}
