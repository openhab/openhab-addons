/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto.templates;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * See {@link TemplateModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "devices")
public class DeviceListModel {

    @XmlElement(name = "device")
    private List<DeviceModel> devices;

    public List<DeviceModel> getDevices() {
        if (devices == null) {
            devices = List.of();
        }
        return devices;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[devices=").append(devices).append("]").toString();
    }
}
