/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ErrorType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 communication errors.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Error {
    private @SerializedName("error_code") @NonNullByDefault({}) String errorCode;
    private @NonNullByDefault({}) String description;

    public String getDescription() {
        return description;
    }

    public ErrorType getErrorType() {
        return ErrorType.of(errorCode);
    }
}
