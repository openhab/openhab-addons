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
package org.openhab.binding.lametrictime.internal.api.local;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lametrictime.internal.api.local.dto.Error;
import org.openhab.binding.lametrictime.internal.api.local.dto.Failure;

/**
 * Parent class for LaMetricTime exceptions.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeException extends Exception {
    private static final long serialVersionUID = 1L;

    public LaMetricTimeException() {
    }

    public LaMetricTimeException(String message) {
        super(message);
    }

    public LaMetricTimeException(Throwable cause) {
        super(cause);
    }

    public LaMetricTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LaMetricTimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public LaMetricTimeException(Failure failure) {
        super(buildMessage(failure));
    }

    private static String buildMessage(Failure failure) {
        StringBuilder builder = new StringBuilder();

        List<Error> errors = failure.getErrors();
        if (!errors.isEmpty()) {
            builder.append(errors.get(0).getMessage());
        }

        for (int i = 1; i < errors.size(); i++) {
            builder.append("; ").append(errors.get(i).getMessage());
        }

        return builder.toString();
    }
}
