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
 * Electricity device, containing current (Watt) and total (KWh) consumption.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ElectricityDevice extends AbstractNumericValueDevice {

    private static final String LINK_WATTS = "watt";
    private static final String LINK_KWH = "kwh";

    public ElectricityDevice(Item item) {
        super(DeviceType.ELECTRICITY, item, "W");
    }

    @Override
    public void addLink(String linkType, String deviceId) {
        super.addLink(linkType, deviceId);

        if (getLinks().containsKey(LINK_WATTS)) {
            setUnit("KWh");
        } else if (getLinks().containsKey(LINK_KWH)) {
            setUnit("W");
        }
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        DecimalType value = item.getStateAs(DecimalType.class);

        if (getLinks().containsKey(LINK_WATTS) || getUnit().equalsIgnoreCase(LINK_KWH)) {
            addParam(new NumericValueParam(ParamType.KWH, getUnit(), value));
        } else if (getLinks().isEmpty() || getLinks().containsKey(LINK_KWH)) {
            addParam(new NumericValueParam(ParamType.WATTS, getUnit(), value));
        }
    }

    @Override
    public void updateParams() {
        super.updateParams();

        if (getLinks().containsKey(LINK_WATTS)) {
            String deviceName = getLinks().get(LINK_WATTS);
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            AbstractDevice wattsDevice = getDeviceRegistry().getDevice(deviceId);
            if (wattsDevice == null) {
                logger.error("Couldn't resolve linked watts device '{}', make sure the Item has iss tags", deviceName);
            } else {
                setWattsParam(wattsDevice);
            }
        }

        if (getLinks().containsKey(LINK_KWH)) {
            String deviceName = getLinks().get(LINK_KWH);
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            AbstractDevice kwhDevice = getDeviceRegistry().getDevice(deviceId);
            if (kwhDevice == null) {
                logger.error("Couldn't resolve linked KWh device '{}', make sure the Item has iss tags", deviceName);
            } else {
                setKwhParam(kwhDevice);
            }
        }
    }

    private void setWattsParam(AbstractDevice device) {
        NumericValueParam valueParam = (NumericValueParam) device.getParams().get(ParamType.WATTS);
        if (valueParam == null) {
            logger.warn("Linked Watts device has no Watt value parameter: {}", device);
            return;
        }

        NumericValueParam wattsParam = new NumericValueParam(ParamType.WATTS, valueParam.getUnit(), null);
        String unit = wattsParam.getUnit();
        if (unit == null || unit.isEmpty()) {
            wattsParam.setUnit("W");
        }
        wattsParam.setValue(valueParam.getValue());
        addParam(wattsParam);
    }

    private void setKwhParam(AbstractDevice device) {
        NumericValueParam valueParam = (NumericValueParam) device.getParams().get(ParamType.KWH);
        if (valueParam == null) {
            logger.warn("Linked KWh device has no KWh value parameter: {}", device);
            return;
        }

        NumericValueParam kwhParam = new NumericValueParam(ParamType.KWH, valueParam.getUnit(), null);
        String unit = kwhParam.getUnit();
        if (unit == null || unit.isEmpty()) {
            kwhParam.setUnit("KWh");
        }
        kwhParam.setValue(valueParam.getValue());
        addParam(kwhParam);
    }
}
