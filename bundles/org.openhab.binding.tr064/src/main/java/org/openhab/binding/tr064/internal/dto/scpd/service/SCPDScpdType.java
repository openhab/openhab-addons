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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for scpdType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scpdType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="specVersion" type="{urn:dslforum-org:service-1-0}specVersionType"/&gt;
 *         &lt;element name="actionList" type="{urn:dslforum-org:service-1-0}actionListType"/&gt;
 *         &lt;element name="serviceStateTable" type="{urn:dslforum-org:service-1-0}serviceStateTableType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scpdType", propOrder = { "specVersion", "actionList", "serviceStateTable" })
public class SCPDScpdType implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected SCPDSpecVersionType specVersion;
    @XmlElementWrapper(required = true)
    @XmlElement(name = "action", namespace = "urn:dslforum-org:service-1-0")
    protected List<SCPDActionType> actionList = new ArrayList<SCPDActionType>();
    @XmlElementWrapper(required = true)
    @XmlElement(name = "stateVariable", namespace = "urn:dslforum-org:service-1-0")
    protected List<SCPDStateVariableType> serviceStateTable = new ArrayList<SCPDStateVariableType>();

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

    public List<SCPDActionType> getActionList() {
        return actionList;
    }

    public void setActionList(List<SCPDActionType> actionList) {
        this.actionList = actionList;
    }

    public List<SCPDStateVariableType> getServiceStateTable() {
        return serviceStateTable;
    }

    public void setServiceStateTable(List<SCPDStateVariableType> serviceStateTable) {
        this.serviceStateTable = serviceStateTable;
    }
}
