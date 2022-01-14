/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;

/**
 * Abstraction of devices that allow a link to a current energy consumption item.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public abstract class AbstractEnergyLinkDevice extends AbstractDevice {

    public AbstractEnergyLinkDevice(DeviceType type, Item item) {
        super(type, item);
    }

    @Override
    public void updateParams() {
        super.updateParams();

        if (getLinks().containsKey("energy")) {
            String deviceName = getLinks().get("energy");
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            AbstractDevice energyDevice = getDeviceRegistry().getDevice(deviceId);
            if (energyDevice == null) {
                logger.error("Couldn't resolve linked energy device '{}', make sure the Item has iss tags", deviceName);
            } else {
                NumericValueParam valueParam = (NumericValueParam) energyDevice.getParams().get(ParamType.WATTS);
                if (valueParam == null) {
                    logger.warn("Linked energy device has no Watts value parameter: {}", energyDevice);
                } else {
                    NumericValueParam energyParam = new NumericValueParam(ParamType.ENERGY, valueParam.getUnit(), null);
                    energyParam.setValue(valueParam.getValue());
                    addParam(energyParam);
                }
            }
        }
    }
}
