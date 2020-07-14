/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.tr064.internal.dto.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for itemType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="itemType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="statePattern" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "itemType", propOrder = { "value" })
public class ItemType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "unit")
    protected String unit;
    @XmlAttribute(name = "statePattern")
    protected String statePattern;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getUnit() {
        if (unit == null) {
            return "";
        } else {
            return unit;
        }
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the statePattern property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getStatePattern() {
        return statePattern;
    }

    /**
     * Sets the value of the statePattern property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setStatePattern(String value) {
        this.statePattern = value;
    }
}
