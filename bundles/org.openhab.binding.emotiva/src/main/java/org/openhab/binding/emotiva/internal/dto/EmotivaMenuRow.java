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
 * Data field use by {@link EmotivaMenuNotifyDTO}.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "row")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaMenuRow {

    @XmlAttribute
    private String number;

    @XmlElement
    private List<EmotivaMenuCol> col;

    public EmotivaMenuRow() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<EmotivaMenuCol> getCol() {
        return col;
    }

    public void setCol(List<EmotivaMenuCol> col) {
        this.col = col;
    }
}
