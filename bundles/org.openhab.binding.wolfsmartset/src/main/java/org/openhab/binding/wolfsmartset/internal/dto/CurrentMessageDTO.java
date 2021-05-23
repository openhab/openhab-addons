
package org.openhab.binding.wolfsmartset.internal.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class CurrentMessageDTO {

    @SerializedName("Id")
    @Expose
    private Integer id;
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("OccurTimeLocal")
    @Expose
    private String occurTimeLocal;
    @SerializedName("Active")
    @Expose
    private Boolean active;
    @SerializedName("Index")
    @Expose
    private Integer index;
    @SerializedName("Device")
    @Expose
    private String device;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOccurTimeLocal() {
        return occurTimeLocal;
    }

    public void setOccurTimeLocal(String occurTimeLocal) {
        this.occurTimeLocal = occurTimeLocal;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
