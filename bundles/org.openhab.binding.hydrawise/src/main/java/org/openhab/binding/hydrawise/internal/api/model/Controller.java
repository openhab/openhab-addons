
/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Controller {

    private String name;

    private Integer lastContact;

    private String serialNumber;

    private Integer controllerId;

    private String swVersion;

    private String hardware;

    private Boolean isBoc;

    private String address;

    private String timezone;

    private Integer deviceId;

    private Object parentDeviceId;

    private String image;

    private String description;

    private Integer customerId;

    private Double latitude;

    private Double longitude;

    private String lastContactReadable;

    private String status;

    private String statusIcon;

    private Boolean online;

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