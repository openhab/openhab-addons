/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.live.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@XmlRootElement(name = "sensors")
public class TellstickNetSensors {

    List<TellstickNetSensor> sensors;

    @XmlElement(name = "sensor")
    public List<TellstickNetSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<TellstickNetSensor> devices) {
        this.sensors = devices;
    }
}
