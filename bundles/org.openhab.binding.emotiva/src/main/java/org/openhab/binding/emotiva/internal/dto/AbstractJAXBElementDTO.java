/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.dto;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

/**
 * Defines elements used by common request DTO classes.
 *
 * @author Espen Fossen - Initial contribution
 */
public class AbstractJAXBElementDTO {

    @XmlTransient
    protected List<EmotivaCommandDTO> commands;

    @XmlAnyElement
    protected List<JAXBElement<String>> jaxbElements;

    public List<EmotivaCommandDTO> getCommands() {
        return commands;
    }

    public void setCommands(List<EmotivaCommandDTO> commands) {
        this.commands = commands;
    }

    public void setJaxbElements(List<JAXBElement<String>> jaxbElements) {
        this.jaxbElements = jaxbElements;
    }

    public JAXBElement<String> createJAXBElement(QName name) {
        return new JAXBElement<String>(name, String.class, null);
    }
}
