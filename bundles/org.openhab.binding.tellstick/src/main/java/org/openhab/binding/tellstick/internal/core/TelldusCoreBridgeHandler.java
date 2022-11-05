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
package org.openhab.binding.tellstick.internal.core;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.tellstick.internal.conf.TellstickBridgeConfiguration;
import org.openhab.binding.tellstick.internal.handler.DeviceStatusListener;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.handler.TelldusDevicesHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.JNA;
import org.tellstick.device.SupportedMethodsException;
import org.tellstick.device.TellstickDevice;
import org.tellstick.device.TellstickDeviceEvent;
import org.tellstick.device.TellstickEventHandler;
import org.tellstick.device.TellstickSensor;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.Device;
import org.tellstick.device.iface.DeviceChangeListener;
import org.tellstick.device.iface.SensorListener;
import org.tellstick.enums.ChangeType;
import org.tellstick.enums.DataType;

/**
 * {@link TelldusCoreBridgeHandler} is the handler for Telldus Core (Duo and Basic) and connects it
 * to the framework. All {@link TelldusDevicesHandler}s use the
 * {@link TelldusCoreDeviceController} to execute the actual commands.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TelldusCoreBridgeHandler extends BaseBridgeHandler
        implements DeviceChangeListener, SensorListener, TelldusBridgeHandler {

    public TelldusCoreBridgeHandler(Bridge br) {
        super(br);
    }

    private Logger logger = LoggerFactory.getLogger(TelldusCoreBridgeHandler.class);
    private TelldusDeviceController deviceController = null;
    private List<TellstickDevice> deviceList = new Vector<>();
    private List<TellstickSensor> sensorList = new Vector<>();
    private TellstickEventHandler eventHandler;
    private static boolean initialized = false;
    private Set<DeviceStatusListener> deviceStatusListeners = ConcurrentHashMap.newKeySet();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received.");
            rescanTelldusDevices();
        } else {
            logger.warn("No bridge commands defined.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Telldus Core Handler disposed.");
        if (deviceController != null) {
            deviceController.dispose();
            deviceController = null;
        }
        if (eventHandler != null) {
            eventHandler.remove();
            eventHandler = null;
        }
        clearDeviceList();
        initialized = false;
        JNA.CLibrary.INSTANCE.tdClose();
        super.dispose();
    }

    private String init(String libraryPath) {
        if (!initialized) {
            if (libraryPath != null) {
                logger.info("Loading {} from {}", JNA.nativeLibrary, libraryPath);
                System.setProperty("jna.library.path", libraryPath);
            } else {
                logger.info("Loading {} from system default paths", JNA.nativeLibrary);
            }
            TellstickDevice.setSupportedMethods(JNA.CLibrary.TELLSTICK_BELL | JNA.CLibrary.TELLSTICK_TURNOFF
                    | JNA.CLibrary.TELLSTICK_TURNON | JNA.CLibrary.TELLSTICK_DIM);
            JNA.CLibrary.INSTANCE.tdInit();
            initialized = true;
        }
        return libraryPath;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Tellstick bridge handler.");
        TellstickBridgeConfiguration configuration = getConfigAs(TellstickBridgeConfiguration.class);
        init(configuration.libraryPath);

        scheduler.submit(() -> {
            rescanTelldusDevices();
            setupListeners();
            setupDeviceController(configuration);
            updateStatus(ThingStatus.ONLINE);
        });

        updateStatus(ThingStatus.UNKNOWN);
    }

    private void setupDeviceController(TellstickBridgeConfiguration configuration) {
        deviceController = new TelldusCoreDeviceController(configuration.resendInterval);
        eventHandler.addListener((TelldusCoreDeviceController) deviceController);
    }

    @Override
    public void rescanTelldusDevices() {
        try {
            deviceList = Collections.synchronizedList(TellstickDevice.getDevices());
            for (TellstickDevice device : deviceList) {
                for (DeviceStatusListener listener : deviceStatusListeners) {
                    listener.onDeviceAdded(getThing(), device);
                }
                for (DeviceStatusListener listener : deviceStatusListeners) {
                    listener.onDeviceStateChanged(getThing(), device,
                            new TellstickDeviceEvent(device, null, null, null, System.currentTimeMillis()));
                }
            }

            sensorList = Collections.synchronizedList(TellstickSensor.getAllSensors());
            for (TellstickSensor sensor : sensorList) {
                for (DeviceStatusListener listener : deviceStatusListeners) {
                    listener.onDeviceAdded(getThing(), sensor);
                }
                for (DeviceStatusListener listener : deviceStatusListeners) {
                    for (DataType type : sensor.getData().keySet()) {
                        listener.onDeviceStateChanged(getThing(), sensor,
                                new TellstickSensorEvent(sensor.getId(), sensor.getData(type), type,
                                        sensor.getProtocol(), sensor.getModel(), System.currentTimeMillis()));
                    }

                }
            }
        } catch (SupportedMethodsException e) {
            logger.error("Failed to get devices ", e);
        }
    }

    private synchronized void setupListeners() {
        eventHandler = new TellstickEventHandler(deviceList);
        eventHandler.addListener(this);
    }

    public void onConnectionLost() {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    public void onConnection() {
        logger.debug("Bridge connected. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.add(deviceStatusListener);
    }

    @Override
    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    public void clearDeviceList() {
        deviceList.clear();
        sensorList.clear();
    }

    private Device getDevice(String id, List<TellstickDevice> devices) {
        for (Device device : devices) {
            if (device.getId() == Integer.valueOf(id)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public Device getDevice(String serialNumber) {
        return getDevice(serialNumber, deviceList);
    }

    @Override
    public void onRequest(TellstickSensorEvent newEvent) {
        String uuid = TellstickSensor.createUUId(newEvent.getSensorId(), newEvent.getModel(), newEvent.getProtocol());
        Device device = getSensor(uuid);
        logger.debug("Sensor Event for {} event {}", device, newEvent);
        if (device == null) {
            TellstickSensor sensor = new TellstickSensor(newEvent.getSensorId(), newEvent.getProtocol(),
                    newEvent.getModel());
            sensor.setData(newEvent.getDataType(), newEvent.getData());
            sensorList.add(sensor);
            for (DeviceStatusListener listener : deviceStatusListeners) {
                listener.onDeviceAdded(getThing(), sensor);
            }
        } else {
            TellstickSensor useSensor = (TellstickSensor) device;
            String currentValue = useSensor.getData(newEvent.getDataType());
            Calendar compdate = Calendar.getInstance();
            compdate.add(Calendar.MINUTE, -1);
            logger.debug("Update curr {} new {}", currentValue, newEvent.getData());
            if (currentValue == null || !currentValue.equals(newEvent.getData())
                    || useSensor.getTimeStamp().before(compdate.getTime())) {
                // Changed or more than 1 minute since update
                useSensor.setData(newEvent.getDataType(), newEvent.getData());
                useSensor.setTimeStamp(new Date(newEvent.getTimestamp()));
                for (DeviceStatusListener listener : deviceStatusListeners) {
                    listener.onDeviceStateChanged(getThing(), useSensor, newEvent);
                }
            } else {
                logger.trace("Ignored update {}", newEvent);
            }
        }
    }

    @Override
    public void onRequest(TellstickDeviceEvent newEvent) {
        if (newEvent.getChangeType() == ChangeType.ADDED) {
            for (DeviceStatusListener listener : deviceStatusListeners) {
                listener.onDeviceAdded(getThing(), newEvent.getDevice());
            }
        } else if (newEvent.getChangeType() == ChangeType.REMOVED) {
            for (DeviceStatusListener listener : deviceStatusListeners) {
                listener.onDeviceRemoved(getThing(), newEvent.getDevice());
            }
        } else {
            for (DeviceStatusListener listener : deviceStatusListeners) {
                listener.onDeviceStateChanged(getThing(), newEvent.getDevice(), newEvent);
            }
        }
    }

    @Override
    public Device getSensor(String deviceUUId) {
        for (Device device : sensorList) {
            if (device.getUUId().equals(deviceUUId)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public TelldusDeviceController getController() {
        return this.deviceController;
    }
}
