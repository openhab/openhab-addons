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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for deserialize an error entry from a JSON response using GSON.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class ErrorEntry {

    private @Nullable String date;

    @SerializedName("error_code")
    private @Nullable Integer errorCode;

    @SerializedName("error_message")
    private @Nullable String errorMessage;

    private @Nullable String time;

    private @Nullable String unix;

    /**
     * Returns the date the error happened in the format {@code dd.MM.yy}.
     *
     * @return the date the error happened in the format {@code dd.MM.yy}
     */
    public @Nullable String getDate() {
        return date;
    }

    /**
     * Returns the error code.
     *
     * @return the error code. Some codes are documented here:
     *         <a href="http://www.robonect.de/viewtopic.php?f=11&amp;t=110">
     *         http://www.robonect.de/viewtopic.php?f=11&amp;t=110</a>
     */
    public @Nullable Integer getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the localized error message from the mower.
     *
     * @return The localized error message from the mower.
     */
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the time the error happened in the format {@code HH:mm:ss}.
     *
     * @return The time the error happened in the format {@code HH:mm:ss}
     */
    public @Nullable String getTime() {
        return time;
    }

    /**
     * Returns the unix time when the error happened.
     *
     * @return The unix time when the error happened.
     */
    public @Nullable String getUnix() {
        return unix;
    }

    /**
     * Sets the error date.
     *
     * @param date the error date
     */
    public void setDate(@Nullable String date) {
        this.date = date;
    }

    /**
     * Sets the error code.
     *
     * @param errorCode the error code
     */
    public void setErrorCode(@Nullable Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Sets the localized error message.
     *
     * @param errorMessage the localized error message
     */
    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Sets the error time.
     *
     * @param time the error time
     */
    public void setTime(@Nullable String time) {
        this.time = time;
    }

    /**
     * Sets the error timestamp in seconds.
     *
     * @param unix the error timestamp in seconds
     */
    public void setUnix(@Nullable String unix) {
        this.unix = unix;
    }
}
