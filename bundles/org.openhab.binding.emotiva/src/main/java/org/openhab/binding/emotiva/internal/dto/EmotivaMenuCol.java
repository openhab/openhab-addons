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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Data field use by {@link EmotivaMenuNotifyDTO}.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "col")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaMenuCol {

    @XmlAttribute
    private String number;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String fixed;
    @XmlAttribute
    private String highlight;
    @XmlAttribute
    private String arrow;

    public EmotivaMenuCol() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getArrow() {
        return arrow;
    }

    public void setArrow(String arrow) {
        this.arrow = arrow;
    }
}
