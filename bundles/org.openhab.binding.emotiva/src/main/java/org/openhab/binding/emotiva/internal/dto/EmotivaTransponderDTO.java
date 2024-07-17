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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaTransponder message type. Received from a device if after a successful device discovery via the
 * {@link EmotivaPingDTO} message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaTransponder")
public class EmotivaTransponderDTO {

    @XmlElement(name = "model")
    private String model;
    @XmlElement(name = "revision")
    private String revision;
    @XmlElement(name = "dataRevision")
    private String dataRevision;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "control")
    private ControlDTO control;

    public java.lang.String getModel() {
        return model;
    }

    public java.lang.String getRevision() {
        return revision;
    }

    public String getDataRevision() {
        return dataRevision;
    }

    public String getName() {
        return name;
    }

    public ControlDTO getControl() {
        return control;
    }
}
