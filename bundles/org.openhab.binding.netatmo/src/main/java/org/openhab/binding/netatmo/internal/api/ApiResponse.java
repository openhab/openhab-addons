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
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ApiResponse} models a response returned by API call
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiResponse<T> {
    /**
     * The {@link Ok} models a response that only holds the result of the request sent to the API
     */
    static class Ok extends ApiResponse<String> {
        private static final String SUCCESS = "ok";

        boolean failed() {
            return !SUCCESS.equals(getStatus());
        }
    }

    private String status = "";
    private @Nullable T body;

    public String getStatus() {
        return status;
    }

    public @Nullable T getBody() {
        return body;
    }
}
