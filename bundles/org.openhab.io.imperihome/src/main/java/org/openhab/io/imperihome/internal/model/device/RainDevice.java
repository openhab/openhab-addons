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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.NumericValueParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;

/**
 * Rain sensor device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class RainDevice extends AbstractNumericValueDevice {

    private static final String LINK_ACCUMULATION = "accum";

    public RainDevice(Item item) {
        super(DeviceType.RAIN, item, "mm/h");
    }

    @Override
    public void updateParams() {
        super.updateParams();

        if (getLinks().containsKey(LINK_ACCUMULATION)) {
            String deviceName = getLinks().get(LINK_ACCUMULATION);
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            AbstractDevice accumDevice = getDeviceRegistry().getDevice(deviceId);
            if (accumDevice == null) {
                logger.error("Couldn't resolve linked accumulation device '{}', make sure the Item has iss tags",
                        deviceName);
                return;
            }

            NumericValueParam valueParam = (NumericValueParam) accumDevice.getParams().get(ParamType.GENERIC_VALUE);
            if (valueParam == null) {
                logger.warn("Linked Accumulation device has no Value parameter: {}", accumDevice);
                return;
            }

            NumericValueParam accumParam = new NumericValueParam(ParamType.ACCUMULATION, valueParam.getUnit(), null);
            String unit = accumParam.getUnit();
            if (unit == null || unit.isBlank()) {
                accumParam.setUnit("mm");
            }

            accumParam.setValue(valueParam.getValue());
            addParam(accumParam);
        }
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        DecimalType value = item.getStateAs(DecimalType.class);
        addParam(new NumericValueParam(ParamType.RAIN_VALUE, getUnit(), value));
    }
}
