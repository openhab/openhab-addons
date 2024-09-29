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
package org.openhab.binding.openweathermap.internal.dto.onecall;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>alerts</code> object of the JSON response of the One Call APIs.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Alert {
    private String event;
    private int start;
    private int end;
    private String description;
    @SerializedName("sender_name")
    private String senderName;

    public String getEvent() {
        return event;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getDescription() {
        return description;
    }

    public String getSenderName() {
        return senderName;
    }
}
