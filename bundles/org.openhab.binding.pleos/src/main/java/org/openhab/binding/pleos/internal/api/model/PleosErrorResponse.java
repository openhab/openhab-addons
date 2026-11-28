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
package org.openhab.binding.pleos.internal.api.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Data transfer record representing the standard error response structure of the Pleos API.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public record PleosErrorResponse(@Nullable ErrorBody error) {

    public record ErrorBody(@Nullable String code, @Nullable String message, @Nullable String requestId,
            @Nullable List<ErrorDetail> details) {
    }

    public record ErrorDetail(@Nullable String field, @Nullable String issue) {
    }
}
