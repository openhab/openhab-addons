/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios.internal.ws.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openhab.binding.helios.handler.HeliosHandler27;

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
        return new JAXBElement<SOAPCallStateChanged>(DATA_QNAME, SOAPCallStateChanged.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "CodeEntered")
    public JAXBElement<SOAPCodeEntered> createHeliosCodeEntered(SOAPCodeEntered value) {
        return new JAXBElement<SOAPCodeEntered>(DATA_QNAME, SOAPCodeEntered.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "CardEntered")
    public JAXBElement<SOAPCardEntered> createHeliosCardEntered(SOAPCardEntered value) {
        return new JAXBElement<SOAPCardEntered>(DATA_QNAME, SOAPCardEntered.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "DeviceState")
    public JAXBElement<SOAPDeviceState> createHeliosDeviceState(SOAPDeviceState value) {
        return new JAXBElement<SOAPDeviceState>(DATA_QNAME, SOAPDeviceState.class, null, value);
    }

    @XmlElementDecl(namespace = HeliosHandler27.HELIOS_URI, name = "KeyPressed")
    public JAXBElement<SOAPKeyPressed> createHeliosKeyPressedd(SOAPKeyPressed value) {
        return new JAXBElement<SOAPKeyPressed>(DATA_QNAME, SOAPKeyPressed.class, null, value);
    }

}
