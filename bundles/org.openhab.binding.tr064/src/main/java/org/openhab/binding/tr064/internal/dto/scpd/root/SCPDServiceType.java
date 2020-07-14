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

package org.openhab.binding.tr064.internal.dto.scpd.root;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for serviceType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="serviceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="serviceType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="serviceId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="controlURL" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="eventSubURL" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SCPDURL" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceType", propOrder = { "serviceType", "serviceId", "controlURL", "eventSubURL", "scpdurl" })
public class SCPDServiceType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String serviceType;
    @XmlElement(required = true)
    protected String serviceId;
    @XmlElement(required = true)
    protected String controlURL;
    @XmlElement(required = true)
    protected String eventSubURL;
    @XmlElement(name = "SCPDURL", required = true)
    protected String scpdurl;

    /**
     * Gets the value of the serviceType property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the value of the serviceType property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setServiceType(String value) {
        this.serviceType = value;
    }

    /**
     * Gets the value of the serviceId property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the value of the serviceId property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setServiceId(String value) {
        this.serviceId = value;
    }

    /**
     * Gets the value of the controlURL property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getControlURL() {
        return controlURL;
    }

    /**
     * Sets the value of the controlURL property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setControlURL(String value) {
        this.controlURL = value;
    }

    /**
     * Gets the value of the eventSubURL property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getEventSubURL() {
        return eventSubURL;
    }

    /**
     * Sets the value of the eventSubURL property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setEventSubURL(String value) {
        this.eventSubURL = value;
    }

    /**
     * Gets the value of the scpdurl property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getSCPDURL() {
        return scpdurl;
    }

    /**
     * Sets the value of the scpdurl property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setSCPDURL(String value) {
        this.scpdurl = value;
    }
}
