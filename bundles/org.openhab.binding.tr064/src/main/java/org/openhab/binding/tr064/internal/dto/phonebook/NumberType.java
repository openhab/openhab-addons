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

package org.openhab.binding.tr064.internal.dto.phonebook;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for numberType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="numberType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="vanity" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="prio" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="quickdial" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "numberType", propOrder = { "value" })
public class NumberType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "vanity")
    protected String vanity;
    @XmlAttribute(name = "prio")
    protected String prio;
    @XmlAttribute(name = "quickdial")
    protected Byte quickdial;

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
     * Gets the value of the vanity property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getVanity() {
        return vanity;
    }

    /**
     * Sets the value of the vanity property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setVanity(String value) {
        this.vanity = value;
    }

    /**
     * Gets the value of the prio property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getPrio() {
        return prio;
    }

    /**
     * Sets the value of the prio property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setPrio(String value) {
        this.prio = value;
    }

    /**
     * Gets the value of the quickdial property.
     * 
     * @return
     *         possible object is
     *         {@link Byte }
     * 
     */
    public Byte getQuickdial() {
        return quickdial;
    }

    /**
     * Sets the value of the quickdial property.
     * 
     * @param value
     *            allowed object is
     *            {@link Byte }
     * 
     */
    public void setQuickdial(Byte value) {
        this.quickdial = value;
    }
}
