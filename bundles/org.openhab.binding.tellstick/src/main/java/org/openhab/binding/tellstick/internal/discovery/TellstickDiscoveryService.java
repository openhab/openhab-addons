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
package org.openhab.binding.tellstick.internal.discovery;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openhab.binding.tellstick.internal.TellstickBindingConstants;
import org.openhab.binding.tellstick.internal.handler.DeviceStatusListener;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.live.xml.LiveDataType;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetDevice;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetSensor;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalDeviceDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorDTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.JNA;
import org.tellstick.device.TellstickSensor;
import org.tellstick.device.iface.Device;
import org.tellstick.device.iface.DimmableDevice;
import org.tellstick.device.iface.SwitchableDevice;
import org.tellstick.device.iface.TellstickEvent;
import org.tellstick.enums.DataType;

/**
 * The {@link TellstickDiscoveryService} class is used to discover Tellstick
 * devices that are connected to the Lan gateway.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickDiscoveryService extends AbstractDiscoveryService implements DeviceStatusListener {
    private static final long DEFAULT_TTL = 60 * 60; // 1 Hour

    public TellstickDiscoveryService(int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    private final Logger logger = LoggerFactory.getLogger(TellstickDiscoveryService.class);

    private List<TelldusBridgeHandler> telldusBridgeHandlers = new Vector<>();

    public TellstickDiscoveryService(TelldusBridgeHandler telldusBridgeHandler) {
        super(TellstickBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, 10, true);
        this.telldusBridgeHandlers.add(telldusBridgeHandler);
    }

    public void activate() {
        for (TelldusBridgeHandler telldusBridgeHandler : telldusBridgeHandlers) {
            telldusBridgeHandler.registerDeviceStatusListener(this);
        }
    }

    @Override
    public void deactivate() {
        for (TelldusBridgeHandler telldusBridgeHandler : telldusBridgeHandlers) {
            telldusBridgeHandler.unregisterDeviceStatusListener(this);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return TellstickBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        logger.debug("Adding new TellstickDevice! '{}' with id '{}' and type '{}' to inbox", device, device.getId(),
                device.getDeviceType());
        ThingUID thingUID = getThingUID(bridge, device);
        logger.debug("Detected thingUID: {}", thingUID);
        if (thingUID != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withTTL(DEFAULT_TTL)
                    .withProperty(TellstickBindingConstants.DEVICE_ID, device.getUUId())
                    .withProperty(TellstickBindingConstants.DEVICE_NAME, device.getName()).withBridge(bridge.getUID())
                    .withLabel(device.getDeviceType() + ": " + device.getName()).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.warn("Discovered Tellstick! device is unsupported: type '{}' with id '{}'", device.getDeviceType(),
                    device.getId());
        }
    }

    @Override
    protected void startScan() {
        for (TelldusBridgeHandler telldusBridgeHandler : telldusBridgeHandlers) {
            telldusBridgeHandler.rescanTelldusDevices();
        }
    }

    @Override
    public void onDeviceStateChanged(Bridge bridge, Device device, TellstickEvent event) {
        // this can be ignored here
    }

    @Override
    public void onDeviceRemoved(Bridge bridge, Device device) {
        ThingUID thingUID = getThingUID(bridge, device);
        if (thingUID != null) {
            thingRemoved(thingUID);
        } else {
            logger.warn("Removed Tellstick! device is unsupported: type '{}' with id '{}'", device.getDeviceType(),
                    device.getId());
        }
    }

    private ThingUID getThingUID(Bridge bridge, Device device) {
        ThingUID thingUID = null;
        switch (device.getDeviceType()) {
            case SENSOR:
                ThingTypeUID sensorThingId = findSensorType(device);
                thingUID = new ThingUID(sensorThingId, bridge.getUID(), device.getUUId());
                break;
            case DEVICE:
                if (device instanceof DimmableDevice) {
                    thingUID = new ThingUID(TellstickBindingConstants.DIMMER_THING_TYPE, bridge.getUID(),
                            device.getUUId());
                } else if (device instanceof SwitchableDevice) {
                    thingUID = new ThingUID(TellstickBindingConstants.SWITCH_THING_TYPE, bridge.getUID(),
                            device.getUUId());
                } else if (device instanceof TellstickNetDevice) {
                    if ((((TellstickNetDevice) device).getMethods() & JNA.CLibrary.TELLSTICK_DIM) > 0) {
                        thingUID = new ThingUID(TellstickBindingConstants.DIMMER_THING_TYPE, bridge.getUID(),
                                device.getUUId());
                    } else {
                        thingUID = new ThingUID(TellstickBindingConstants.SWITCH_THING_TYPE, bridge.getUID(),
                                device.getUUId());
                    }
                } else if (device instanceof TellstickLocalDeviceDTO) {
                    if ((((TellstickLocalDeviceDTO) device).getMethods() & JNA.CLibrary.TELLSTICK_DIM) > 0) {
                        thingUID = new ThingUID(TellstickBindingConstants.DIMMER_THING_TYPE, bridge.getUID(),
                                device.getUUId());
                    } else {
                        thingUID = new ThingUID(TellstickBindingConstants.SWITCH_THING_TYPE, bridge.getUID(),
                                device.getUUId());
                    }
                }
                break;
            default:
                break;
        }
        return thingUID;
    }

    private ThingTypeUID findSensorType(Device device) {
        logger.debug("Device: {}", device);
        ThingTypeUID sensorThingId;
        if (device instanceof TellstickSensor) {
            TellstickSensor sensor = (TellstickSensor) device;
            logger.debug("Sensor: {}", device);
            if (sensor.getData(DataType.WINDAVERAGE) != null || sensor.getData(DataType.WINDGUST) != null
                    || sensor.getData(DataType.WINDDIRECTION) != null) {
                sensorThingId = TellstickBindingConstants.WINDSENSOR_THING_TYPE;
            } else if (sensor.getData(DataType.RAINTOTAL) != null || sensor.getData(DataType.RAINRATE) != null) {
                sensorThingId = TellstickBindingConstants.RAINSENSOR_THING_TYPE;
            } else {
                sensorThingId = TellstickBindingConstants.SENSOR_THING_TYPE;
            }
        } else if (device instanceof TellstickNetSensor) {
            TellstickNetSensor sensor = (TellstickNetSensor) device;
            if (sensor.isSensorOfType(LiveDataType.WINDAVERAGE) || sensor.isSensorOfType(LiveDataType.WINDDIRECTION)
                    || sensor.isSensorOfType(LiveDataType.WINDGUST)) {
                sensorThingId = TellstickBindingConstants.WINDSENSOR_THING_TYPE;
            } else if (sensor.isSensorOfType(LiveDataType.RAINRATE) || sensor.isSensorOfType(LiveDataType.RAINTOTAL)) {
                sensorThingId = TellstickBindingConstants.RAINSENSOR_THING_TYPE;
            } else if (sensor.isSensorOfType(LiveDataType.WATT)) {
                sensorThingId = TellstickBindingConstants.POWERSENSOR_THING_TYPE;
            } else {
                sensorThingId = TellstickBindingConstants.SENSOR_THING_TYPE;
            }
        } else {
            TellstickLocalSensorDTO sensor = (TellstickLocalSensorDTO) device;
            if (sensor.isSensorOfType(LiveDataType.WINDAVERAGE) || sensor.isSensorOfType(LiveDataType.WINDDIRECTION)
                    || sensor.isSensorOfType(LiveDataType.WINDGUST)) {
                sensorThingId = TellstickBindingConstants.WINDSENSOR_THING_TYPE;
            } else if (sensor.isSensorOfType(LiveDataType.RAINRATE) || sensor.isSensorOfType(LiveDataType.RAINTOTAL)) {
                sensorThingId = TellstickBindingConstants.RAINSENSOR_THING_TYPE;
            } else if (sensor.isSensorOfType(LiveDataType.WATT)) {
                sensorThingId = TellstickBindingConstants.POWERSENSOR_THING_TYPE;
            } else {
                sensorThingId = TellstickBindingConstants.SENSOR_THING_TYPE;
            }
        }
        return sensorThingId;
    }

    public void addBridgeHandler(TelldusBridgeHandler tellstickBridgeHandler) {
        telldusBridgeHandlers.add(tellstickBridgeHandler);
        tellstickBridgeHandler.registerDeviceStatusListener(this);
    }
}
