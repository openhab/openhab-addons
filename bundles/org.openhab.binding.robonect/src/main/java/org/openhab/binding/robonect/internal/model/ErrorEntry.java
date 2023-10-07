/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * POJO for deserialize an error entry from a JSON response using GSON.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class ErrorEntry {

    private String date;

    @SerializedName("error_code")
    private Integer errorCode;

    @SerializedName("error_message")
    private String errorMessage;

    private String time;

    private String unix;

    /**
     * @return - the date the error happend in the format "dd.MM.yy"
     */
    public String getDate() {
        return date;
    }

    /**
     * @return - the error code. Some codes are documented here:
     *         <a href="http://www.robonect.de/viewtopic.php?f=11&t=110">
     *         http://www.robonect.de/viewtopic.php?f=11&amp;t=110</a>
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @return - The localized error message from the mower.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return - The time the error happened in the format "HH:mm:ss"
     */
    public String getTime() {
        return time;
    }

    /**
     * @return - The unix time when the error happened.
     */
    public String getUnix() {
        return unix;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnix(String unix) {
        this.unix = unix;
    }
}
