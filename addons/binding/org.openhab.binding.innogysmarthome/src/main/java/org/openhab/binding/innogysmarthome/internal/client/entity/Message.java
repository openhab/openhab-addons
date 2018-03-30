/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity;

import java.util.List;

import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;

import com.google.api.client.util.Key;

/**
 * Defines the structure of a {@link Message}. Messages are part of the innogy system and besides other things are used
 * to raise battery warnings.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Message {
    /** device related messages */
    public static final String TYPE_DEVICE_UNREACHABLE = "DeviceUnreachable";
    public static final String TYPE_DEVICE_ACTIVITY_LOGGING_ENABLED = "DeviceActivityLoggingEnabled";
    public static final String TYPE_DEVICE_FACTORY_RESET = "DeviceFactoryReset";
    public static final String TYPE_DEVICE_LOW_BATTERY = "DeviceLowBattery";
    public static final String TYPE_DEVICE_MOLD = "DeviceMold";
    public static final String TYPE_DEVICE_LOW_RF_QUALITY = "DeviceLowRfQuality";
    public static final String TYPE_DEVICE_FREEZE = "DeviceFreeze";
    public static final String TYPE_SH_DEVICE_UPDATE_AVAILABLE = "ShDeviceUpdateAvailable";
    public static final String TYPE_SH_DEVICE_UPDATE_FAILED = "ShDeviceUpdateFailed";

    /** user related messages */
    public static final String TYPE_USER_EMAIL_ADDRESS_NOT_VALIDATED = "UserEmailAddressNotValidated";
    public static final String TYPE_USER_INVITATION_ACCEPTED = "UserInvitiationAccepted";
    public static final String TYPE_USER_FOREIGN_DELETION = "UserForeignDeletion";

    /** SHC related messages */
    public static final String TYPE_SHC_REMOTE_REBOOTED = "ShcRemoteRebooted";
    public static final String TYPE_SHC_UPDATE_COMPLETED = "ShcUpdateCompleted";
    public static final String TYPE_SHC_UPDATE_CANCELED = "ShcUpdateCanceled";
    public static final String TYPE_SHC_DEFERRABLE_UPDATE = "ShcDeferrableUpdate";
    public static final String TYPE_SHC_REAL_TIME_CLOCK_LOST = "ShcRealTimeClockLost";
    public static final String TYPE_SHC_ONLINE_SWITCH_IS_OFF = "ShcOnlineSwitchIsOff";
    public static final String TYPE_SHC_MANDATORY_UPDATE = "ShcMandatoryUpdate";
    public static final String TYPE_SHC_NO_CONNECTION_TO_BACKEND = "ShcNoConnectionToBackend";

    /** app related messages */
    public static final String TYPE_APP_ADDED_TO_SHC = "AppAddedToShc";
    public static final String TYPE_APP_UPDATED_ON_SHC = "AppUpdatedOnShc";
    public static final String TYPE_APP_TOKEN_SYNC_FAILURE = "AppTokenSyncFailure";
    public static final String TYPE_APP_DOWNLOAD_FAILED = "AppDownloadFailed";
    public static final String TYPE_APPLICATION_LOADING_ERROR = "ApplicationLoadingError";
    public static final String TYPE_APPLICATION_EXPIRED = "ApplicationExpired";
    public static final String TYPE_INVALID_CUSTOM_APP = "InvalidCustomApp";
    public static final String TYPE_CUSTOM_APP_WAS_UPGRADED = "CustomAppWasUpgraded";
    public static final String TYPE_CUSTOM_APP_UPGRADE_FAILED = "CustomAppUpgradeFailed";

    /** others */
    public static final String TYPE_BID_COS_INCLUSION_TIMEOUT = "BidCosInclusionTimeout";
    public static final String TYPE_ADDRESS_COLLISION = "AddressCollision";
    public static final String TYPE_BACKEND_CONFIG_OUT_OF_SYNC = "BackendConfigOutOfSync";
    public static final String TYPE_SMOKE_DETECTED = "SmokeDetected";
    public static final String TYPE_LEMON_BEAT_DONGLE_INITIALIZATION_FAILED = "LemonBeatDongleInitializationFailed";
    public static final String TYPE_USB_DEVICE_UNPLUGGED = "USBDeviceUnplugged";
    public static final String TYPE_INVALID_AES_KEY = "InvalidAesKey";
    public static final String TYPE_MEMORY_SHORTAGE = "MemoryShortage";
    public static final String TYPE_LOG_LEVEL_CHANGED = "LogLevelChanged";
    public static final String TYPE_RULE_EXCEPTION_FAILED = "RuleExecutionFailed";
    public static final String TYPE_SEND_MESSAGE_LIMIT_EXCEEDED = "SendMessageLimitExceeded";
    public static final String TYPE_CONFIG_FIX_ENTITY_DELETED = "ConfigFixEntityDeleted";

    /**
     * Identifier of the message – must be unique.
     */
    @Key("id")
    private String id;

    /**
     * Specifies the type of the message.
     */
    @Key("type")
    private String type;

    /**
     * Defines whether it is an alert or a message, default is message.
     */
    @Key("class")
    private String messageClass;

    /**
     * Reference to the description of the message.
     *
     * Optional.
     */
    @Key("desc")
    private String desc;

    /**
     * Timestamp when the message was created.
     *
     * Optional.
     */
    @Key("timestamp")
    private String timestamp;

    /**
     * Defines whether the message has been viewed by a user.
     */
    @Key("read")
    private boolean isRead;

    /**
     * Reference to the underlying product, which the message relates to.
     */
    @Key("Product")
    private Link productLink;

    /**
     * Reference to the underlying devices, which the message relates to.
     *
     * Optional.
     */
    @Key("Devices")
    private List<Link> deviceLinkList;

    /**
     * Reference to the underlying capabilities, which the message relates to.
     *
     * Optional.
     */
    @Key("Capabilities")
    private List<Link> capabilityLinkList;

    /**
     * Container for all parameters of the message. The parameters are contained in Property entities.
     *
     * Optional.
     */
    @Key("Data")
    private List<Property> dataPropertyList;

    /**
     * Container for tagging the message, e.g. if the message should only be visible to certain roles.
     *
     * Optional.
     */
    @Key("Tags")
    private List<Property> tagsPropertyList;

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
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
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
     * @return the productLink
     */
    public Link getProductLink() {
        return productLink;
    }

    /**
     * @param productLink the productLink to set
     */
    public void setProductLink(Link productLink) {
        this.productLink = productLink;
    }

    /**
     * @return the deviceLinkList
     */
    public List<Link> getDeviceLinkList() {
        return deviceLinkList;
    }

    /**
     * @param deviceLinkList the deviceLinkList to set
     */
    public void setDeviceLinkList(List<Link> deviceLinkList) {
        this.deviceLinkList = deviceLinkList;
    }

    /**
     * @return the capabilityLinkList
     */
    public List<Link> getCapabilityLinkList() {
        return capabilityLinkList;
    }

    /**
     * @param capabilityLinkList the capabilityLinkList to set
     */
    public void setCapabilityLinkList(List<Link> capabilityLinkList) {
        this.capabilityLinkList = capabilityLinkList;
    }

    /**
     * @return the dataPropertyList
     */
    public List<Property> getDataPropertyList() {
        return dataPropertyList;
    }

    /**
     * @param dataPropertyList the dataPropertyList to set
     */
    public void setDataPropertyList(List<Property> dataPropertyList) {
        this.dataPropertyList = dataPropertyList;
    }

    /**
     * @return the tagsPropertyList
     */
    public List<Property> getTagsPropertyList() {
        return tagsPropertyList;
    }

    /**
     * @param tagsPropertyList the tagsPropertyList to set
     */
    public void setTagsPropertyList(List<Property> tagsPropertyList) {
        this.tagsPropertyList = tagsPropertyList;
    }
}
