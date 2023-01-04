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

import java.util.List;
import java.util.Map;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.NumericValueParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;
import org.openhab.io.imperihome.internal.processor.TagType;

/**
 * Thermostat device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ThermostatDevice extends AbstractDevice {

    public ThermostatDevice(Item item) {
        super(DeviceType.THERMOSTAT, item);
    }

    @Override
    public void processCustomTags(Map<TagType, List<String>> issTags) {
        if (issTags.containsKey(TagType.STEP)) {
            setStep(issTags.get(TagType.STEP).get(0));
        }
        if (issTags.containsKey(TagType.MIN_VAL)) {
            setMinValue(issTags.get(TagType.MIN_VAL).get(0));
        }
        if (issTags.containsKey(TagType.MAX_VAL)) {
            setMaxValue(issTags.get(TagType.MAX_VAL).get(0));
        }
        if (issTags.containsKey(TagType.MODES)) {
            setAvailableModes(issTags.get(TagType.MODES).get(0));
        }
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        DecimalType state = (DecimalType) item.getStateAs(DecimalType.class);
        if (state != null) {
            DeviceParam param = new DeviceParam(ParamType.CUR_SETPOINT, state.floatValue());
            addParam(param);
        }
    }

    @Override
    public void updateParams() {
        AbstractDevice curModeDevice = getLinkedDevice("curmode", true);
        if (curModeDevice != null) {
            setCurModeParam(curModeDevice);
        }

        AbstractDevice curTempDevice = getLinkedDevice("curtemp", true);
        if (curTempDevice != null) {
            setCurTempParam(curTempDevice);
        }
    }

    public void setStep(String step) {
        addParam(new DeviceParam(ParamType.STEP, step));
    }

    public void setMinValue(String minValue) {
        addParam(new DeviceParam(ParamType.MIN_VAL, minValue));
    }

    public void setMaxValue(String maxValue) {
        addParam(new DeviceParam(ParamType.MAX_VAL, maxValue));
    }

    public void setAvailableModes(String modes) {
        addParam(new DeviceParam(ParamType.AVAIL_MODE, modes));
    }

    private void setCurModeParam(AbstractDevice device) {
        DeviceParam valueParam = device.getParams().get(ParamType.GENERIC_VALUE);
        if (valueParam == null) {
            logger.warn("Linked curmode device has no Value parameter: {}", device);
            return;
        }
        addParam(new DeviceParam(ParamType.CUR_MODE, valueParam.getValue()));
    }

    private void setCurTempParam(AbstractDevice device) {
        NumericValueParam valueParam = (NumericValueParam) device.getParams().get(ParamType.TEMPERATURE_VALUE);
        if (valueParam == null) {
            logger.warn("Linked curtemp device has no Value parameter: {}", device);
            return;
        }

        NumericValueParam tempParam = new NumericValueParam(ParamType.CUR_TEMP, valueParam.getUnit(), null);
        tempParam.setValue(valueParam.getValue());
        addParam(tempParam);
    }

    private AbstractDevice getLinkedDevice(String linkName, boolean logWhenMissing) {
        String deviceName = getLinks().get(linkName);
        AbstractDevice device = null;
        if (deviceName != null) {
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            device = getDeviceRegistry().getDevice(deviceId);
        }
        if (logWhenMissing && device == null) {
            logger.error("Couldn't resolve linked {} device '{}', make sure the Item has iss tags", linkName,
                    deviceName);
        }
        return device;
    }
}
