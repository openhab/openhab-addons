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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Emotiva Control XML object, which is part of the {@link EmotivaTransponderDTO} response.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "control")
@XmlAccessorType(XmlAccessType.FIELD)
public class ControlDTO {

    @XmlElement(name = "version")
    String version;
    @XmlElement(name = "controlPort")
    int controlPort;
    @XmlElement(name = "notifyPort")
    int notifyPort;
    @XmlElement(name = "infoPort")
    int infoPort;
    @XmlElement(name = "menuNotifyPort")
    int menuNotifyPort;
    @XmlElement(name = "setupPortTCP")
    int setupPortTCP;
    @XmlElement(name = "setupXMLVersion")
    int setupXMLVersion;
    @XmlElement(name = "keepAlive")
    int keepAlive;

    public ControlDTO() {
    }

    public String getVersion() {
        return version;
    }

    public int getControlPort() {
        return controlPort;
    }

    public int getNotifyPort() {
        return notifyPort;
    }

    public int getInfoPort() {
        return infoPort;
    }

    public int getMenuNotifyPort() {
        return menuNotifyPort;
    }

    public int getSetupPortTCP() {
        return setupPortTCP;
    }

    public int getSetupXMLVersion() {
        return setupXMLVersion;
    }

    public int getKeepAlive() {
        return keepAlive;
    }
}
