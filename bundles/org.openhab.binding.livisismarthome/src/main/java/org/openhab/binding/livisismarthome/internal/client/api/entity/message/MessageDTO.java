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
package org.openhab.binding.livisismarthome.internal.client.api.entity.message;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the structure of a {@link MessageDTO}. Messages are part of the LIVISI SmartHome system and besides other
 * things
 * are used
 * to raise battery warnings.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class MessageDTO {

    /** device related messages */
    public static final String TYPE_DEVICE_UNREACHABLE = "DeviceUnreachable";
    public static final String TYPE_DEVICE_LOW_BATTERY = "DeviceLowBattery";

    /**
     * Identifier of the message – must be unique.
     */
    private String id;

    /**
     * Specifies the type of the message.
     */
    private String type;

    /**
     * Defines whether the message has been viewed by a user.
     */
    @SerializedName("read")
    private boolean isRead;

    /**
     * Defines whether it is an alert or a message, default is message.
     */
    @SerializedName("class")
    private String messageClass;

    /**
     * Timestamp when the message was created.
     *
     * Optional.
     */
    private String timestamp;

    /**
     * Reference to the underlying devices, which the message relates to.
     *
     * Optional.
     */
    private List<String> devices;

    /**
     * Container for all parameters of the message. The parameters are contained in Property entities.
     *
     * Optional.
     */
    private MessagePropertiesDTO properties;

    /**
     * The product (context) that generated the message.
     */
    private String namespace;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the messageClass
     */
    public String getMessageClass() {
        return messageClass;
    }

    /**
     * @param messageClass the messageClass to set
     */
    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the isRead
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * @param isRead the isRead to set
     */
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    /**
     * @return the devices
     */
    public List<String> getDevices() {
        return devices;
    }

    /**
     * @param devices the devices to set
     */
    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    /**
     * @return the dataPropertyList
     */
    public MessagePropertiesDTO getProperties() {
        return properties;
    }

    /**
     * @param properties the dataPropertyList to set
     */
    public void setProperties(MessagePropertiesDTO properties) {
        this.properties = properties;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns true, if the message is of type "DeviceUnreachable".
     *
     * @return true if the message is of type "DeviceUnreachable", otherwise false
     */
    public boolean isTypeDeviceUnreachable() {
        return TYPE_DEVICE_UNREACHABLE.equals(type);
    }

    /**
     * Returns true, if the message is of type "DeviceLowBattery".
     *
     * @return true if the message is of type "DeviceLowBattery", otherwise false
     */
    public boolean isTypeDeviceLowBattery() {
        return TYPE_DEVICE_LOW_BATTERY.equals(type);
    }
}
