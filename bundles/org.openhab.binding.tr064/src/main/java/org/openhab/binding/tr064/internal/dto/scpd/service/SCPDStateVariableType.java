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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for stateVariableType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stateVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dataType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="allowedValueRange" type="{urn:dslforum-org:service-1-0}allowedValueRangeType" minOccurs="0"/&gt;
 *         &lt;element name="allowedValueList" type="{urn:dslforum-org:service-1-0}allowedValueListType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sendEvents" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stateVariableType", propOrder = { "name", "dataType", "defaultValue", "allowedValueRange",
        "allowedValueList" })
public class SCPDStateVariableType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String dataType;
    protected String defaultValue;
    protected SCPDAllowedValueRangeType allowedValueRange;
    @XmlElementWrapper
    @XmlElement(name = "allowedValue", namespace = "urn:dslforum-org:service-1-0")
    protected List<String> allowedValueList = new ArrayList<String>();
    @XmlAttribute(name = "sendEvents")
    protected String sendEvents;

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

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setDataType(String value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the allowedValueRange property.
     * 
     * @return
     *         possible object is
     *         {@link SCPDAllowedValueRangeType }
     * 
     */
    public SCPDAllowedValueRangeType getAllowedValueRange() {
        return allowedValueRange;
    }

    /**
     * Sets the value of the allowedValueRange property.
     * 
     * @param value
     *            allowed object is
     *            {@link SCPDAllowedValueRangeType }
     * 
     */
    public void setAllowedValueRange(SCPDAllowedValueRangeType value) {
        this.allowedValueRange = value;
    }

    /**
     * Gets the value of the sendEvents property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getSendEvents() {
        return sendEvents;
    }

    /**
     * Sets the value of the sendEvents property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setSendEvents(String value) {
        this.sendEvents = value;
    }

    public List<String> getAllowedValueList() {
        return allowedValueList;
    }

    public void setAllowedValueList(List<String> allowedValueList) {
        this.allowedValueList = allowedValueList;
    }
}
