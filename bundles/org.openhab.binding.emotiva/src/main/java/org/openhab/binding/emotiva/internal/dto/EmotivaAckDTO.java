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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaAck message type. Received from Emotiva device whenever a {@link EmotivaControlDTO} with
 * {@link EmotivaCommandDTO} is sent.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaAck")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaAckDTO {

    @XmlAnyElement(lax = true)
    private List<Object> commands;

    @SuppressWarnings("unused")
    public EmotivaAckDTO() {
    }

    public List<Object> getCommands() {
        return commands;
    }

    public void setCommands(List<Object> commands) {
        this.commands = commands;
    }

    @Override
    public String toString() {
        return "EmotivaAckDTO{" + "commands=" + commands + '}';
    }
}
