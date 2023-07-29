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
package org.openhab.binding.innogysmarthome.internal.client.entity.event;

import java.util.HashMap;

import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;

/**
 * Defines the {@link Event}, which is sent by the innogy websocket to inform the clients about changes.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Event extends BaseEvent {

    public static final String EVENT_PROPERTY_CONFIGURATION_VERSION = "ConfigurationVersion";
    public static final String EVENT_PROPERTY_IS_CONNECTED = "IsConnected";

    /**
     * Reference to the associated entity (instance or metadata) for the given event. Always available.
     */
    private String source;

    /**
     * The product (context) that generated the event.
     */
    private String namespace;

    /**
     * This container includes only properties, e.g. for the changed state properties. If there is other data than
     * properties to be transported, the data container will be used.
     * Optional.
     */
    private EventProperties properties;

    protected HashMap<String, Property> propertyMap;

    /**
     * Data for the event, The data container can contain any type of entity dependent on the event type. For example,
     * the DeviceFound events contains the entire Device entity rather than selected properties.
     * Optional.
     */
    private EventData data;

    /**
     * @return the link to the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the link to the source to set
     */
    public void setSource(String source) {
        this.source = source;
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
     * @return the properties
     */
    public EventProperties getProperties() {
        return properties;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setProperties(EventProperties properties) {
        this.properties = properties;
    }

    /**
     * @return the dataList
     */
    public EventData getData() {
        return data;
    }

    /**
     * @param dataList the dataList to set
     */
    public void setData(EventData data) {
        this.data = data;
    }

    /**
     * Returns the id of the link or null, if there is no link or the link does not have an id.
     *
     * @return String the id of the link or null
     */
    public String getSourceId() {
        final String linkType = getSourceLinkType();
        if (linkType != null && !Link.LINK_TYPE_UNKNOWN.equals(linkType) && !Link.LINK_TYPE_SHC.equals(linkType)) {
            if (source != null) {
                return source.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns the Type of the {@link Link} in the {@link Event}.
     *
     * @return
     */
    public String getSourceLinkType() {
        if (source != null) {
            return Link.getLinkType(source);
        }
        return null;
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Capability}.
     *
     * @return
     */
    public Boolean isLinkedtoCapability() {
        return source == null ? false : Link.isTypeCapability(source);
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Device}.
     *
     * @return
     */
    public Boolean isLinkedtoDevice() {
        return source == null ? false : Link.isTypeDevice(source);
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Message}.
     *
     * @return
     */
    public Boolean isLinkedtoMessage() {
        return source == null ? false : Link.isTypeMessage(source);
    }

    /**
     * Returns true, if the {@link Link} points to the SHC {@link Device}.
     *
     * @return
     */
    public Boolean isLinkedtoSHC() {
        return source == null ? false : Link.isTypeSHC(source);
    }

    /**
     * Returns the configurationVersion or null, if this {@link Property} is not available in the event.
     *
     * @return
     */
    public Integer getConfigurationVersion() {
        return getData().getConfigVersion();
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
        return getProperties().getIsConnected();
    }
}
