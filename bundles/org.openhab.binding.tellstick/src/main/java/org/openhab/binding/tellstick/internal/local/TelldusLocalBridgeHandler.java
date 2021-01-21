/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tellstick.internal.conf.TelldusLocalConfiguration;
import org.openhab.binding.tellstick.internal.handler.DeviceStatusListener;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.handler.TelldusDevicesHandler;
import org.openhab.binding.tellstick.internal.local.json.LocalDataTypeValue;
import org.openhab.binding.tellstick.internal.local.json.TellstickLocalDevice;
import org.openhab.binding.tellstick.internal.local.json.TellstickLocalDevices;
import org.openhab.binding.tellstick.internal.local.json.TellstickLocalSensor;
import org.openhab.binding.tellstick.internal.local.json.TellstickLocalSensorEvent;
import org.openhab.binding.tellstick.internal.local.json.TellstickLocalSensors;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
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
 * @author Jan Gustafsson- Initial contribution
 */
public class TelldusLocalBridgeHandler extends BaseBridgeHandler implements TelldusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TelldusLocalBridgeHandler.class);

    private TellstickLocalDevices deviceList = null;
    private TellstickLocalSensors sensorList = null;
    private TelldusLocalDeviceController controller = null;
    private List<DeviceStatusListener> deviceStatusListeners = new Vector<>();
    private final HttpClient httpClient;

    private static final int REFRESH_DELAY = 10;

    public TelldusLocalBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> immediateRefreshJob;

    @Override
    public void dispose() {
        logger.debug("Local Handler disposed.");
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

    @Override
    public void initialize() {
        logger.debug("Initializing Telldus Local bridge handler.");
        TelldusLocalConfiguration configuration = getConfigAs(TelldusLocalConfiguration.class);
        this.controller = new TelldusLocalDeviceController(configuration, httpClient);
        startAutomaticRefresh(configuration.refreshInterval);
        refreshDeviceList();
        updateStatus(ThingStatus.ONLINE);
    }

    private synchronized void startAutomaticRefresh(long refreshInterval) {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
        }
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshDeviceList, 0, refreshInterval,
                TimeUnit.MILLISECONDS);
    }

    private void scheduleImmediateRefresh() {
        // We schedule in 10 sec, to avoid multiple updates
        logger.debug("Current remaining delay {}", pollingJob.getDelay(TimeUnit.SECONDS));
        if (pollingJob.getDelay(TimeUnit.SECONDS) > REFRESH_DELAY) {
            if (immediateRefreshJob == null || immediateRefreshJob.isDone()) {
                immediateRefreshJob = scheduler.schedule(this::refreshDeviceList, REFRESH_DELAY, TimeUnit.SECONDS);
            }
        }
    }

    synchronized void refreshDeviceList() {
        try {
            updateDevices(deviceList);
            updateSensors(sensorList);
            updateStatus(ThingStatus.ONLINE);
        } catch (TellstickException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.error("Failed to update", e);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.error("Failed to update", e);
        }
    }

    private synchronized void updateDevices(TellstickLocalDevices previouslist) throws TellstickException {
        TellstickLocalDevices newList = controller.callRestMethod(TelldusLocalDeviceController.HTTP_LOCAL_API_DEVICES,
                TellstickLocalDevices.class);
        logger.debug("Device list {}", newList.getDevices());
        if (newList.getDevices() != null) {
            if (previouslist == null) {
                logger.debug("updateDevices, Creating devices.");
                for (TellstickLocalDevice device : newList.getDevices()) {
                    device.setUpdated(true);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceAdded(getThing(), device);
                    }
                }
                this.deviceList = newList;
            } else {
                logger.debug("updateDevices, Updating devices.");
                for (TellstickLocalDevice device : newList.getDevices()) {
                    int index = previouslist.getDevices().indexOf(device);
                    logger.debug("Device:{} found at {}", device, index);
                    if (index >= 0) {
                        TellstickLocalDevice orgDevice = previouslist.getDevices().get(index);
                        if (device.getState() != orgDevice.getState()) {
                            orgDevice.setState(device.getState());
                            orgDevice.setStatevalue(device.getStatevalue());
                            orgDevice.setUpdated(true);
                        }
                    } else {
                        logger.debug("New Device - Adding:{}", device);
                        previouslist.getDevices().add(device);
                        device.setUpdated(true);
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceAdded(getThing(), device);
                        }
                    }
                }
            }

            for (TellstickLocalDevice device : deviceList.getDevices()) {
                if (device.isUpdated()) {
                    logger.debug("Updated device:{}", device);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceStateChanged(getThing(), device,
                                new TellstickDeviceEvent(device, null, null, null, System.currentTimeMillis()));
                    }
                    device.setUpdated(false);
                }
            }
        }
    }

    private synchronized void updateSensors(TellstickLocalSensors previouslist) throws TellstickException {
        TellstickLocalSensors newList = controller.callRestMethod(TelldusLocalDeviceController.HTTP_LOCAL_API_SENSORS,
                TellstickLocalSensors.class);
        logger.debug("Updated sensors:{}", newList.getSensors());
        if (newList.getSensors() != null) {
            if (previouslist == null) {
                logger.debug("First update of sensors");
                this.sensorList = newList;
                for (TellstickLocalSensor sensor : sensorList.getSensors()) {
                    sensor.setUpdated(true);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceAdded(getThing(), sensor);
                    }
                }
            } else {
                logger.debug("Update sensors, reset updated flag");
                for (TellstickLocalSensor sensor : previouslist.getSensors()) {
                    sensor.setUpdated(false);
                }
                logger.debug("Update sensors, reset updated flag1");

                for (TellstickLocalSensor sensor : newList.getSensors()) {
                    int index = this.sensorList.getSensors().indexOf(sensor);
                    if (index >= 0) {
                        TellstickLocalSensor orgSensor = this.sensorList.getSensors().get(index);
                        logger.debug("Update for sensor:{}", sensor);
                        orgSensor.setData(sensor.getData());
                        orgSensor.setUpdated(true);
                        sensor.setUpdated(true);
                    } else {
                        logger.debug("Adding sensor {}", sensor.getId());
                        this.sensorList.getSensors().add(sensor);
                        sensor.setUpdated(true);
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceAdded(getThing(), sensor);
                        }
                    }
                }
            }
            for (TellstickLocalSensor sensor : sensorList.getSensors()) {
                if (sensor.getData() != null && sensor.isUpdated()) {
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        for (LocalDataTypeValue type : sensor.getData()) {
                            listener.onDeviceStateChanged(getThing(), sensor,
                                    new TellstickLocalSensorEvent(sensor.getId(), type.getValue(), type,
                                            sensor.getProtocol(), sensor.getModel(), System.currentTimeMillis()));
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
            scheduleImmediateRefresh();
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
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

    private Device getDevice(String id, List<TellstickLocalDevice> devices) {
        for (Device device : devices) {
            if (device.getId() == Integer.valueOf(id)) {
                return device;
            }
        }
        return null;
    }

    private Device getSensor(String id, List<TellstickLocalSensor> sensors) {
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

    private List<TellstickLocalDevice> getDevices() {
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
