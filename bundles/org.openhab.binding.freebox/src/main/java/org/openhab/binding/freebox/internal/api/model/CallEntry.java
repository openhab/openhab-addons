/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CallEntry} is the Java class used to map the "CallEntry"
 * structure used by the call API
 * https://dev.freebox.fr/sdk/os/call/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class CallEntry {
    public static enum CallType {
        UNKNOWN,
        @SerializedName("accepted")
        ACCEPTED,
        @SerializedName("missed")
        MISSED,
        @SerializedName("outgoing")
        OUTGOING,
        INCOMING;
    }

    private int id;
    private CallType type = CallType.UNKNOWN;
    private long datetime; // Call creation timestamp.
    private String number = "";
    private String name = "";
    private int duration; // Call duration in seconds.
    @SerializedName("new")
    private boolean newCall;
    private int contactId;

    public int getId() {
        return id;
    }

    public CallType getType() {
        if (type == CallType.MISSED && duration == 0) {
            type = CallType.INCOMING;
        }
        return type;
    }

    public long getDatetime() {
        return datetime;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isNewCall() {
        return newCall;
    }

    public int getContactId() {
        return contactId;
    }

}
