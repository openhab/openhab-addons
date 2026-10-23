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
package org.openhab.binding.bmwcardata.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link BMWCarDataException} signals a failed communication with the BMW CarData services.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataException extends Exception {

    private static final long serialVersionUID = 1L;

    /** BMW's error id for an exhausted daily request quota. */
    public static final String ERROR_RATE_LIMIT = "CU-429";

    /** HTTP status code of the failed request, or 0 if no response was received. */
    private final int httpStatus;
    /** BMW's error id from the response body, e.g. {@code CU-402}, if the response carried one. */
    private final @Nullable String errorId;
    private final boolean budgetExhausted;

    public BMWCarDataException(String message) {
        this(message, 0, null, null, false);
    }

    public BMWCarDataException(String message, @Nullable Throwable cause) {
        this(message, 0, null, cause, false);
    }

    public BMWCarDataException(String message, int httpStatus) {
        this(message, httpStatus, null, null, false);
    }

    public BMWCarDataException(String message, int httpStatus, @Nullable Throwable cause) {
        this(message, httpStatus, null, cause, false);
    }

    public BMWCarDataException(String message, int httpStatus, @Nullable String errorId) {
        this(message, httpStatus, errorId, null, false);
    }

    private BMWCarDataException(String message, int httpStatus, @Nullable String errorId, @Nullable Throwable cause,
            boolean budgetExhausted) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorId = errorId;
        this.budgetExhausted = budgetExhausted;
    }

    /**
     * @return an exception for a request the binding did not send, because its request budget is used up
     */
    static BMWCarDataException budgetExhausted(int used, int budget) {
        return new BMWCarDataException("Request budget used up: " + used + " of " + budget + " in the last 24 hours", 0,
                null, null, true);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public @Nullable String getErrorId() {
        return errorId;
    }

    /**
     * @return whether no more requests can be sent for now, by BMW's limit or by the binding's own budget
     */
    public boolean isRateLimited() {
        return budgetExhausted || httpStatus == 429 || ERROR_RATE_LIMIT.equals(errorId);
    }

    /**
     * @return whether the binding withheld the request to stay within its request budget
     */
    public boolean isBudgetExhausted() {
        return budgetExhausted;
    }
}
