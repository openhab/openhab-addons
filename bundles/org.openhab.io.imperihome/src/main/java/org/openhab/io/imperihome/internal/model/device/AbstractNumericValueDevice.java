/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.imperihome.internal.model.device;

import org.openhab.core.items.Item;
import org.openhab.io.imperihome.internal.model.param.NumericValueParam;

/**
 * Parent of devices with a {@link NumericValueParam}. Contains the value unit.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public abstract class AbstractNumericValueDevice extends AbstractDevice {

    private transient String unit;

    public AbstractNumericValueDevice(DeviceType type, Item item, String defaultUnit) {
        super(type, item);
        this.unit = defaultUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "super=" + super.toString() + ", unit='" + unit + '\'' + '}';
    }
}
