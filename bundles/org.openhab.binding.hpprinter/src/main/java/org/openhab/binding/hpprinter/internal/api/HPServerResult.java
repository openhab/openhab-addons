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
package org.openhab.binding.hpprinter.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HPServerResult} is responsible for returning the
 * reading of data from the HP Embedded Web Server.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPServerResult<result> {
    private final RequestStatus status;
    private final @Nullable result data;
    private final String errorMessage;

    public HPServerResult(RequestStatus status, @Nullable String errorMessage) {
        this.status = status;
        this.data = null;
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    public HPServerResult(result data) {
        this.status = RequestStatus.SUCCESS;
        this.data = data;
        this.errorMessage = "";
    }

    @SuppressWarnings("null")
    public result getData() {
        final result localData = data;
        if (status != RequestStatus.SUCCESS || localData == null) {
            throw new IllegalStateException("No data available for result");
        }
        return localData;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public enum RequestStatus {
        SUCCESS,
        TIMEOUT,
        ERROR
    }
}
