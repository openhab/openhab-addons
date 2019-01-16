/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

import java.util.Calendar;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FreeboxCallEntry} is the Java class used to map the "CallEntry"
 * structure used by the call API
 * https://dev.freebox.fr/sdk/os/call/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxCallEntry {
    private static final String CALL_ENTRY_TYPE_ACCEPTED = "accepted";
    private static final String CALL_ENTRY_TYPE_MISSED = "missed";
    private static final String CALL_ENTRY_TYPE_OUTGOING = "outgoing";

    private int id;
    private String type;
    private long datetime;
    private String number;
    private String name;
    private int duration;
    @SerializedName("new")
    private boolean newCall;
    private int contactId;

    public Calendar getTimeStamp() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(datetime * 1000);
        return c;
    }

    public boolean isAccepted() {
        return CALL_ENTRY_TYPE_ACCEPTED.equalsIgnoreCase(type);
    }

    public boolean isMissed() {
        return CALL_ENTRY_TYPE_MISSED.equalsIgnoreCase(type);
    }

    public boolean isOutGoing() {
        return CALL_ENTRY_TYPE_OUTGOING.equalsIgnoreCase(type);
    }

    public int getId() {
        return id;
    }

    public String getType() {
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
