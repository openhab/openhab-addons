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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

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
    /**
     * BMW's error id for a VIN the client has no permission for, e.g. as it is not mapped to the customer as primary
     * user or the vehicle cannot send data.
     */
    public static final String ERROR_VIN_NOT_PERMITTED = "CU-104";
    /** BMW's error id when the customer has reached the maximum of 10 telematic data containers. */
    public static final String ERROR_CONTAINER_LIMIT = "CU-124";
    /**
     * BMW's error ids for a container id that cannot be used: no permission (not allocated to the customer or not
     * existing), invalid format, or already set for deletion. BMW does not document the HTTP status they come with.
     */
    private static final Set<String> ERRORS_UNUSABLE_CONTAINER = Set.of("CU-105", "CU-121", "CU-122");
    // the format of BMW's error ids, so that only such an id from the response ends up in a status description
    private static final Pattern ERROR_ID_FORMAT = Pattern.compile("[A-Z]{2,4}-\\d{3}");

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
     * @return whether the request failed on BMW's side or on the way there, e.g. a 502 of BMW's gateway or a
     *         timeout, so that the same request may succeed later
     */
    public boolean isTransient() {
        if (httpStatus >= 500) {
            return true;
        }
        Throwable cause = getCause();
        return httpStatus == 0 && (cause instanceof TimeoutException || cause instanceof ExecutionException);
    }

    /**
     * @return a short description of the failure as argument of a translated status description, e.g.
     *         {@code HTTP 403 CU-104}; it contains no comma, bracket or quote, which separate the arguments there
     */
    public String getShortDescription() {
        if (httpStatus > 0) {
            String id = errorId;
            return id != null && ERROR_ID_FORMAT.matcher(id).matches() ? "HTTP " + httpStatus + " " + id
                    : "HTTP " + httpStatus;
        }
        if (budgetExhausted) {
            return "request budget used up";
        }
        Throwable cause = getCause();
        if (cause instanceof TimeoutException) {
            return "timeout";
        }
        if (cause instanceof ExecutionException || cause instanceof IOException
                || cause instanceof InterruptedException) {
            return "no response";
        }
        // BMW answered, but the response is malformed, incomplete or not acceptable, e.g. a link other than https
        return "invalid response";
    }

    /**
     * @return whether the client has no permission for the VIN, which retrying does not change
     */
    public boolean isVinNotPermitted() {
        return ERROR_VIN_NOT_PERMITTED.equals(errorId);
    }

    /**
     * @return whether the container id of the request cannot be used anymore, e.g. as the container was deleted
     */
    public boolean isContainerUnusable() {
        String id = errorId;
        return httpStatus == 404 || (id != null && ERRORS_UNUSABLE_CONTAINER.contains(id));
    }

    /**
     * @return whether the binding withheld the request to stay within its request budget
     */
    public boolean isBudgetExhausted() {
        return budgetExhausted;
    }
}
