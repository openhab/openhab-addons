/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.helios.internal.ws.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openhab.binding.helios.internal.handler.HeliosHandler27;

/**
 * The {@link SOAPObjectFactory} is a helper class that is used to generate JAXB
 * Elements
 *
 * @author Karel Goderis - Initial contribution
 */
@XmlRegistry
public class SOAPObjectFactory {

    private static final QName DATA_QNAME = new QName(HeliosHandler27.HELIOS_URI, "Data");

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "CallStateChanged")
    public JAXBElement<SOAPCallStateChanged> createHeliosCallStateChanged(SOAPCallStateChanged value) {
        return new JAXBElement<>(DATA_QNAME, SOAPCallStateChanged.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "CodeEntered")
    public JAXBElement<SOAPCodeEntered> createHeliosCodeEntered(SOAPCodeEntered value) {
        return new JAXBElement<>(DATA_QNAME, SOAPCodeEntered.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "CardEntered")
    public JAXBElement<SOAPCardEntered> createHeliosCardEntered(SOAPCardEntered value) {
        return new JAXBElement<>(DATA_QNAME, SOAPCardEntered.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "DeviceState")
    public JAXBElement<SOAPDeviceState> createHeliosDeviceState(SOAPDeviceState value) {
        return new JAXBElement<>(DATA_QNAME, SOAPDeviceState.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "KeyPressed")
    public JAXBElement<SOAPKeyPressed> createHeliosKeyPressedd(SOAPKeyPressed value) {
        return new JAXBElement<>(DATA_QNAME, SOAPKeyPressed.class, null, value);
    }
}
