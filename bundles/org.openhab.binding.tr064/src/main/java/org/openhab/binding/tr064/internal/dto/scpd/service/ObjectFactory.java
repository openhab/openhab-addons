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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.openhab.binding.tr064.internal.dto.scpd.service package.
 * <p>
 * An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups. Factory methods for each of these are
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Scpd_QNAME = new QName("urn:dslforum-org:service-1-0", "scpd");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.openhab.binding.tr064.internal.dto.scpd.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SCPDScpdType }
     * 
     */
    public SCPDScpdType createSCPDScpdType() {
        return new SCPDScpdType();
    }

    /**
     * Create an instance of {@link SCPDSpecVersionType }
     * 
     */
    public SCPDSpecVersionType createSCPDSpecVersionType() {
        return new SCPDSpecVersionType();
    }

    /**
     * Create an instance of {@link SCPDArgumentType }
     * 
     */
    public SCPDArgumentType createSCPDArgumentType() {
        return new SCPDArgumentType();
    }

    /**
     * Create an instance of {@link SCPDActionType }
     * 
     */
    public SCPDActionType createSCPDActionType() {
        return new SCPDActionType();
    }

    /**
     * Create an instance of {@link SCPDStateVariableType }
     * 
     */
    public SCPDStateVariableType createSCPDStateVariableType() {
        return new SCPDStateVariableType();
    }

    /**
     * Create an instance of {@link SCPDAllowedValueRangeType }
     * 
     */
    public SCPDAllowedValueRangeType createSCPDAllowedValueRangeType() {
        return new SCPDAllowedValueRangeType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SCPDScpdType }{@code >}
     * 
     * @param value
     *            Java instance representing xml element's value.
     * @return
     *         the new instance of {@link JAXBElement }{@code <}{@link SCPDScpdType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:dslforum-org:service-1-0", name = "scpd")
    public JAXBElement<SCPDScpdType> createScpd(SCPDScpdType value) {
        return new JAXBElement<SCPDScpdType>(_Scpd_QNAME, SCPDScpdType.class, null, value);
    }
}
