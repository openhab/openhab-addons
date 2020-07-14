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
 * Java class for phonebooksType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="phonebooksType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="phonebook" type="{}phonebookType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "phonebooksType", propOrder = { "phonebook" })
public class PhonebooksType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected PhonebookType phonebook;

    /**
     * Gets the value of the phonebook property.
     * 
     * @return
     *         possible object is
     *         {@link PhonebookType }
     * 
     */
    public PhonebookType getPhonebook() {
        return phonebook;
    }

    /**
     * Sets the value of the phonebook property.
     * 
     * @param value
     *            allowed object is
     *            {@link PhonebookType }
     * 
     */
    public void setPhonebook(PhonebookType value) {
        this.phonebook = value;
    }
}
