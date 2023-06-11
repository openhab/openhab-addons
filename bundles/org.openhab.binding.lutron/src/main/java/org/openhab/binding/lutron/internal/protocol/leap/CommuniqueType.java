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
package org.openhab.binding.lutron.internal.protocol.leap;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP CommuniqueType enum
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public enum CommuniqueType {
    // Requests
    @SerializedName("CreateRequest")
    CREATEREQUEST("CreateRequest"),
    @SerializedName("ReadRequest")
    READREQUEST("ReadRequest"),
    @SerializedName("UpdateRequest")
    UPDATEREQUEST("UpdateRequest"),
    @SerializedName("DeleteRequest")
    DELETEREQUEST("DeleteRequest"), // ?
    @SerializedName("SubscribeRequest")
    SUBSCRIBEREQUEST("SubscribeRequest"),
    @SerializedName("UnubscribeRequest")
    UNSUBSCRIBEREQUEST("UnubscribeRequest"),
    @SerializedName("Execute")
    EXECUTEREQUEST("Execute"),

    // Responses
    @SerializedName("CreateResponse")
    CREATERESPONSE("CreateResponse"),
    @SerializedName("ReadResponse")
    READRESPONSE("ReadResponse"),
    @SerializedName("UpdateResponse")
    UPDATERESPONSE("UpdateResponse"),
    @SerializedName("DeleteResponse")
    DELETERESPONSE("DeleteResponse"), // ?
    @SerializedName("SubscribeResponse")
    SUBSCRIBERESPONSE("SubscribeResponse"),
    @SerializedName("UnsubscribeResponse")
    UNSUBSCRIBERESPONSE("UnsubscribeResponse"),
    @SerializedName("ExecuteResponse")
    EXECUTERESPONSE("ExecuteResponse"), // ?
    @SerializedName("ExceptionResponse")
    EXCEPTIONRESPONSE("ExceptionResponse");

    private final transient String string;

    CommuniqueType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
