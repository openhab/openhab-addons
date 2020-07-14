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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for allowedValueRangeType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="allowedValueRangeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="minimum" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="maximum" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="step" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "allowedValueRangeType", propOrder = { "minimum", "maximum", "step" })
public class SCPDAllowedValueRangeType implements Serializable {

    private final static long serialVersionUID = 1L;
    protected byte minimum;
    protected byte maximum;
    protected byte step;

    /**
     * Gets the value of the minimum property.
     * 
     */
    public byte getMinimum() {
        return minimum;
    }

    /**
     * Sets the value of the minimum property.
     * 
     */
    public void setMinimum(byte value) {
        this.minimum = value;
    }

    /**
     * Gets the value of the maximum property.
     * 
     */
    public byte getMaximum() {
        return maximum;
    }

    /**
     * Sets the value of the maximum property.
     * 
     */
    public void setMaximum(byte value) {
        this.maximum = value;
    }

    /**
     * Gets the value of the step property.
     * 
     */
    public byte getStep() {
        return step;
    }

    /**
     * Sets the value of the step property.
     * 
     */
    public void setStep(byte value) {
        this.step = value;
    }
}
