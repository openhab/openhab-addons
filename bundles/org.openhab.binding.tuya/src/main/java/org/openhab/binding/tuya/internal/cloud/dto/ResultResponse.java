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
package org.openhab.binding.tuya.internal.cloud.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResultResponse} encapsulates the Tuya Cloud Response
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ResultResponse<T> {
    public boolean success = false;
    public long code = 0;
    @SerializedName("t")
    public long timestamp = 0;

    public @Nullable String msg;
    public @Nullable T result;

    @Override
    public String toString() {
        return "Result{timestamp=" + timestamp + ", code=" + code + ", msg=" + msg + ", success=" + success
                + ", result=" + result + "}";
    }
}
