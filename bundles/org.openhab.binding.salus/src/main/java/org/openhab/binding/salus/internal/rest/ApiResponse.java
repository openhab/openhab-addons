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
package org.openhab.binding.salus.internal.rest;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public record ApiResponse<T> (T body, Error error) {
    public static <T> ApiResponse<T> ok(T body) {
        return new ApiResponse<>(body, null);
    }

    public static <T> ApiResponse<T> error(Error error) {
        return new ApiResponse<>(null, error);
    }

    public ApiResponse {
        if (body != null && error != null) {
            throw new IllegalArgumentException("body and error cannot be both present");
        }
        if (body == null && error == null) {
            throw new IllegalArgumentException("body and error cannot be both null");
        }
    }

    public boolean succeed() {
        return body != null;
    }

    public boolean failed() {
        return !succeed();
    }
}
