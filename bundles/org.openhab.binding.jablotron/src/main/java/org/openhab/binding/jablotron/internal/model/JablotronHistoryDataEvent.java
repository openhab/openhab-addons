/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronHistoryDataEvent} class defines the event object for the
 * getEventHistory response
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronHistoryDataEvent {
    @SerializedName("icon-type")
    String iconType = "";

    @SerializedName("event-text")
    String eventText = "";

    @SerializedName("invoker-name")
    String invokerName = "";

    @SerializedName("section-name")
    String sectionName = "";

    String date = "";

    public String getIconType() {
        return iconType;
    }

    public String getEventText() {
        return eventText;
    }

    public String getDate() {
        return date;
    }

    public String getInvokerName() {
        return invokerName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public void setEventText(String eventText) {
        this.eventText = eventText;
    }

    public void setInvokerName(String invokerName) {
        this.invokerName = invokerName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
