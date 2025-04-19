/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.ecoflow.internal.api.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class DataWrapper<T> {
    public final int code;
    public final String message;
    public final T data;

    DataWrapper(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
