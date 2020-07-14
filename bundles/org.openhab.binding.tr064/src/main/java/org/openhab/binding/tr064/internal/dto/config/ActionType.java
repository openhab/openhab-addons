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
 *         &lt;element name="parameter" type="{channelconfig}parameterType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="argument" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="postProcessor" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "actionType", propOrder = { "parameter" })
public class ActionType implements Serializable {

    private final static long serialVersionUID = 1L;
    protected ParameterType parameter;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "argument")
    protected String argument;
    @XmlAttribute(name = "postProcessor")
    protected String postProcessor;

    /**
     * Gets the value of the parameter property.
     * 
     * @return
     *         possible object is
     *         {@link ParameterType }
     * 
     */
    public ParameterType getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     * 
     * @param value
     *            allowed object is
     *            {@link ParameterType }
     * 
     */
    public void setParameter(ParameterType value) {
        this.parameter = value;
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
     * Gets the value of the argument property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Sets the value of the argument property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setArgument(String value) {
        this.argument = value;
    }

    /**
     * Gets the value of the postProcessor property.
     * 
     * @return
     *         possible object is
     *         {@link String }
     * 
     */
    public String getPostProcessor() {
        return postProcessor;
    }

    /**
     * Sets the value of the postProcessor property.
     * 
     * @param value
     *            allowed object is
     *            {@link String }
     * 
     */
    public void setPostProcessor(String value) {
        this.postProcessor = value;
    }
}
