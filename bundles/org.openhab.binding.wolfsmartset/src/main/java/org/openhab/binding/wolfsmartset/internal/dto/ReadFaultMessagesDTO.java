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
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
@Generated("jsonschema2pojo")
public class ReadFaultMessagesDTO {

    @SerializedName("MessagesChanged")
    @Expose
    private Boolean messagesChanged;
    @SerializedName("LastRead")
    @Expose
    private String lastRead;
    @SerializedName("CurrentMessages")
    @Expose
    private List<CurrentMessageDTO> currentMessages = null;
    @SerializedName("HistoryMessages")
    @Expose
    private List<HistoryMessageDTO> historyMessages = null;
    @SerializedName("DeviceFaultMessages")
    @Expose
    private List<Object> deviceFaultMessages = null;

    public Boolean getMessagesChanged() {
        return messagesChanged;
    }

    public void setMessagesChanged(Boolean messagesChanged) {
        this.messagesChanged = messagesChanged;
    }

    public String getLastRead() {
        return lastRead;
    }

    public void setLastRead(String lastRead) {
        this.lastRead = lastRead;
    }

    public List<CurrentMessageDTO> getCurrentMessages() {
        return currentMessages;
    }

    public void setCurrentMessages(List<CurrentMessageDTO> currentMessages) {
        this.currentMessages = currentMessages;
    }

    public List<HistoryMessageDTO> getHistoryMessages() {
        return historyMessages;
    }

    public void setHistoryMessages(List<HistoryMessageDTO> historyMessages) {
        this.historyMessages = historyMessages;
    }

    public List<Object> getDeviceFaultMessages() {
        return deviceFaultMessages;
    }

    public void setDeviceFaultMessages(List<Object> deviceFaultMessages) {
        this.deviceFaultMessages = deviceFaultMessages;
    }
}
