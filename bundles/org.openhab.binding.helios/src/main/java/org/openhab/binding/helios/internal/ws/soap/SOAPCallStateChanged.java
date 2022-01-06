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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.helios.internal.handler.HeliosHandler27;

/**
 * Helios SOAP Protocol Message
 *
 * @author Karel Goderis - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Data", namespace = HeliosHandler27.HELIOS_URI)
public class SOAPCallStateChanged extends SOAPDataField {

    @XmlElement(name = "State", namespace = HeliosHandler27.HELIOS_URI)
    protected String state;

    @XmlElement(name = "Direction", namespace = HeliosHandler27.HELIOS_URI)
    protected String direction;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
