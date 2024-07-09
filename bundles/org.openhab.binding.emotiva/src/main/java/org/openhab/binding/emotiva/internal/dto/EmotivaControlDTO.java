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

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_CONTROL_MESSAGE_SET_DEFAULT_VALUE;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;

/**
 * The EmotivaControl message type. Use to send commands via {@link EmotivaCommandDTO} to Emotiva devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaControl")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaControlDTO extends AbstractJAXBElementDTO {

    @SuppressWarnings("unused")
    public EmotivaControlDTO() {
    }

    public EmotivaControlDTO(List<EmotivaCommandDTO> commands) {
        this.commands = commands;
    }

    public static EmotivaControlDTO create(EmotivaControlCommands command) {
        return new EmotivaControlDTO(
                List.of(EmotivaCommandDTO.fromTypeWithAck(command, DEFAULT_CONTROL_MESSAGE_SET_DEFAULT_VALUE)));
    }

    public static EmotivaControlDTO create(EmotivaControlCommands command, int value) {
        return new EmotivaControlDTO(List.of(EmotivaCommandDTO.fromTypeWithAck(command, String.valueOf(value))));
    }

    public static EmotivaControlDTO create(EmotivaControlCommands command, double value) {
        return new EmotivaControlDTO(
                List.of(EmotivaCommandDTO.fromTypeWithAck(command, String.valueOf(Math.round(value * 2) / 2.0))));
    }

    public static EmotivaControlDTO create(EmotivaControlCommands command, String value) {
        return new EmotivaControlDTO(List.of(EmotivaCommandDTO.fromTypeWithAck(command, value)));
    }
}
