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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HeadStatus} is responsible for storing
 * the "status" node from the {@link Head}
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class HeadStatus {
    @SerializedName("Code")
    private int code;
    @SerializedName("Reason")
    private String reason;
    @SerializedName("UserMessage")
    private String userMessage;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        if (reason == null) {
            reason = "";
        }
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUserMessage() {
        if (userMessage == null) {
            userMessage = "";
        }
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
}
