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
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * The EmotivaUnsubscriptionDTO message type. Use to remove subscription after registration via {
 * 
 * @link EmotivaSubscriptionRequest}.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaUnsubscribe")
public class EmotivaUnsubscribeDTO extends AbstractJAXBElementDTO {

    @SuppressWarnings("unused")
    public EmotivaUnsubscribeDTO() {
    }

    public EmotivaUnsubscribeDTO(EmotivaSubscriptionTags[] emotivaCommandTypes) {
        List<EmotivaCommandDTO> list = new ArrayList<>();
        for (EmotivaSubscriptionTags commandType : emotivaCommandTypes) {
            list.add(EmotivaCommandDTO.fromType(commandType));
        }
        this.commands = list;
    }

    public EmotivaUnsubscribeDTO(EmotivaSubscriptionTags tag) {
        this.commands = List.of(EmotivaCommandDTO.fromType(tag));
    }

    public EmotivaUnsubscribeDTO(EmotivaCommandDTO commandType) {
        this.commands = List.of(commandType);
    }

    public EmotivaUnsubscribeDTO(List<EmotivaSubscriptionTags> commandType) {
        this.commands = commandType.stream().map(EmotivaCommandDTO::fromTypeWithAck).collect(Collectors.toList());
    }
}
