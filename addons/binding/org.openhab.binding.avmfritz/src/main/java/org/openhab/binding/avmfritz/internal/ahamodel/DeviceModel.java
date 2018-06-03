/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link AVMFritzBaseModel}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@XmlType(name = "device")
public class DeviceModel extends AVMFritzBaseModel {

    private TemperatureModel temperature;

    public TemperatureModel getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureModel temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(super.toString()).append(getTemperature()).toString();
    }
}
