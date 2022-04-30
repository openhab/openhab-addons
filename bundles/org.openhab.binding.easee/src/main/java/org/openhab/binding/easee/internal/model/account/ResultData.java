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
package org.openhab.binding.easee.internal.model.account;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.model.GenericErrorResponse;

/**
 * data class which can contain success response or error response of authentication command
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ResultData {
    private @Nullable AuthenticationDataResponse authenticationResponse;
    private @Nullable GenericErrorResponse errorResponse;

    public @Nullable AuthenticationDataResponse getSuccessResponse() {
        return authenticationResponse;
    }

    public void setSuccessResponse(@Nullable AuthenticationDataResponse successResponse) {
        this.authenticationResponse = successResponse;
    }

    public @Nullable GenericErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(@Nullable GenericErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
