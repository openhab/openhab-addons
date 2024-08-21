/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The super class of all answers from the robonect module. All answersd derive from this class. An answer is either
 * successful where all the information of the subclass will be filled, or it is not successful, and this class will
 * hold the error information.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectAnswer {

    private boolean successful;

    @SerializedName("error_code")
    private Integer errorCode;

    @SerializedName("error_message")
    private String errorMessage;

    /**
     * @return - true if the request was successful, false otherwise.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * allows to set the successful status for testing.
     * 
     * @param successful
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @return - in case of a not successful request, the error code, null otherwise.
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @return - in case of a not successful request, the error message, null otherwise.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
