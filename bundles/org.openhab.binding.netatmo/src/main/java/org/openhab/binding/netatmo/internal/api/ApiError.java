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
package org.openhab.binding.netatmo.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.ServiceError;

/**
 * The {@link ApiError} models an errored response from API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiError {
    private class Body {
        private String message = "";
        private ServiceError code = ServiceError.UNKNOWN;
    }

    private Body error = new Body();

    public String getMessage() {
        return error.message;
    }

    public ServiceError getCode() {
        return error.code;
    }
}
