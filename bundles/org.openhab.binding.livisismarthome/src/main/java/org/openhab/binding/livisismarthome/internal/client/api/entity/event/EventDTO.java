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
package org.openhab.binding.livisismarthome.internal.client.api.entity.event;

import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;

/**
 * Defines the {@link EventDTO}, which is sent by the LIVISI websocket to inform the clients about changes.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class EventDTO extends BaseEventDTO {

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
    private EventPropertiesDTO properties;

    /**
     * Data for the event, The data container can contain any type of entity dependent on the event type. For example,
     * the DeviceFound events contains the entire Device entity rather than selected properties.
     * Optional.
     */
    private EventDataDTO data;

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
    public EventPropertiesDTO getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(EventPropertiesDTO properties) {
        this.properties = properties;
    }

    /**
     * @return the dataList
     */
    public EventDataDTO getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(EventDataDTO data) {
        this.data = data;
    }

    /**
     * Returns the id of the link or null, if there is no link or the link does not have an id.
     *
     * @return String the id of the link or null
     */
    public String getSourceId() {
        final String linkType = getSourceLinkType();
        if (linkType != null && !LinkDTO.LINK_TYPE_UNKNOWN.equals(linkType)
                && !LinkDTO.LINK_TYPE_SHC.equals(linkType)) {
            if (source != null) {
                String sourceId = source.replace(linkType, "");
                sourceId = sourceId.replace("-", "");
                return sourceId;
            }
        }
        return null;
    }

    /**
     * Returns the Type of the {@link LinkDTO} in the {@link EventDTO}.
     *
     * @return link type
     */
    public String getSourceLinkType() {
        if (source != null) {
            return LinkDTO.getLinkType(source);
        }
        return null;
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO}.
     *
     * @return true if the link points to a capability, otherwise false
     */
    public Boolean isLinkedtoCapability() {
        return source != null && LinkDTO.isTypeCapability(source);
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}.
     *
     * @return true if the link points to a device, otherwise false
     */
    public Boolean isLinkedtoDevice() {
        return source != null && LinkDTO.isTypeDevice(source);
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO}.
     *
     * @return true if the link points to a message, otherwise false
     */
    public Boolean isLinkedtoMessage() {
        return source != null && LinkDTO.isTypeMessage(source);
    }

    /**
     * Returns true, if the {@link LinkDTO} points to the SHC
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}.
     *
     * @return true if the link points to a SHC bridge device, otherwise false
     */
    public Boolean isLinkedtoSHC() {
        return source != null && LinkDTO.isTypeSHC(source);
    }

    /**
     * Returns true, if it is a button pressed event. Otherwise false.
     * It is a button pressed event when button index is set (LastPressedButtonIndex is set too).
     * 
     * @return true if it is a button pressed event, otherwise false
     */
    public boolean isButtonPressedEvent() {
        final Integer buttonIndex = getProperties().getKeyPressButtonIndex();
        return buttonIndex != null;
    }

    /**
     * Returns the configurationVersion or null, if this
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.PropertyDTO} is not available in the event.
     *
     * @return configuration version
     */
    public Integer getConfigurationVersion() {
        return getData().getConfigVersion();
    }

    /**
     * Returns the isConnected {@link org.openhab.binding.livisismarthome.internal.client.api.entity.PropertyDTO} value.
     * Only available for event of type ControllerConnectivityChanged
     *
     * @return {@link Boolean} or <code>null</code>, if not available
     */
    public Boolean getIsConnected() {
        return getProperties().getIsConnected();
    }
}
