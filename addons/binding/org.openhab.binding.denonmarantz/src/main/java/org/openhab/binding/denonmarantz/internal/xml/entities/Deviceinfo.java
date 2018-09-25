/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains information about a Denon/Marantz receiver.
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "device_Info")
@XmlAccessorType(XmlAccessType.FIELD)
public class Deviceinfo {

    private Integer deviceZones;

    private String modelName;

    public Integer getDeviceZones() {
        return deviceZones;
    }

    public void setDeviceZones(Integer deviceZones) {
        this.deviceZones = deviceZones;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
