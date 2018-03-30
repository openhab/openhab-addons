/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.innogysmarthome.internal.client.entity.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.PropertyList;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;

import com.google.api.client.util.Key;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Defines the {@link Event}, which is sent by the innogy websocket to inform the clients about changes.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Event extends PropertyList {

    public static final String TYPE_STATE_CHANGED = "device/SHC.RWE/1.0/event/StateChanged";
    public static final String TYPE_NEW_MESSAGE_RECEIVED = "device/SHC.RWE/1.0/event/NewMessageReceived";
    public static final String TYPE_MESSAGE_DELETED = "device/SHC.RWE/1.0/event/MessageDeleted";
    public static final String TYPE_DISCONNECT = "/event/Disconnect";
    public static final String TYPE_CONFIG_CHANGED = "device/SHC.RWE/1.0/event/ConfigChanged";
    public static final String TYPE_CONTROLLER_CONNECTIVITY_CHANGED = "device/SHC.RWE/1.0/event/ControllerConnectivityChanged";

    public static final String EVENT_PROPERTY_CONFIGURATION_VERSION = "ConfigurationVersion";
    public static final String EVENT_PROPERTY_IS_CONNECTED = "IsConnected";

    /**
     * Specifies the type of the event. The type must be the full path to uniquely reference the event definition.
     * Always available.
     */
    @Key("type")
    private String type;

    /**
     * Date and time when the event occurred in the system. Always available.
     */
    @Key("timestamp")
    private String timestamp;

    /**
     * Link to the metadata to the event definition.
     * Optional.
     */
    @Key("desc")
    private String desc;

    /**
     * Reference to the associated entity (instance or metadata) for the given event. Always available.
     */
    @Key("link")
    private Link link;

    /**
     * This container includes only properties, e.g. for the changed state properties. If there is other data than
     * properties to be transported, the data container will be used.
     * Optional.
     */
    @Key("Properties")
    @SerializedName("Properties")
    private List<Property> propertyList;

    protected HashMap<String, Property> propertyMap;

    /**
     * Data for the event, The data container can contain any type of entity dependent on the event type. For example,
     * the DeviceFound events contains the entire Device entity rather than selected properties.
     * Optional.
     */
    @Key("Data")
    @SerializedName("Data")
    private List<JsonObject> dataList;

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
     * @return the desc
     */
    protected String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the link
     */
    public Link getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(Link link) {
        this.link = link;
    }

    /**
     * @return the propertyList
     */
    @Override
    public List<Property> getPropertyList() {
        return propertyList;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    /*
     * (non-Javadoc)
     *
     * @see in.ollie.innogysmarthome.entity.PropertyList#getPropertyMap()
     */
    @Override
    protected Map<String, Property> getPropertyMap() {
        if (propertyMap == null) {
            propertyMap = PropertyList.getHashMap(propertyList);
        }

        return propertyMap;
    }

    /**
     * @return the dataList
     */
    public List<JsonObject> getDataList() {
        return dataList;
    }

    public List<Message> getDataListAsMessage() {
        List<Message> messageList = new ArrayList<>();
        List<JsonObject> objectList = getDataList();
        for (JsonObject o : objectList) {
            Message m = new Message();
            m.setId(o.get("id").getAsString());
            m.setType(o.get("type").getAsString());
            m.setRead(o.get("read").getAsBoolean());
            m.setMessageClass(o.get("class").getAsString());
            m.setDesc(o.get("desc").getAsString());
            m.setTimestamp(o.get("timestamp").getAsString());
            if (o.has("Devices")) {
                List<Link> deviceLinkList = new ArrayList<>();
                JsonArray deviceArr = o.get("Devices").getAsJsonArray();

                for (JsonElement deviceObject : deviceArr) {
                    deviceLinkList.add(new Link(deviceObject.getAsJsonObject().get("value").getAsString()));
                }
                m.setDeviceLinkList(deviceLinkList);
            }
            // TODO: add datapropertylist
            // m.setDataPropertyList(dataPropertyList);
            m.setProductLink(new Link(o.get("Product").getAsJsonObject().get("value").getAsString()));
            messageList.add(m);
        }
        return messageList;
    }

    /**
     * @param dataList the dataList to set
     */
    public void setDataList(List<JsonObject> dataList) {
        this.dataList = dataList;
    }

    /**
     * Returns the id of the link or null, if there is no link or the link does not have an id.
     *
     * @return String the id of the link or null
     */
    public String getLinkId() {
        String linkType = getLinkType();
        if (linkType != null && !linkType.equals(Link.LINK_TYPE_UNKNOWN) && !linkType.equals(Link.LINK_TYPE_SHC)) {
            String linkValue = getLink().getValue();
            if (linkValue != null) {
                return linkValue.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns the Type of the {@link Link} in the {@link Event}.
     *
     * @return
     */
    public String getLinkType() {
        Link link = getLink();
        if (link != null) {
            return link.getLinkType();
        }
        return null;
    }

    /**
     * Returns true, if the {@link Event} is a StateChangedEvent.
     *
     * @return
     */
    public boolean isStateChangedEvent() {
        return getType().equals(TYPE_STATE_CHANGED);
    }

    /**
     * Returns true, if the {@link Event} is a NewMessageReceivedEvent.
     *
     * @return
     */
    public boolean isNewMessageReceivedEvent() {
        return getType().equals(TYPE_NEW_MESSAGE_RECEIVED);
    }

    /**
     * Returns true, if the {@link Event} is a MessageDeletedEvent.
     *
     * @return
     */
    public boolean isMessageDeletedEvent() {
        return getType().equals(TYPE_MESSAGE_DELETED);
    }

    /**
     * Returns true, if the {@link Event} is a Disconnect event.
     *
     * @return
     */
    public boolean isDisconnectedEvent() {
        return getType().equals(TYPE_DISCONNECT);
    }

    /**
     * Returns true, if the {@link Event} is a ConfigChanged event.
     *
     * @return
     */
    public boolean isConfigChangedEvent() {
        return getType().equals(TYPE_CONFIG_CHANGED);
    }

    /**
     * Returns true, if the {@link Event} is a ControllerConnectivityChanged event.
     *
     * @return
     */
    public boolean isControllerConnectivityChangedEvent() {
        return getType().equals(TYPE_CONTROLLER_CONNECTIVITY_CHANGED);
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Capability}.
     *
     * @return
     */
    public Boolean isLinkedtoCapability() {
        return getLink() == null ? false : getLink().isTypeCapability();
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Device}.
     *
     * @return
     */
    public Boolean isLinkedtoDevice() {
        return getLink() == null ? false : getLink().isTypeDevice();
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Message}.
     *
     * @return
     */
    public Boolean isLinkedtoMessage() {
        return getLink() == null ? false : getLink().isTypeMessage();
    }

    /**
     * Returns true, if the {@link Link} points to the SHC {@link Device}.
     *
     * @return
     */
    public Boolean isLinkedtoSHC() {
        return getLink() == null ? false : getLink().isTypeSHC();
    }

    /**
     * Returns the configurationVersion or null, if this {@link Property} is not available in the event.
     *
     * @return
     */
    public Integer getConfigurationVersion() {
        return getPropertyValueAsInteger(EVENT_PROPERTY_CONFIGURATION_VERSION);
    }

    /**
     * Returns the isConnected {@link Property} value. Only available for event of type ControllerConnectivityChanged
     *
     * @return {@link Boolean} or <code>null</code>, if {@link Property} is not available or {@link Event} is not of
     *         type ControllerConnectivityChanged.
     */
    public Boolean getIsConnected() {
        if (!isControllerConnectivityChangedEvent()) {
            return null;
        }
        return getPropertyValueAsBoolean(EVENT_PROPERTY_IS_CONNECTED);
    }
}
