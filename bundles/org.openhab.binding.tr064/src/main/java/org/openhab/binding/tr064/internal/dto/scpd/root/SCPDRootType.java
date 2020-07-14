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
 * Java class for rootType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rootType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="specVersion" type="{urn:dslforum-org:device-1-0}specVersionType"/&gt;
 *         &lt;element name="systemVersion" type="{urn:dslforum-org:device-1-0}systemVersionType"/&gt;
 *         &lt;element name="device" type="{urn:dslforum-org:device-1-0}deviceType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rootType", propOrder = { "specVersion", "systemVersion", "device" })
public class SCPDRootType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected SCPDSpecVersionType specVersion;
    @XmlElement(required = true)
    protected SCPDSystemVersionType systemVersion;
    @XmlElement(required = true)
    protected SCPDDeviceType device;

    /**
     * Gets the value of the specVersion property.
     * 
     * @return
     *         possible object is
     *         {@link SCPDSpecVersionType }
     * 
     */
    public SCPDSpecVersionType getSpecVersion() {
        return specVersion;
    }

    /**
     * Sets the value of the specVersion property.
     * 
     * @param value
     *            allowed object is
     *            {@link SCPDSpecVersionType }
     * 
     */
    public void setSpecVersion(SCPDSpecVersionType value) {
        this.specVersion = value;
    }

    /**
     * Gets the value of the systemVersion property.
     * 
     * @return
     *         possible object is
     *         {@link SCPDSystemVersionType }
     * 
     */
    public SCPDSystemVersionType getSystemVersion() {
        return systemVersion;
    }

    /**
     * Sets the value of the systemVersion property.
     * 
     * @param value
     *            allowed object is
     *            {@link SCPDSystemVersionType }
     * 
     */
    public void setSystemVersion(SCPDSystemVersionType value) {
        this.systemVersion = value;
    }

    /**
     * Gets the value of the device property.
     * 
     * @return
     *         possible object is
     *         {@link SCPDDeviceType }
     * 
     */
    public SCPDDeviceType getDevice() {
        return device;
    }

    /**
     * Sets the value of the device property.
     * 
     * @param value
     *            allowed object is
     *            {@link SCPDDeviceType }
     * 
     */
    public void setDevice(SCPDDeviceType value) {
        this.device = value;
    }
}
