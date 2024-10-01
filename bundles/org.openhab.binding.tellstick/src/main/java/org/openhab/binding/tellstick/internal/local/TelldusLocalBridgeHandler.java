/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tellstick.internal.conf.TelldusLocalConfiguration;
import org.openhab.binding.tellstick.internal.handler.DeviceStatusListener;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.handler.TelldusDevicesHandler;
import org.openhab.binding.tellstick.internal.local.dto.LocalDataTypeValueDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalDeviceDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalDevicesDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorEventDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalSensorsDTO;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.device.TellstickDeviceEvent;
import org.tellstick.device.TellstickException;
import org.tellstick.device.iface.Device;

/**
 * {@link TelldusLocalBridgeHandler} is the handler for Telldus Local API (Tellstick ZNET v1/v2) and connects it
 * to the framework. All {@link TelldusDevicesHandler}s use the
 * {@link TelldusLocalDeviceController} to execute the actual commands.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TelldusLocalBridgeHandler extends BaseBridgeHandler implements TelldusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TelldusLocalBridgeHandler.class);

    private TellstickLocalDevicesDTO deviceList = null;
    private TellstickLocalSensorsDTO sensorList = null;
    private TelldusLocalDeviceController controller = null;
    private Set<DeviceStatusListener> deviceStatusListeners = ConcurrentHashMap.newKeySet();
    private final HttpClient httpClient;
    private ScheduledFuture<?> pollingJob;
    /**
     * Use cache for refresh command to not update again when call is made within 10 seconds of previous call.
     */
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(10),
            this::refreshDeviceList);

    public TelldusLocalBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        TelldusLocalConfiguration configuration = getConfigAs(TelldusLocalConfiguration.class);
        this.controller = new TelldusLocalDeviceController(configuration, httpClient);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshDeviceList, 11, configuration.refreshInterval,
                TimeUnit.MILLISECONDS);
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (this.controller != null) {
            this.controller.dispose();
        }
        deviceList = null;
        sensorList = null;
        super.dispose();
    }

    private boolean refreshDeviceList() {
        try {
            updateDevices(deviceList);
            updateSensors(sensorList);
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (TellstickException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return false;
    }

    private synchronized void updateDevices(TellstickLocalDevicesDTO previouslist)
            throws TellstickException, InterruptedException {
        TellstickLocalDevicesDTO newList = controller
                .callRestMethod(TelldusLocalDeviceController.HTTP_LOCAL_API_DEVICES, TellstickLocalDevicesDTO.class);
        if (newList != null && newList.getDevices() != null) {
            logger.debug("Device list {}", newList.getDevices());
            if (previouslist == null) {
                for (TellstickLocalDeviceDTO device : newList.getDevices()) {
                    device.setUpdated(true);
                    synchronized (deviceStatusListeners) {
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceAdded(getThing(), device);
                        }
                    }
                }
                this.deviceList = newList;
            } else {
                for (TellstickLocalDeviceDTO device : newList.getDevices()) {
                    int index = previouslist.getDevices().indexOf(device);
                    logger.debug("Device:{} found at {}", device, index);
                    if (index >= 0) {
                        TellstickLocalDeviceDTO orgDevice = previouslist.getDevices().get(index);
                        if (device.getState() != orgDevice.getState()) {
                            orgDevice.setState(device.getState());
                            orgDevice.setStatevalue(device.getStatevalue());
                            orgDevice.setUpdated(true);
                        }
                    } else {
                        logger.debug("New Device - Adding:{}", device);
                        previouslist.getDevices().add(device);
                        device.setUpdated(true);
                        synchronized (deviceStatusListeners) {
                            for (DeviceStatusListener listener : deviceStatusListeners) {
                                listener.onDeviceAdded(getThing(), device);
                            }
                        }
                    }
                }
            }

            for (TellstickLocalDeviceDTO device : deviceList.getDevices()) {
                if (device.isUpdated()) {
                    synchronized (deviceStatusListeners) {
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceStateChanged(getThing(), device,
                                    new TellstickDeviceEvent(device, null, null, null, System.currentTimeMillis()));
                        }
                    }
                    device.setUpdated(false);
                }
            }
        } else {
            logger.debug("updateDevices, rest API returned null");
        }
    }

    private synchronized void updateSensors(TellstickLocalSensorsDTO previouslist)
            throws TellstickException, InterruptedException {
        TellstickLocalSensorsDTO newList = controller
                .callRestMethod(TelldusLocalDeviceController.HTTP_LOCAL_API_SENSORS, TellstickLocalSensorsDTO.class);
        if (newList != null && newList.getSensors() != null) {
            logger.debug("Updated sensors:{}", newList.getSensors());
            if (previouslist == null) {
                this.sensorList = newList;
                for (TellstickLocalSensorDTO sensor : sensorList.getSensors()) {
                    sensor.setUpdated(true);
                    synchronized (deviceStatusListeners) {
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceAdded(getThing(), sensor);
                        }
                    }
                }
            } else {
                for (TellstickLocalSensorDTO sensor : previouslist.getSensors()) {
                    sensor.setUpdated(false);
                }

                for (TellstickLocalSensorDTO sensor : newList.getSensors()) {
                    int index = this.sensorList.getSensors().indexOf(sensor);
                    if (index >= 0) {
                        TellstickLocalSensorDTO orgSensor = this.sensorList.getSensors().get(index);
                        orgSensor.setData(sensor.getData());
                        orgSensor.setUpdated(true);
                        sensor.setUpdated(true);
                    } else {
                        this.sensorList.getSensors().add(sensor);
                        sensor.setUpdated(true);
                        synchronized (deviceStatusListeners) {
                            for (DeviceStatusListener listener : deviceStatusListeners) {
                                listener.onDeviceAdded(getThing(), sensor);
                            }
                        }
                    }
                }
            }
            for (TellstickLocalSensorDTO sensor : sensorList.getSensors()) {
                if (sensor.getData() != null && sensor.isUpdated()) {
                    synchronized (deviceStatusListeners) {
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            for (LocalDataTypeValueDTO type : sensor.getData()) {
                                listener.onDeviceStateChanged(getThing(), sensor,
                                        new TellstickLocalSensorEventDTO(sensor.getId(), type.getValue(), type,
                                                sensor.getProtocol(), sensor.getModel(), System.currentTimeMillis()));
                            }
                        }
                    }
                    sensor.setUpdated(false);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshCache.getValue();
        }
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

    private Device getDevice(String id, List<TellstickLocalDeviceDTO> devices) {
        for (Device device : devices) {
            if (device.getId() == Integer.valueOf(id)) {
                return device;
            }
        }
        return null;
    }

    private Device getSensor(String id, List<TellstickLocalSensorDTO> sensors) {
        for (Device sensor : sensors) {
            if (sensor.getId() == Integer.valueOf(id)) {
                return sensor;
            }
        }
        return null;
    }

    @Override
    public Device getDevice(String serialNumber) {
        return getDevice(serialNumber, getDevices());
    }

    private List<TellstickLocalDeviceDTO> getDevices() {
        if (deviceList == null) {
            refreshDeviceList();
        }
        return deviceList.getDevices();
    }

    @Override
    public Device getSensor(String deviceUUId) {
        Device result = null;
        if (sensorList != null) {
            result = getSensor(deviceUUId, sensorList.getSensors());
        }
        return result;
    }

    @Override
    public void rescanTelldusDevices() {
        this.deviceList = null;
        this.sensorList = null;
        refreshDeviceList();
    }

    @Override
    public TelldusDeviceController getController() {
        return controller;
    }
}
