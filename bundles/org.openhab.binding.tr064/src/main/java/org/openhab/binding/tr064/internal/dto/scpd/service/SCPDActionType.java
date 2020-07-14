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

package org.openhab.binding.tr064.internal.dto.scpd.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for actionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="actionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="argumentList" type="{urn:dslforum-org:service-1-0}argumentListType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "actionType", propOrder = { "name", "argumentList" })
public class SCPDActionType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String name;
    @XmlElementWrapper(required = true)
    @XmlElement(name = "argument", namespace = "urn:dslforum-org:service-1-0")
    protected List<SCPDArgumentType> argumentList = new ArrayList<SCPDArgumentType>();

    /**
     * Gets the value of the name property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

    public List<SCPDArgumentType> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<SCPDArgumentType> argumentList) {
        this.argumentList = argumentList;
    }
}
