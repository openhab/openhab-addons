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
 * Java class for systemVersionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="systemVersionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="HW" type="{http://www.w3.org/2001/XMLSchema}short"/&gt;
 *         &lt;element name="Major" type="{http://www.w3.org/2001/XMLSchema}short"/&gt;
 *         &lt;element name="Minor" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="Patch" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="Buildnumber" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="Display" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "systemVersionType", propOrder = { "hw", "major", "minor", "patch", "buildnumber", "display" })
public class SCPDSystemVersionType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "HW")
    protected short hw;
    @XmlElement(name = "Major")
    protected short major;
    @XmlElement(name = "Minor")
    protected byte minor;
    @XmlElement(name = "Patch")
    protected byte patch;
    @XmlElement(name = "Buildnumber")
    protected int buildnumber;
    @XmlElement(name = "Display", required = true)
    protected String display;

    /**
     * Gets the value of the hw property.
     * 
     */
    public short getHW() {
        return hw;
    }

    /**
     * Sets the value of the hw property.
     * 
     */
    public void setHW(short value) {
        this.hw = value;
    }

    /**
     * Gets the value of the major property.
     * 
     */
    public short getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     */
    public void setMajor(short value) {
        this.major = value;
    }

    /**
     * Gets the value of the minor property.
     * 
     */
    public byte getMinor() {
        return minor;
    }

    /**
     * Sets the value of the minor property.
     * 
     */
    public void setMinor(byte value) {
        this.minor = value;
    }

    /**
     * Gets the value of the patch property.
     * 
     */
    public byte getPatch() {
        return patch;
    }

    /**
     * Sets the value of the patch property.
     * 
     */
    public void setPatch(byte value) {
        this.patch = value;
    }

    /**
     * Gets the value of the buildnumber property.
     * 
     */
    public int getBuildnumber() {
        return buildnumber;
    }

    /**
     * Sets the value of the buildnumber property.
     * 
     */
    public void setBuildnumber(int value) {
        this.buildnumber = value;
    }

    /**
     * Gets the value of the display property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the value of the display property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setDisplay(String value) {
        this.display = value;
    }
}
