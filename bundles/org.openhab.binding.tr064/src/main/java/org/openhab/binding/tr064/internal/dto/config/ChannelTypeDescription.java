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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for channelTypeDescription complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="channelTypeDescription"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="item" type="{channelconfig}itemType"/&gt;
 *         &lt;element name="service" type="{channelconfig}serviceType"/&gt;
 *         &lt;element name="getAction" type="{channelconfig}actionType" minOccurs="0"/&gt;
 *         &lt;element name="setAction" type="{channelconfig}actionType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="advanced" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "channelTypeDescription", propOrder = { "item", "service", "getAction", "setAction" })
public class ChannelTypeDescription implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected ItemType item;
    @XmlElement(required = true)
    protected ServiceType service;
    protected ActionType getAction;
    protected ActionType setAction;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "label")
    protected String label;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "advanced")
    protected Boolean advanced;

    /**
     * Gets the value of the item property.
     * 
     * @return
     *         possible object is
     *         {@link ItemType }
     * 
     */
    public ItemType getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     * 
     * @param value
     *            allowed object is
     *            {@link ItemType }
     * 
     */
    public void setItem(ItemType value) {
        this.item = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *         possible object is
     *         {@link ServiceType }
     * 
     */
    public ServiceType getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *            allowed object is
     *            {@link ServiceType }
     * 
     */
    public void setService(ServiceType value) {
        this.service = value;
    }

    /**
     * Gets the value of the getAction property.
     * 
     * @return
     *         possible object is
     *         {@link ActionType }
     * 
     */
    public ActionType getGetAction() {
        return getAction;
    }

    /**
     * Sets the value of the getAction property.
     * 
     * @param value
     *            allowed object is
     *            {@link ActionType }
     * 
     */
    public void setGetAction(ActionType value) {
        this.getAction = value;
    }

    /**
     * Gets the value of the setAction property.
     * 
     * @return
     *         possible object is
     *         {@link ActionType }
     * 
     */
    public ActionType getSetAction() {
        return setAction;
    }

    /**
     * Sets the value of the setAction property.
     * 
     * @param value
     *            allowed object is
     *            {@link ActionType }
     * 
     */
    public void setSetAction(ActionType value) {
        this.setAction = value;
    }

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
     * Gets the value of the label property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the advanced property.
     * 
     * @return
     *         possible object is
     *         {@link Boolean }
     * 
     */
    public boolean isAdvanced() {
        if (advanced == null) {
            return false;
        } else {
            return advanced;
        }
    }

    /**
     * Sets the value of the advanced property.
     * 
     * @param value
     *            allowed object is
     *            {@link Boolean }
     * 
     */
    public void setAdvanced(Boolean value) {
        this.advanced = value;
    }
}
