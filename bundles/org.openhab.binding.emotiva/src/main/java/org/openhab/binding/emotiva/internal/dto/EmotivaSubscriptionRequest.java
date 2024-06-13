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

import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * A helper class for sending {@link EmotivaSubscriptionDTO} messages.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaSubscription")
public class EmotivaSubscriptionRequest extends AbstractJAXBElementDTO {

    @XmlAttribute
    private String protocol = PROTOCOL_V2.value();

    @SuppressWarnings("unused")
    public EmotivaSubscriptionRequest() {
    }

    public EmotivaSubscriptionRequest(List<EmotivaCommandDTO> commands, String protocol) {
        this.protocol = protocol;
        this.commands = commands;
    }

    public EmotivaSubscriptionRequest(EmotivaSubscriptionTags[] emotivaCommandTypes, String protocol) {
        this.protocol = protocol;
        List<EmotivaCommandDTO> list = new ArrayList<>();
        for (EmotivaSubscriptionTags commandType : emotivaCommandTypes) {
            list.add(EmotivaCommandDTO.fromTypeWithAck(commandType));
        }
        this.commands = list;
    }

    public EmotivaSubscriptionRequest(EmotivaSubscriptionTags tag) {
        this.commands = List.of(EmotivaCommandDTO.fromTypeWithAck(tag));
    }

    public EmotivaSubscriptionRequest(EmotivaControlCommands commandType, String protocol) {
        this.protocol = protocol;
        this.commands = List.of(EmotivaCommandDTO.fromTypeWithAck(commandType));
    }
}
