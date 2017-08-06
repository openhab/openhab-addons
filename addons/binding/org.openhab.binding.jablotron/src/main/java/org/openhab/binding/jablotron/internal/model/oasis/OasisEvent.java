/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.oasis;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OasisLastEntryCID} class defines the OASIS last event
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class OasisEvent {
    private String datum;
    private long time;
    private String code;
    private String event;

    @SerializedName("ev_class")
    private String eventClass;

    public String getDatum() {
        return datum;
    }

    public long getTime() {
        return time;
    }

    public String getCode() {
        return code;
    }

    public String getEvent() {
        return event;
    }

    public String getEventClass() {
        return eventClass;
    }
}
