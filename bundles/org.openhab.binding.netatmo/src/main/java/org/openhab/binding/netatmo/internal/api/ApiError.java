/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

/**
 * The {@link ApiError} models an errored response from API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiError {
    private String message = "";
    private int code;

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
