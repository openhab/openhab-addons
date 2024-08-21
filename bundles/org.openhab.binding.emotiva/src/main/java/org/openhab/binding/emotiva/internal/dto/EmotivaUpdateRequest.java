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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * A helper class for sending EmotivaUpdate messages with {@link EmotivaCommandDTO} commands.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaUpdate")
public class EmotivaUpdateRequest extends AbstractJAXBElementDTO {

    @XmlAttribute
    private String protocol;

    @SuppressWarnings("unused")
    public EmotivaUpdateRequest() {
    }

    public EmotivaUpdateRequest(List<EmotivaCommandDTO> commands) {
        this.commands = commands;
    }

    public EmotivaUpdateRequest(EmotivaControlCommands command, String protocol) {
        this.protocol = protocol;
        List<EmotivaCommandDTO> list = new ArrayList<>();
        list.add(EmotivaCommandDTO.fromType(command));
        this.commands = list;
    }

    public EmotivaUpdateRequest(EmotivaSubscriptionTags tag) {
        this.commands = List.of(EmotivaCommandDTO.fromType(tag));
    }

    public EmotivaUpdateRequest(EmotivaSubscriptionTags[] tags, String protocol) {
        this.protocol = protocol;
        List<EmotivaCommandDTO> list = new ArrayList<>();
        for (EmotivaSubscriptionTags tag : tags) {
            list.add(EmotivaCommandDTO.fromType(tag));
        }
        this.commands = list;
    }

    public EmotivaUpdateRequest(EmotivaControlCommands commandType) {
        this.commands = List.of(EmotivaCommandDTO.fromType(commandType));
    }
}
