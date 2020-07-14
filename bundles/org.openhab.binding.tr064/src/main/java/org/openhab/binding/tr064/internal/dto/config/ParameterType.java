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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for parameterType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="parameterType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="thingParameter" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="internalOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameterType", propOrder = { "value" })
public class ParameterType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "thingParameter", required = true)
    protected String thingParameter;
    @XmlAttribute(name = "pattern")
    protected String pattern;
    @XmlAttribute(name = "internalOnly")
    protected Boolean internalOnly;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setValue(String value) {
        this.value = value;
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
     * Gets the value of the thingParameter property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getThingParameter() {
        return thingParameter;
    }

    /**
     * Sets the value of the thingParameter property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setThingParameter(String value) {
        this.thingParameter = value;
    }

    /**
     * Gets the value of the pattern property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setPattern(String value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the internalOnly property.
     * 
     * @return
     *         possible object is
     *         {@link Boolean }
     * 
     */
    public boolean isInternalOnly() {
        if (internalOnly == null) {
            return false;
        } else {
            return internalOnly;
        }
    }

    /**
     * Sets the value of the internalOnly property.
     * 
     * @param value
     *            allowed object is
     *            {@link Boolean }
     * 
     */
    public void setInternalOnly(Boolean value) {
        this.internalOnly = value;
    }
}
