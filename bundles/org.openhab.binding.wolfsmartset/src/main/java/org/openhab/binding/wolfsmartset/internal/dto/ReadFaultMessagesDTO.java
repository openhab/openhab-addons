
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
