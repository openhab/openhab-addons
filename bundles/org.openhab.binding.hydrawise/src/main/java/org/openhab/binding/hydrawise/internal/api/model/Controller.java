package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Controller {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("last_contact")
    @Expose
    private Integer lastContact;
    @SerializedName("serial_number")
    @Expose
    private String serialNumber;
    @SerializedName("controller_id")
    @Expose
    private Integer controllerId;
    @SerializedName("sw_version")
    @Expose
    private String swVersion;
    @SerializedName("hardware")
    @Expose
    private String hardware;
    @SerializedName("is_boc")
    @Expose
    private Boolean isBoc;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("device_id")
    @Expose
    private Integer deviceId;
    @SerializedName("parent_device_id")
    @Expose
    private Object parentDeviceId;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("customer_id")
    @Expose
    private Integer customerId;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("last_contact_readable")
    @Expose
    private String lastContactReadable;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_icon")
    @Expose
    private String statusIcon;
    @SerializedName("online")
    @Expose
    private Boolean online;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public Integer getLastContact() {
        return lastContact;
    }

    /**
     * @param lastContact
     */
    public void setLastContact(Integer lastContact) {
        this.lastContact = lastContact;
    }

    /**
     * @return
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return
     */
    public Integer getControllerId() {
        return controllerId;
    }

    /**
     * @param controllerId
     */
    public void setControllerId(Integer controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * @return
     */
    public String getSwVersion() {
        return swVersion;
    }

    /**
     * @param swVersion
     */
    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    /**
     * @return
     */
    public String getHardware() {
        return hardware;
    }

    /**
     * @param hardware
     */
    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    /**
     * @return
     */
    public Boolean getIsBoc() {
        return isBoc;
    }

    /**
     * @param isBoc
     */
    public void setIsBoc(Boolean isBoc) {
        this.isBoc = isBoc;
    }

    /**
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * @param timezone
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * @return
     */
    public Integer getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId
     */
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return
     */
    public Object getParentDeviceId() {
        return parentDeviceId;
    }

    /**
     * @param parentDeviceId
     */
    public void setParentDeviceId(Object parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    /**
     * @return
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public Integer getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * @return
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return
     */
    public String getLastContactReadable() {
        return lastContactReadable;
    }

    /**
     * @param lastContactReadable
     */
    public void setLastContactReadable(String lastContactReadable) {
        this.lastContactReadable = lastContactReadable;
    }

    /**
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return
     */
    public String getStatusIcon() {
        return statusIcon;
    }

    /**
     * @param statusIcon
     */
    public void setStatusIcon(String statusIcon) {
        this.statusIcon = statusIcon;
    }

    /**
     * @return
     */
    public Boolean getOnline() {
        return online;
    }

    /**
     * @param online
     */
    public void setOnline(Boolean online) {
        this.online = online;
    }

    /**
     * @return
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}