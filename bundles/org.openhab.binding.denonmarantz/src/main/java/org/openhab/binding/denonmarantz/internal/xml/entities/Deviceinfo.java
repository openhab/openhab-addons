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
package org.openhab.binding.denonmarantz.internal.xml.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains information about a Denon/Marantz receiver.
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "device_Info")
@XmlAccessorType(XmlAccessType.FIELD)
@NonNullByDefault
public class Deviceinfo {

    private @Nullable Integer deviceZones;

    private @Nullable String modelName;

    public @Nullable Integer getDeviceZones() {
        return deviceZones;
    }

    public void setDeviceZones(Integer deviceZones) {
        this.deviceZones = deviceZones;
    }

    public @Nullable String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
