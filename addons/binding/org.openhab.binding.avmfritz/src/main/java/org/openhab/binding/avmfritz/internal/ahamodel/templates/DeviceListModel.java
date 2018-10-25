/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel.templates;

import java.util.ArrayList;

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
    private ArrayList<DeviceModel> devices;

    public ArrayList<DeviceModel> getDevices() {
        if (devices == null) {
            devices = new ArrayList<>();
        }
        return devices;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(getDevices()).toString();
    }
}
