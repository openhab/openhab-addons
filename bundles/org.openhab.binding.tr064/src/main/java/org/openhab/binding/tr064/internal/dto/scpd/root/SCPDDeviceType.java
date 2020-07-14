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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for deviceType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deviceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="deviceType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="friendlyName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="manufacturer" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="manufacturerURL" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="modelDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="modelName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="modelNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="modelURL" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="UDN" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="UPC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="iconList" type="{urn:dslforum-org:device-1-0}iconListType" minOccurs="0"/&gt;
 *         &lt;element name="serviceList" type="{urn:dslforum-org:device-1-0}serviceListType"/&gt;
 *         &lt;element name="deviceList" type="{urn:dslforum-org:device-1-0}deviceListType" minOccurs="0"/&gt;
 *         &lt;element name="presentationURL" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deviceType", propOrder = { "deviceType", "friendlyName", "manufacturer", "manufacturerURL",
        "modelDescription", "modelName", "modelNumber", "modelURL", "udn", "upc", "iconList", "serviceList",
        "deviceList", "presentationURL" })
public class SCPDDeviceType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String deviceType;
    @XmlElement(required = true)
    protected String friendlyName;
    @XmlElement(required = true)
    protected String manufacturer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String manufacturerURL;
    @XmlElement(required = true)
    protected String modelDescription;
    @XmlElement(required = true)
    protected String modelName;
    @XmlElement(required = true)
    protected String modelNumber;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String modelURL;
    @XmlElement(name = "UDN", required = true)
    protected String udn;
    @XmlElement(name = "UPC")
    protected String upc;
    protected SCPDIconListType iconList;
    @XmlElementWrapper(required = true)
    @XmlElement(name = "service", namespace = "urn:dslforum-org:device-1-0")
    protected List<SCPDServiceType> serviceList = new ArrayList<SCPDServiceType>();
    @XmlElementWrapper
    @XmlElement(name = "device", namespace = "urn:dslforum-org:device-1-0")
    protected List<SCPDDeviceType> deviceList = new ArrayList<SCPDDeviceType>();
    @XmlSchemaType(name = "anyURI")
    protected String presentationURL;

    /**
     * Gets the value of the deviceType property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the value of the deviceType property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setDeviceType(String value) {
        this.deviceType = value;
    }

    /**
     * Gets the value of the friendlyName property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Sets the value of the friendlyName property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setFriendlyName(String value) {
        this.friendlyName = value;
    }

    /**
     * Gets the value of the manufacturer property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the value of the manufacturer property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setManufacturer(String value) {
        this.manufacturer = value;
    }

    /**
     * Gets the value of the manufacturerURL property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getManufacturerURL() {
        return manufacturerURL;
    }

    /**
     * Sets the value of the manufacturerURL property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setManufacturerURL(String value) {
        this.manufacturerURL = value;
    }

    /**
     * Gets the value of the modelDescription property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getModelDescription() {
        return modelDescription;
    }

    /**
     * Sets the value of the modelDescription property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setModelDescription(String value) {
        this.modelDescription = value;
    }

    /**
     * Gets the value of the modelName property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the value of the modelName property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setModelName(String value) {
        this.modelName = value;
    }

    /**
     * Gets the value of the modelNumber property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getModelNumber() {
        return modelNumber;
    }

    /**
     * Sets the value of the modelNumber property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setModelNumber(String value) {
        this.modelNumber = value;
    }

    /**
     * Gets the value of the modelURL property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getModelURL() {
        return modelURL;
    }

    /**
     * Sets the value of the modelURL property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setModelURL(String value) {
        this.modelURL = value;
    }

    /**
     * Gets the value of the udn property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getUDN() {
        return udn;
    }

    /**
     * Sets the value of the udn property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setUDN(String value) {
        this.udn = value;
    }

    /**
     * Gets the value of the upc property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getUPC() {
        return upc;
    }

    /**
     * Sets the value of the upc property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setUPC(String value) {
        this.upc = value;
    }

    /**
     * Gets the value of the iconList property.
     * 
     * @return
     *         possible object is
     *         {@link SCPDIconListType }
     * 
     */
    public SCPDIconListType getIconList() {
        return iconList;
    }

    /**
     * Sets the value of the iconList property.
     * 
     * @param value
     *            allowed object is
     *            {@link SCPDIconListType }
     * 
     */
    public void setIconList(SCPDIconListType value) {
        this.iconList = value;
    }

    /**
     * Gets the value of the presentationURL property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getPresentationURL() {
        return presentationURL;
    }

    /**
     * Sets the value of the presentationURL property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setPresentationURL(String value) {
        this.presentationURL = value;
    }

    public List<SCPDServiceType> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<SCPDServiceType> serviceList) {
        this.serviceList = serviceList;
    }

    public List<SCPDDeviceType> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<SCPDDeviceType> deviceList) {
        this.deviceList = deviceList;
    }
}
