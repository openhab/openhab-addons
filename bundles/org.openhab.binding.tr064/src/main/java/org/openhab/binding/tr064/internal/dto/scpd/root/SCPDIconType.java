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
 * Java class for iconType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="iconType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="mimetype" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="depth" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "iconType", propOrder = { "mimetype", "width", "height", "depth", "url" })
public class SCPDIconType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String mimetype;
    protected byte width;
    protected byte height;
    protected byte depth;
    @XmlElement(required = true)
    protected String url;

    /**
     * Gets the value of the mimetype property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * Sets the value of the mimetype property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setMimetype(String value) {
        this.mimetype = value;
    }

    /**
     * Gets the value of the width property.
     * 
     */
    public byte getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     */
    public void setWidth(byte value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     */
    public byte getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     */
    public void setHeight(byte value) {
        this.height = value;
    }

    /**
     * Gets the value of the depth property.
     * 
     */
    public byte getDepth() {
        return depth;
    }

    /**
     * Sets the value of the depth property.
     * 
     */
    public void setDepth(byte value) {
        this.depth = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setUrl(String value) {
        this.url = value;
    }
}
