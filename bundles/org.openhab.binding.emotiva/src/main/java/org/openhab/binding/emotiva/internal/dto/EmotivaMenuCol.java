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
    private String arrow;
    @XmlAttribute
    private String checkbox;
    @XmlAttribute
    private String fixed;
    @XmlAttribute
    private String fixedWidth;
    @XmlAttribute
    private String highlight;
    @XmlAttribute
    private String offset;
    @XmlAttribute
    private String number;
    @XmlAttribute
    private String value;

    public EmotivaMenuCol() {
    }

    public String getArrow() {
        return arrow;
    }

    public void setArrow(String arrow) {
        this.arrow = arrow;
    }

    public String getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(String checkbox) {
        this.checkbox = checkbox;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    public String getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(String fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
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
}
