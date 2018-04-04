/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal.live.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland
 *
 */
@XmlRootElement(name = "sensors")
public class TellstickNetSensors {

    public TellstickNetSensors() {
        super();
    }

    List<TellstickNetSensor> sensors;

    @XmlElement(name = "sensor")
    public List<TellstickNetSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<TellstickNetSensor> devices) {
        this.sensors = devices;
    }
}
