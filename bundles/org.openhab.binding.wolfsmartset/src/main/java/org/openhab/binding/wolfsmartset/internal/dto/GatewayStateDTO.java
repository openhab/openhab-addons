
package org.openhab.binding.wolfsmartset.internal.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class GatewayStateDTO {

    @SerializedName("GatewayId")
    @Expose
    private Integer gatewayId;
    @SerializedName("IsOnline")
    @Expose
    private Boolean isOnline;
    @SerializedName("GatewayOfflineCause")
    @Expose
    private Integer gatewayOfflineCause;
    @SerializedName("IsLocked")
    @Expose
    private Boolean isLocked;
    @SerializedName("IsDeleted")
    @Expose
    private Boolean isDeleted;
    @SerializedName("IsBusy")
    @Expose
    private Boolean isBusy;
    @SerializedName("ImageName")
    @Expose
    private String imageName;

    public Integer getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Integer gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Integer getGatewayOfflineCause() {
        return gatewayOfflineCause;
    }

    public void setGatewayOfflineCause(Integer gatewayOfflineCause) {
        this.gatewayOfflineCause = gatewayOfflineCause;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsBusy() {
        return isBusy;
    }

    public void setIsBusy(Boolean isBusy) {
        this.isBusy = isBusy;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
