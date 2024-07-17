/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.dto;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_CONTROL_ACK_VALUE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * The EmotivaCommand DTO. Use by multiple message types to control commands in Emotiva devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaCommandDTO {

    @XmlValue
    private String commandName;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String visible;
    @XmlAttribute
    private String status;
    @XmlAttribute
    private String ack;

    @SuppressWarnings("unused")
    public EmotivaCommandDTO() {
    }

    public EmotivaCommandDTO(EmotivaControlCommands command) {
        this.commandName = command.name();
    }

    public EmotivaCommandDTO(EmotivaSubscriptionTags tag) {
        this.commandName = tag.name();
    }

    public EmotivaCommandDTO(EmotivaControlCommands command, String value) {
        this.commandName = command.name();
        this.value = value;
    }

    public EmotivaCommandDTO(EmotivaControlCommands commandName, String value, String ack) {
        this(commandName, value);
        this.ack = ack;
    }

    /**
     * Creates a new instance based on command. Primarily used by EmotivaControl messages.
     *
     * @return EmotivaCommandDTO with ack=yes always added
     */
    public static EmotivaCommandDTO fromTypeWithAck(EmotivaControlCommands command) {
        EmotivaCommandDTO emotivaCommandDTO = new EmotivaCommandDTO(command);
        emotivaCommandDTO.setAck(DEFAULT_CONTROL_ACK_VALUE);
        return emotivaCommandDTO;
    }

    /**
     * Creates a new instance based on command and value. Primarily used by EmotivaControl messages.
     *
     * @return EmotivaCommandDTO with ack=yes always added
     */
    public static EmotivaCommandDTO fromTypeWithAck(EmotivaControlCommands command, String value) {
        EmotivaCommandDTO emotivaCommandDTO = new EmotivaCommandDTO(command);
        if (value != null) {
            emotivaCommandDTO.setValue(value);
        }
        emotivaCommandDTO.setAck(DEFAULT_CONTROL_ACK_VALUE);
        return emotivaCommandDTO;
    }

    public static EmotivaCommandDTO fromType(EmotivaControlCommands command) {
        return new EmotivaCommandDTO(command);
    }

    public static EmotivaCommandDTO fromType(EmotivaSubscriptionTags command) {
        return new EmotivaCommandDTO(command);
    }

    public static EmotivaCommandDTO fromTypeWithAck(EmotivaSubscriptionTags command) {
        EmotivaCommandDTO emotivaCommandDTO = new EmotivaCommandDTO(command);
        emotivaCommandDTO.setAck(DEFAULT_CONTROL_ACK_VALUE);
        return emotivaCommandDTO;
    }

    public String getName() {
        return commandName;
    }

    public String getValue() {
        return value;
    }

    public String getVisible() {
        return visible;
    }

    public String getStatus() {
        return status;
    }

    public String getAck() {
        return ack;
    }

    public void setName(String name) {
        this.commandName = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }
}
