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
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;

/**
 * Defines the {@link EventDTO}, which is sent by the LIVISI websocket to inform the clients about changes.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class MessageEventDTO extends BaseEventDTO {

    /**
     * Reference to the associated entity (instance or metadata) for the given event. Always available.
     */
    private String source;

    /**
     * The product (context) that generated the event.
     */
    private String namespace;

    /**
     * Data for the event, The data container can contain any type of entity dependent on the event type. For example,
     * the DeviceFound events contains the entire Device entity rather than selected properties.
     * Optional.
     */
    private MessageDTO data;

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
     * @return the dataList
     */
    public MessageDTO getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(MessageDTO data) {
        this.data = data;
    }

    public MessageDTO getMessage() {
        return data;
    }

    /**
     * Returns the id of the link or null, if there is no link or the link does not have an id.
     *
     * @return String the id of the link or null
     */
    public String getSourceId() {
        if (source != null) {
            final String linkType = getSourceLinkType();
            if (linkType != null && !LinkDTO.LINK_TYPE_UNKNOWN.equals(linkType)
                    && !LinkDTO.LINK_TYPE_SHC.equals(linkType)) {
                return source.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns the Type of the {@link LinkDTO} in the {@link EventDTO}.
     *
     * @return type of the {@link LinkDTO}
     */
    private String getSourceLinkType() {
        if (source != null) {
            return LinkDTO.getLinkType(source);
        }
        return null;
    }
}
