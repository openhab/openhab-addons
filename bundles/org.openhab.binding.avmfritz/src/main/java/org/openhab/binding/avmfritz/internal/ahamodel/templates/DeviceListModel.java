/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.ahamodel.templates;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@ TemplateModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlRootElement(name = "devices")
public class DeviceListModel {

    @XmlElement(name = "device")
    private List<DeviceModel> devices;

    public List<DeviceModel> getDevices() {
        if (devices == null) {
            devices = Collections.emptyList();
        }
        return devices;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(getDevices()).toString();
    }
}
