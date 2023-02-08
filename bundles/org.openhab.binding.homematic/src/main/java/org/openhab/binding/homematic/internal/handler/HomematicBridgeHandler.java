/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.handler;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.CHANNEL_TYPE_DUTY_CYCLE_RATIO;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.communicator.HomematicGatewayAdapter;
import org.openhab.binding.homematic.internal.communicator.HomematicGatewayFactory;
import org.openhab.binding.homematic.internal.discovery.HomematicDeviceDiscoveryService;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.binding.homematic.internal.type.UidUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomematicBridgeHandler} is the handler for a Homematic gateway and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicBridgeHandler extends BaseBridgeHandler implements HomematicGatewayAdapter {
    private final Logger logger = LoggerFactory.getLogger(HomematicBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    private static final int DUTY_CYCLE_RATIO_LIMIT = 99;
    private static final int DUTY_CYCLE_DISCONNECTED = -1;
    private static SimplePortPool portPool = new SimplePortPool();

    private final Object dutyCycleRatioUpdateLock = new Object();
    private final Object initDisposeLock = new Object();

    private Future<?> initializeFuture;
    private boolean isDisposed;

    private HomematicConfig config;
    private HomematicGateway gateway;
    private final HomematicTypeGenerator typeGenerator;
    private final HttpClient httpClient;

    private HomematicDeviceDiscoveryService discoveryService;

    private final String ipv4Address;
    private boolean isInDutyCycle = false;
    private int dutyCycleRatio = 0;

    public HomematicBridgeHandler(@NonNull Bridge bridge, HomematicTypeGenerator typeGenerator, String ipv4Address,
            HttpClient httpClient) {
        super(bridge);
        this.typeGenerator = typeGenerator;
        this.ipv4Address = ipv4Address;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        synchronized (initDisposeLock) {
            isDisposed = false;
            initializeFuture = scheduler.submit(this::initializeInternal);
        }
    }

    public void setDiscoveryService(HomematicDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private void initializeInternal() {
        synchronized (initDisposeLock) {
            config = createHomematicConfig();

            try {
                String id = getThing().getUID().getId();
                gateway = HomematicGatewayFactory.createGateway(id, config, this, httpClient);
                configureThingProperties();
                gateway.initialize();

                // scan for already known devices (new devices will not be discovered,
                // since installMode==true is only achieved if the bridge is online
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();

                updateStatus(ThingStatus.ONLINE);
                if (!config.getGatewayInfo().isHomegear()) {
                    try {
                        gateway.loadRssiValues();
                    } catch (IOException ex) {
                        logger.warn("Unable to load RSSI values from bridge '{}'", getThing().getUID().getId());
                        logger.error("{}", ex.getMessage(), ex);
                    }
                }
                gateway.startWatchdogs();
            } catch (IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                logger.debug(
                        "Homematic bridge was set to OFFLINE-COMMUNICATION_ERROR due to the following exception: {}",
                        ex.getMessage(), ex);
                disposeInternal();
                scheduleReinitialize();
            }
        }
    }

    private void configureThingProperties() {
        final HmGatewayInfo info = config.getGatewayInfo();
        final Map<String, String> properties = getThing().getProperties();

        if (!properties.containsKey(PROPERTY_FIRMWARE_VERSION)) {
            getThing().setProperty(PROPERTY_FIRMWARE_VERSION, info.getFirmware());
        }
        if (!properties.containsKey(PROPERTY_SERIAL_NUMBER)) {
            getThing().setProperty(PROPERTY_SERIAL_NUMBER, info.getAddress());
        }
        if (!properties.containsKey(PROPERTY_MODEL_ID)) {
            getThing().setProperty(PROPERTY_MODEL_ID, info.getType());
        }
    }

    /**
     * Schedules a reinitialization, if the Homematic gateway is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        if (!isDisposed) {
            initializeFuture = scheduler.schedule(this::initializeInternal, REINITIALIZE_DELAY_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        synchronized (initDisposeLock) {
            super.dispose();

            if (initializeFuture != null) {
                initializeFuture.cancel(true);
            }

            disposeInternal();
            isDisposed = true;
        }
    }

    private void disposeInternal() {
        logger.debug("Disposing bridge '{}'", getThing().getUID().getId());
        if (discoveryService != null) {
            discoveryService.stopScan();
        }
        if (gateway != null) {
            gateway.dispose();
        }
        if (config != null) {
            portPool.release(config.getXmlCallbackPort());
            portPool.release(config.getBinCallbackPort());
        }
    }

    /**
     * Sets the OFFLINE status for all things of this bridge that has been removed from the gateway.
     */
    @SuppressWarnings("null")
    public void setOfflineStatus() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
            } catch (HomematicClientException e) {
                if (hmThing.getHandler() != null) {
                    ((HomematicThingHandler) hmThing.getHandler()).handleRemoval();
                }
            }
        }
    }

    /**
     * Creates the configuration for the HomematicGateway.
     */
    private HomematicConfig createHomematicConfig() {
        HomematicConfig homematicConfig = getThing().getConfiguration().as(HomematicConfig.class);
        if (homematicConfig.getCallbackHost() == null) {
            homematicConfig.setCallbackHost(this.ipv4Address);
        }
        if (homematicConfig.getXmlCallbackPort() == 0) {
            homematicConfig.setXmlCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getXmlCallbackPort());
        }
        if (homematicConfig.getBinCallbackPort() == 0) {
            homematicConfig.setBinCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getBinCallbackPort());
        }
        logger.debug("{}", homematicConfig);
        return homematicConfig;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HomematicDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            reloadAllDeviceValues();
        }
    }

    /**
     * Returns the TypeGenerator.
     */
    public HomematicTypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    /**
     * Returns the HomematicGateway.
     */
    public HomematicGateway getGateway() {
        return gateway;
    }

    /**
     * Updates the thing for the given Homematic device.
     */
    private void updateThing(HmDevice device) {
        Thing hmThing = getThing().getThing(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                thingHandler.thingUpdated(hmThing);
                for (Channel channel : hmThing.getChannels()) {
                    thingHandler.handleRefresh(channel.getUID());
                }
            }
        }
    }

    @Override
    public void onStateUpdated(HmDatapoint dp) {
        Thing hmThing = getThing().getThing(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null) {
            final ThingStatus status = hmThing.getStatus();
            if (status == ThingStatus.ONLINE || status == ThingStatus.OFFLINE) {
                HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
                if (thingHandler != null) {
                    thingHandler.updateDatapointState(dp);
                }
            }
        }
    }

    @Override
    public HmDatapointConfig getDatapointConfig(HmDatapoint dp) {
        Thing hmThing = getThing().getThing(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                return thingHandler.getChannelConfig(dp);
            }
        }
        return new HmDatapointConfig();
    }

    @Override
    public void onNewDevice(HmDevice device) {
        onDeviceLoaded(device);
        updateThing(device);
    }

    @SuppressWarnings("null")
    @Override
    public void onDeviceDeleted(HmDevice device) {
        discoveryService.deviceRemoved(device);
        updateThing(device);

        Thing hmThing = getThing().getThing(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null && hmThing.getHandler() != null) {
            ((HomematicThingHandler) hmThing.getHandler()).deviceRemoved();
        }
    }

    @Override
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    }

    @Override
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
        reloadAllDeviceValues();
    }

    @Override
    public void onDeviceLoaded(HmDevice device) {
        typeGenerator.generate(device);
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }

        Thing hmThing = getThing().getThing(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                thingHandler.deviceLoaded(device);
            }
        }
    }

    @Override
    public void onDutyCycleRatioUpdate(int dutyCycleRatio) {
        synchronized (dutyCycleRatioUpdateLock) {
            this.dutyCycleRatio = dutyCycleRatio;
            Channel dutyCycleRatioChannel = thing.getChannel(CHANNEL_TYPE_DUTY_CYCLE_RATIO);
            if (dutyCycleRatioChannel != null) {
                this.updateState(dutyCycleRatioChannel.getUID(),
                        new DecimalType(dutyCycleRatio < 0 ? 0 : dutyCycleRatio));
            }

            if (!isInDutyCycle) {
                if (dutyCycleRatio >= DUTY_CYCLE_RATIO_LIMIT) {
                    logger.info("Duty cycle threshold exceeded by homematic bridge {}, it will go OFFLINE.",
                            thing.getUID());
                    isInDutyCycle = true;
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
                } else if (dutyCycleRatio == DUTY_CYCLE_DISCONNECTED) {
                    logger.info(
                            "Duty cycle indicates a communication problem by homematic bridge {}, it will go OFFLINE.",
                            thing.getUID());
                    isInDutyCycle = true;
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } else {
                if (dutyCycleRatio < DUTY_CYCLE_RATIO_LIMIT && dutyCycleRatio != DUTY_CYCLE_DISCONNECTED) {
                    logger.info("Homematic bridge {}: duty cycle back to normal and bridge will come ONLINE again.",
                            thing.getUID());
                    isInDutyCycle = false;
                    this.updateStatus(ThingStatus.ONLINE);
                }
            }
        }
    }

    /**
     * Returns the last value for the duty cycle ratio that was retrieved from the homematic gateway.
     *
     * @return The duty cycle ratio of the gateway
     */
    public int getDutyCycleRatio() {
        return dutyCycleRatio;
    }

    @Override
    public void reloadDeviceValues(HmDevice device) {
        updateThing(device);
        if (device.isGatewayExtras()) {
            typeGenerator.generate(device);
        }
    }

    @Override
    public void reloadAllDeviceValues() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
                gateway.triggerDeviceValuesReload(device);
            } catch (HomematicClientException ex) {
                logger.warn("{}", ex.getMessage());
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (((HomematicThingHandler) childHandler).isDeletionPending()) {
            deleteFromGateway(UidUtils.getHomematicAddress(childThing), false, true, false);
        }
    }

    /**
     * Updates the {@link HmDatapoint} by reloading the value from the homematic gateway.
     *
     * @param dp The HmDatapoint that shall be updated
     * @throws IOException If there is a problem while communicating to the gateway
     */
    public void updateDatapoint(HmDatapoint dp) throws IOException {
        getGateway().loadDatapointValue(dp);
    }

    /**
     * Deletes a device from the gateway.
     *
     * @param address The address of the device to be deleted
     * @param reset <i>true</i> will perform a factory reset on the device before deleting it.
     * @param force <i>true</i> will delete the device even if it is not reachable.
     * @param defer <i>true</i> will delete the device once it becomes available.
     */
    public void deleteFromGateway(String address, boolean reset, boolean force, boolean defer) {
        scheduler.submit(() -> {
            logger.debug("Deleting the device '{}' from gateway '{}'", address, getBridge());
            getGateway().deleteDevice(address, reset, force, defer);
        });
    }
}
