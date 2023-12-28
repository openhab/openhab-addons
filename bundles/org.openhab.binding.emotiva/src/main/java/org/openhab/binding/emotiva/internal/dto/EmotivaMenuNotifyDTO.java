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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaMenuNotify message type. Received from a device if subscribed to the
 * 
 * @link EmotivaSubscriptionTags#menu_update} type.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaMenuNotify")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaMenuNotifyDTO {

    @XmlAttribute
    private String sequence;

    @XmlElement
    private List<EmotivaMenuRow> row;
    @XmlElement
    private EmotivaMenuProgress progress;

    public EmotivaMenuNotifyDTO() {
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<EmotivaMenuRow> getRow() {
        return row;
    }

    public void setRow(List<EmotivaMenuRow> row) {
        this.row = row;
    }

    public EmotivaMenuProgress getProgress() {
        return progress;
    }

    public void setProgress(EmotivaMenuProgress progress) {
        this.progress = progress;
    }
}
