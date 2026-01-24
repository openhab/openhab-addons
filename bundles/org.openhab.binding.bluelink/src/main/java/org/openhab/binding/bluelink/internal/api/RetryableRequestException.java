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
package org.openhab.binding.bluelink.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception indicating a retryable request failure, such as a 5xx HTTP status.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class RetryableRequestException extends BluelinkApiException {
    private static final long serialVersionUID = 1L;

    public RetryableRequestException(final String message) {
        super(message);
    }

    public RetryableRequestException(final String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
