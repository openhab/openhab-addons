/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.live;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tellstick.internal.conf.TelldusLiveConfiguration;
import org.openhab.binding.tellstick.internal.handler.DeviceStatusListener;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.handler.TelldusDevicesHandler;
import org.openhab.binding.tellstick.internal.live.dto.DataTypeValue;
import org.openhab.binding.tellstick.internal.live.dto.TellstickNetDevice;
import org.openhab.binding.tellstick.internal.live.dto.TellstickNetDevices;
import org.openhab.binding.tellstick.internal.live.dto.TellstickNetSensor;
import org.openhab.binding.tellstick.internal.live.dto.TellstickNetSensorEvent;
import org.openhab.binding.tellstick.internal.live.dto.TellstickNetSensors;
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
 * {@link TelldusLiveBridgeHandler} is the handler for Telldus Live service (Tellstick.NET and ZNET) and connects it
 * to the framework. All {@link TelldusDevicesHandler}s use the
 * {@link TelldusLiveDeviceController} to execute the actual commands.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@NonNullByDefault
public class TelldusLiveBridgeHandler extends BaseBridgeHandler implements TelldusBridgeHandler {

    private static final int REFRESH_DELAY = 10;

    private final Logger logger = LoggerFactory.getLogger(TelldusLiveBridgeHandler.class);

    private @Nullable TellstickNetDevices deviceList;
    private @Nullable TellstickNetSensors sensorList;
    private TelldusLiveDeviceController controller = new TelldusLiveDeviceController();
    private Set<DeviceStatusListener> deviceStatusListeners = ConcurrentHashMap.newKeySet();

    private int nbRefresh;
    private long sumRefreshDuration;
    private long minRefreshDuration = 1_000_000;
    private long maxRefreshDuration;

    public TelldusLiveBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> immediateRefreshJob;

    @Override
    public void dispose() {
        logger.debug("Live Handler disposed.");
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        TelldusDeviceController controller = this.controller;
        if (controller != null) {
            controller.dispose();
        }
        deviceList = null;
        sensorList = null;
        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TelldusLive bridge handler.");
        TelldusLiveConfiguration configuration = getConfigAs(TelldusLiveConfiguration.class);
        this.controller = new TelldusLiveDeviceController();
        this.controller.connectHttpClient(configuration.publicKey, configuration.privateKey, configuration.token,
                configuration.tokenSecret);
        updateStatus(ThingStatus.UNKNOWN);
        startAutomaticRefresh(configuration.refreshInterval);
    }

    private synchronized void startAutomaticRefresh(long refreshInterval) {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
        }
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::refreshDeviceList, 0, refreshInterval,
                TimeUnit.MILLISECONDS);
    }

    private void scheduleImmediateRefresh() {
        // We schedule in 10 sec, to avoid multiple updates
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null) {
            return;
        }
        logger.debug("Current remaining delay {}", pollingJob.getDelay(TimeUnit.SECONDS));
        if (pollingJob.getDelay(TimeUnit.SECONDS) > REFRESH_DELAY) {
            ScheduledFuture<?> immediateRefreshJob = this.immediateRefreshJob;
            if (immediateRefreshJob == null || immediateRefreshJob.isDone()) {
                this.immediateRefreshJob = scheduler.schedule(this::refreshDeviceList, REFRESH_DELAY, TimeUnit.SECONDS);
            }
        }
    }

    synchronized void refreshDeviceList() {
        Instant start = Instant.now();
        try {
            updateDevices(deviceList);
            updateSensors(sensorList);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.warn("Failed to update", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        monitorAdditionalRefresh(start, Instant.now());
    }

    private void monitorAdditionalRefresh(Instant start, Instant end) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        long duration = Duration.between(start, end).toMillis();
        sumRefreshDuration += duration;
        nbRefresh++;
        if (duration < minRefreshDuration) {
            minRefreshDuration = duration;
        }
        if (duration > maxRefreshDuration) {
            maxRefreshDuration = duration;
        }
        logger.debug(
                "{} refresh avg = {} ms min = {} max = {} (request avg = {} ms min = {} max = {}) ({} timeouts {} errors)",
                nbRefresh, nbRefresh == 0 ? 0 : sumRefreshDuration / nbRefresh, minRefreshDuration, maxRefreshDuration,
                controller.getAverageRequestDuration(), controller.getMinRequestDuration(),
                controller.getMaxRequestDuration(), controller.getNbTimeouts(), controller.getNbErrors());
    }

    private synchronized void updateDevices(@Nullable TellstickNetDevices previouslist) throws TellstickException {
        TellstickNetDevices newList = controller.callRestMethod(TelldusLiveDeviceController.HTTP_TELLDUS_DEVICES,
                TellstickNetDevices.class);
        if (newList.getDevices() != null) {
            logger.debug("Device list {}", newList.getDevices());
            if (previouslist == null) {
                logger.debug("updateDevices, Creating devices.");
                for (TellstickNetDevice device : newList.getDevices()) {
                    device.setUpdated(true);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceAdded(getThing(), device);
                    }
                }
                this.deviceList = newList;
            } else {
                logger.debug("updateDevices, Updating devices.");
                for (TellstickNetDevice device : newList.getDevices()) {
                    int index = previouslist.getDevices().indexOf(device);
                    logger.debug("Device:{} found at {}", device, index);
                    if (index >= 0) {
                        TellstickNetDevice orgDevice = previouslist.getDevices().get(index);
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

            for (TellstickNetDevice device : deviceList.getDevices()) {
                if (device.isUpdated()) {
                    logger.debug("Updated device:{}", device);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceStateChanged(getThing(), device,
                                new TellstickDeviceEvent(device, null, null, null, System.currentTimeMillis()));
                    }
                    device.setUpdated(false);
                }
            }
        } else {
            logger.debug("updateDevices, rest API returned null");
        }
    }

    private synchronized void updateSensors(@Nullable TellstickNetSensors previouslist) throws TellstickException {
        TellstickNetSensors newList = controller.callRestMethod(TelldusLiveDeviceController.HTTP_TELLDUS_SENSORS,
                TellstickNetSensors.class);
        logger.debug("Updated sensors:{}", newList.getSensors());
        if (newList.getSensors() != null) {
            if (previouslist == null) {
                logger.debug("First update of sensors");
                this.sensorList = newList;
                for (TellstickNetSensor sensor : sensorList.getSensors()) {
                    sensor.setUpdated(true);
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onDeviceAdded(getThing(), sensor);
                    }
                }
            } else {
                logger.debug("Update sensors, reset updated flag");
                for (TellstickNetSensor sensor : previouslist.getSensors()) {
                    sensor.setUpdated(false);
                }
                logger.debug("Update sensors, reset updated flag1");

                for (TellstickNetSensor sensor : newList.getSensors()) {
                    int index = this.sensorList.getSensors().indexOf(sensor);
                    if (index >= 0) {
                        TellstickNetSensor orgSensor = this.sensorList.getSensors().get(index);
                        logger.debug("Update sensor {}, prev update {}, new update {}", sensor.getId(),
                                orgSensor.getLastUpdated(), sensor.getLastUpdated());
                        if (sensor.getLastUpdated() > orgSensor.getLastUpdated()) {
                            logger.debug("Update for sensor:{}", sensor);
                            orgSensor.setData(sensor.getData());
                            orgSensor.setLastUpdated(sensor.getLastUpdated());
                            orgSensor.setUpdated(true);
                            sensor.setUpdated(true);
                        }
                    } else {
                        logger.debug("Adding sensor {}, new update {}", sensor.getId(), sensor.getLastUpdated());
                        this.sensorList.getSensors().add(sensor);
                        sensor.setUpdated(true);
                        for (DeviceStatusListener listener : deviceStatusListeners) {
                            listener.onDeviceAdded(getThing(), sensor);
                        }
                    }
                }
            }
            for (TellstickNetSensor sensor : sensorList.getSensors()) {
                if (sensor.getData() != null && sensor.isUpdated()) {
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        for (DataTypeValue type : sensor.getData()) {
                            listener.onDeviceStateChanged(getThing(), sensor,
                                    new TellstickNetSensorEvent(sensor.getId(), type.getValue(), type,
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
    public boolean registerDeviceStatusListener(@Nullable DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.add(deviceStatusListener);
    }

    @Override
    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    private @Nullable Device getDevice(String id, List<TellstickNetDevice> devices) {
        for (Device device : devices) {
            if (device.getId() == Integer.valueOf(id)) {
                return device;
            }
        }
        return null;
    }

    private @Nullable Device getSensor(String id, List<TellstickNetSensor> sensors) {
        for (Device sensor : sensors) {
            if (sensor.getId() == Integer.valueOf(id)) {
                return sensor;
            }
        }
        return null;
    }

    @Override
    public @Nullable Device getDevice(String serialNumber) {
        return getDevice(serialNumber, getDevices());
    }

    private List<TellstickNetDevice> getDevices() {
        if (deviceList == null) {
            refreshDeviceList();
        }
        return deviceList.getDevices();
    }

    @Override
    public @Nullable Device getSensor(String deviceUUId) {
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
    public @Nullable TelldusDeviceController getController() {
        return controller;
    }
}
