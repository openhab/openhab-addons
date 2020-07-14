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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for contactType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contactType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="category"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte"&gt;
 *               &lt;enumeration value="0"/&gt;
 *               &lt;enumeration value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="person" type="{}personType"/&gt;
 *         &lt;element name="uniqueid" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="telephony" type="{}telephonyType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contactType", propOrder = { "category", "person", "uniqueid", "telephony" })
public class ContactType implements Serializable {

    private final static long serialVersionUID = 1L;
    protected byte category;
    @XmlElement(required = true)
    protected PersonType person;
    protected byte uniqueid;
    @XmlElement(required = true)
    protected TelephonyType telephony;

    /**
     * Gets the value of the category property.
     * 
     */
    public byte getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     */
    public void setCategory(byte value) {
        this.category = value;
    }

    /**
     * Gets the value of the person property.
     * 
     * @return
     *         possible object is
     *         {@link PersonType }
     * 
     */
    public PersonType getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     * 
     * @param value
     *            allowed object is
     *            {@link PersonType }
     * 
     */
    public void setPerson(PersonType value) {
        this.person = value;
    }

    /**
     * Gets the value of the uniqueid property.
     * 
     */
    public byte getUniqueid() {
        return uniqueid;
    }

    /**
     * Sets the value of the uniqueid property.
     * 
     */
    public void setUniqueid(byte value) {
        this.uniqueid = value;
    }

    /**
     * Gets the value of the telephony property.
     * 
     * @return
     *         possible object is
     *         {@link TelephonyType }
     * 
     */
    public TelephonyType getTelephony() {
        return telephony;
    }

    /**
     * Sets the value of the telephony property.
     * 
     * @param value
     *            allowed object is
     *            {@link TelephonyType }
     * 
     */
    public void setTelephony(TelephonyType value) {
        this.telephony = value;
    }
}
