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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.openhab.binding.tr064.internal.dto.config package.
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

    private final static QName _Channels_QNAME = new QName("channelconfig", "channels");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.openhab.binding.tr064.internal.dto.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChannelTypeDescriptions }
     * 
     */
    public ChannelTypeDescriptions createChannelTypeDescriptions() {
        return new ChannelTypeDescriptions();
    }

    /**
     * Create an instance of {@link ItemType }
     * 
     */
    public ItemType createItemType() {
        return new ItemType();
    }

    /**
     * Create an instance of {@link ServiceType }
     * 
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link ParameterType }
     * 
     */
    public ParameterType createParameterType() {
        return new ParameterType();
    }

    /**
     * Create an instance of {@link ActionType }
     * 
     */
    public ActionType createActionType() {
        return new ActionType();
    }

    /**
     * Create an instance of {@link ChannelTypeDescription }
     * 
     */
    public ChannelTypeDescription createChannelTypeDescription() {
        return new ChannelTypeDescription();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChannelTypeDescriptions }{@code >}
     * 
     * @param value
     *            Java instance representing xml element's value.
     * @return
     *         the new instance of {@link JAXBElement }{@code <}{@link ChannelTypeDescriptions }{@code >}
     */
    @XmlElementDecl(namespace = "channelconfig", name = "channels")
    public JAXBElement<ChannelTypeDescriptions> createChannels(ChannelTypeDescriptions value) {
        return new JAXBElement<ChannelTypeDescriptions>(_Channels_QNAME, ChannelTypeDescriptions.class, null, value);
    }
}
