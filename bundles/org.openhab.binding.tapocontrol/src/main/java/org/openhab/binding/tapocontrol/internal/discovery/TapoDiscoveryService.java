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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudConnector;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeConfiguration;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoDeviceConfiguration;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResult;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResultList;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO Smart Home thing discovery
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(TapoDiscoveryService.class);
    private final String uid;
    protected @NonNullByDefault({}) TapoBridgeHandler bridge;
    protected @NonNullByDefault({}) TapoUdpDiscovery udpDiscovery;
    protected @NonNullByDefault({}) TapoCloudConnector cloudConnector;
    private @NonNullByDefault({}) TapoBridgeConfiguration config;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private TapoDiscoveryResultList discoveryResultList = new TapoDiscoveryResultList();

    /***********************************
     *
     * INITIALIZATION
     *
     ************************************/

    public TapoDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, TAPO_DISCOVERY_TIMEOUT_S, false);
        uid = "Discovery-Service";
    }

    @Override
    public void activate() {
        config = bridge.getBridgeConfig();
        if (config.cloudDiscovery || config.udpDiscovery) {
            startBackgroundDiscovery();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        stopScheduler(discoveryJob);
    }

    @Override
    public void startBackgroundDiscovery() {
        startDiscoveryScheduler();
    }

    @Override
    public void stopBackgroundDiscovery() {
        stopScheduler(discoveryJob);
    }

    /**
     * setThingHandler - set bridge and other handlers on initializing service
     */
    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TapoBridgeHandler bridgeHandler) {
            TapoBridgeHandler tapoBridge = bridgeHandler;
            tapoBridge.setDiscoveryService(this);
            bridge = tapoBridge;
            udpDiscovery = new TapoUdpDiscovery(bridge);
            cloudConnector = bridgeHandler.getCloudConnector();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridge;
    }

    /*************************
     * SCHEDULER
     *************************/
    /**
     * Start DeviceDiscovery Scheduler
     */
    protected void startDiscoveryScheduler() {
        config = bridge.getBridgeConfig();
        int pollingInterval = config.discoveryInterval;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        if ((config.cloudDiscovery || config.udpDiscovery) && pollingInterval > 0) {
            logger.debug("{} starting discoveryScheduler with interval {} {}", this.uid, pollingInterval, timeUnit);

            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, pollingInterval, timeUnit);
        } else {
            logger.debug("({}) discoveryScheduler disabled with config '0'", uid);
            stopScheduler(this.discoveryJob);
        }
    }

    /**
     * Stop scheduler
     * 
     * @param scheduler ScheduledFeature which should be stopped
     */
    protected void stopScheduler(@Nullable ScheduledFuture<?> scheduler) {
        if (scheduler != null) {
            scheduler.cancel(true);
            scheduler = null;
        }
    }

    /***********************************
     *
     * SCAN HANDLING
     *
     ************************************/

    /**
     * Start scan manually
     */
    @Override
    public void startScan() {
        logger.trace("{} starting scan", this.uid);
        removeOlderResults(getTimestampOfLastScan());
        discoveryResultList.clear();
        try {
            if (bridge != null) {
                bridge.getErrorHandler().reset();
                if (config.cloudDiscovery && bridge.loginCloud()) {
                    cloudConnector.getDeviceList();
                }
                if (config.udpDiscovery && udpDiscovery != null) {
                    udpDiscovery.startScan();
                }
                handleDiscoveryList(discoveryResultList);
            }
        } catch (Exception e) {
            logger.debug("({}) scan failed: ", uid, e);
        }
    }

    @Override
    public void abortScan() {
        logger.trace("{} scan aborted", this.uid);
    }

    @Override
    public void stopScan() {
        logger.trace("{} scan stoped", this.uid);
    }

    /***********************************
     *
     * handle Results
     *
     ************************************/

    /*
     * add scan results to discoveryResultList
     */
    public void addScanResults(TapoDiscoveryResultList deviceList) {
        if (discoveryResultList.size() == 0) {
            discoveryResultList = deviceList;
        } else {
            for (TapoDiscoveryResult result : deviceList) {
                addScanResult(result);
            }
        }
    }

    /**
     * add singleResult to list
     */
    public void addScanResult(TapoDiscoveryResult device) {
        discoveryResultList.addResult(device);
    }

    /**
     * work with result from get devices from deviceList
     * 
     * @param deviceList
     */
    protected void handleDiscoveryList(TapoDiscoveryResultList deviceList) {
        logger.trace("{} handle discovery result", this.uid);
        try {
            for (TapoDiscoveryResult deviceElement : deviceList) {
                if (!deviceElement.deviceMac().isBlank()) {
                    String deviceModel = getDeviceModel(deviceElement);
                    String deviceLabel = getDeviceLabel(deviceElement);
                    ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

                    /* create thing */
                    if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
                        if (!config.onlyLocalOnlineDevices || deviceElement.ip().length() > 1) {
                            DiscoveryResult discoveryResult = createResult(deviceElement);
                            thingDiscovered(discoveryResult);
                        } else {
                            logger.debug("{} device discovered but is offline '{}'", this.uid, deviceLabel);
                        }
                    } else {
                        logger.debug("{} unsupported device discovered '{}'", this.uid, deviceLabel);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("({}) error handle DiscoveryResult", uid, e);
        }
    }

    /**
     * CREATE DISCOVERY RESULT
     * creates discoveryResult (Thing) from JsonObject got from Cloud
     * 
     * @param device JsonObject with device information
     * @return DiscoveryResult-Object
     */
    private DiscoveryResult createResult(TapoDiscoveryResult device) {
        TapoBridgeHandler tapoBridge = this.bridge;
        String deviceModel = getDeviceModel(device);
        String label = getDeviceLabel(device);
        String deviceMAC = device.deviceMac();
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

        /* create properties */
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, DEVICE_VENDOR);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, formatMac(deviceMAC, MAC_DIVISION_CHAR));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.fwVer());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, device.deviceHwVer());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceModel);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.deviceId());
        if (device.ip().length() >= 7) {
            properties.put(TapoDeviceConfiguration.CONFIG_DEVICE_IP, device.ip());
            properties.put(TapoDeviceConfiguration.CONFIG_HTTP_PORT, device.encryptionShema().httpPort());
            properties.put(TapoDeviceConfiguration.CONFIG_PROTOCOL, device.encryptionShema().encryptType());
        }

        logger.debug("device {} discovered with mac {}", deviceModel, deviceMAC);
        if (tapoBridge != null) {
            ThingUID bridgeUID = tapoBridge.getUID();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceMAC);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_REPRESENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, deviceMAC);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_REPRESENTATION_PROPERTY).withLabel(label).build();
        }
    }
}
