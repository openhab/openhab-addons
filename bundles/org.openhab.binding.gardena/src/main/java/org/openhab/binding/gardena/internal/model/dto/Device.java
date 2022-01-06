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
package org.openhab.binding.gardena.internal.model.dto;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.api.CommonServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.api.DeviceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.LocationDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.MowerServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.PowerSocketServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.SensorServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.ValveServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.ValveSetServiceDataItem;
import org.openhab.binding.gardena.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Gardena device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Device {
    private final Logger logger = LoggerFactory.getLogger(Device.class);

    private transient static final String DEVICE_TYPE_PREFIX = "gardena smart";
    public boolean active = true;
    public String id;
    public String deviceType;
    public String location;
    public CommonServiceDataItem common;
    public MowerServiceDataItem mower;
    public PowerSocketServiceDataItem powerSocket;
    public SensorServiceDataItem sensor;
    public ValveServiceDataItem valve;
    public ValveServiceDataItem valveOne;
    public ValveServiceDataItem valveTwo;
    public ValveServiceDataItem valveThree;
    public ValveServiceDataItem valveFour;
    public ValveServiceDataItem valveFive;
    public ValveServiceDataItem valveSix;
    public ValveSetServiceDataItem valveSet;

    private Map<String, LocalService> localServices = new HashMap<>();

    public Device(String id) {
        this.id = id;
    }

    /**
     * Returns the local service or creates one if it does not exist.
     */
    public LocalService getLocalService(String key) {
        LocalService localService = localServices.get(key);
        if (localService == null) {
            localService = new LocalService();
            localServices.put(key, localService);
            localService.commandDuration = 3600;
        }
        return localService;
    }

    /**
     * Evaluates the device type.
     */
    public void evaluateDeviceType() {
        if (deviceType == null) {
            if (common.attributes.modelType.value.toLowerCase().startsWith(DEVICE_TYPE_PREFIX)) {
                String modelType = common.attributes.modelType.value.toLowerCase();
                modelType = modelType.substring(14);
                deviceType = modelType.replace(" ", "_");
            } else {
                // workaround: we have to guess the device type, valves cannot be identified if modeType is wrong
                if (mower != null) {
                    deviceType = DEVICE_TYPE_MOWER;
                } else if (powerSocket != null) {
                    deviceType = DEVICE_TYPE_POWER;
                } else if (sensor != null) {
                    deviceType = DEVICE_TYPE_SENSOR;
                }
            }
            if (deviceType == null) {
                logger.warn("Can't identify device with id {}, wrong modelType sent from the Gardena API", id);
                active = false;
            }
        }
    }

    /**
     * Assigns the dataItem to the corresponding property.
     */
    public void setDataItem(DataItem<?> dataItem) throws GardenaException {
        if (dataItem instanceof DeviceDataItem) {
            // ignore
        } else if (dataItem instanceof LocationDataItem) {
            LocationDataItem locationDataItem = (LocationDataItem) dataItem;
            if (locationDataItem.attributes != null) {
                location = locationDataItem.attributes.name;
            }
        } else if (dataItem instanceof CommonServiceDataItem) {
            common = (CommonServiceDataItem) dataItem;
        } else if (dataItem instanceof MowerServiceDataItem) {
            mower = (MowerServiceDataItem) dataItem;
        } else if (dataItem instanceof PowerSocketServiceDataItem) {
            powerSocket = (PowerSocketServiceDataItem) dataItem;
        } else if (dataItem instanceof SensorServiceDataItem) {
            sensor = (SensorServiceDataItem) dataItem;
        } else if (dataItem instanceof ValveSetServiceDataItem) {
            valveSet = (ValveSetServiceDataItem) dataItem;
        } else if (dataItem instanceof ValveServiceDataItem) {
            String valveNumber = StringUtils.substringAfterLast(dataItem.id, ":");
            if (valveNumber != null
                    && (valveNumber.equals("") || valveNumber.equals("wc") || valveNumber.equals("0"))) {
                valve = (ValveServiceDataItem) dataItem;
            } else if ("1".equals(valveNumber)) {
                valveOne = (ValveServiceDataItem) dataItem;
            } else if ("2".equals(valveNumber)) {
                valveTwo = (ValveServiceDataItem) dataItem;
            } else if ("3".equals(valveNumber)) {
                valveThree = (ValveServiceDataItem) dataItem;
            } else if ("4".equals(valveNumber)) {
                valveFour = (ValveServiceDataItem) dataItem;
            } else if ("5".equals(valveNumber)) {
                valveFive = (ValveServiceDataItem) dataItem;
            } else if ("6".equals(valveNumber)) {
                valveSix = (ValveServiceDataItem) dataItem;
            } else {
                throw new GardenaException("Unknown valveNumber in dataItem with id: " + dataItem.id);
            }
        } else {
            throw new GardenaException("Unknown dataItem with id: " + dataItem.id);
        }

        if (common != null && common.attributes != null) {
            common.attributes.lastUpdate.timestamp = new Date();
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Device)) {
            return false;
        }
        Device comp = (Device) obj;
        return comp.id.equals(id);
    }
}
