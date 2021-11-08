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
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home thing discovery
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(TapoDiscoveryService.class);
    protected @Nullable ScheduledFuture<?> scanJob;
    protected final TapoBridgeHandler bridge;

    /**
     * INIT CLASS
     * 
     * @param accountHandler
     */
    public TapoDiscoveryService(TapoBridgeHandler accountHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, TAPO_DISCOVERY_TIMEOUT_MS, false);
        logger.debug("Initializing TapoDiscoveryService");
        this.bridge = accountHandler;
    }

    public void activate() {
        activate(new HashMap<>());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * START SCAN MANUALLY
     */
    @Override
    public void startScan() {
        logger.debug("startScan");
        removeOlderResults(getTimestampOfLastScan());
        try {
            JsonArray jsonArray = bridge.getDeviceList();
            handleCloudDevices(jsonArray);
            logger.debug("scan finished");
        } catch (Exception e) {
            logger.error("Error scanning for devices", e);
        }
    }

    /**
     * STOP SCAN
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        logger.debug("stopScan");
    }

    /**
     * ABORT SCAN
     */
    @Override
    public synchronized void abortScan() {
        super.abortScan();
        logger.debug("abortScan");
    }

    /**
     * START DEVICE SCAN DELAYED
     */
    public void startDelayedScan() {
        Long delay = 2000L;
        scanJob = scheduler.scheduleWithFixedDelay(this::startScanOnce, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * SCAN ONCE
     * scan an disable scheduler
     */
    protected void startScanOnce() {
        this.startScan();
        scanJob.cancel(false);
        scanJob = null;
    }

    /**
     * CREATE DISCOVERY RESULT
     * creates discoveryResult (Thing) from JsonObject got from Cloud
     * 
     * @param device JsonObject with device information
     * @return DiscoveryResult-Object
     */
    public DiscoveryResult createResult(JsonObject device) {
        try {
            String deviceModel = getDeviceModel(device);
            String label = getDeviceLabel(device);
            String deviceMAC = device.get(CLOUD_PROPERTY_MAC).getAsString();
            ThingUID bridgeUID = bridge.getThing().getUID();
            ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceMAC);

            /* create properties */
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(Thing.PROPERTY_MAC_ADDRESS, deviceMAC);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.get(CLOUD_PROPERTY_FW).getAsString());
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, device.get(CLOUD_PROPERTY_HW).getAsString());
            properties.put(Thing.PROPERTY_MODEL_ID, deviceModel);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.get(CLOUD_PROPERTY_ID).getAsString());

            logger.debug("device {} discovered", deviceModel);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_REPRASENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
            return discoveryResult;
        } catch (Exception e) {
            logger.error("error creating discoveryResult", e);
            return DiscoveryResultBuilder.create(new ThingUID(BINDING_ID, "")).build();
        }
    }

    /**
     * work with result from get devices from cloud devices
     * 
     * @param deviceList
     */
    protected void handleCloudDevices(JsonArray deviceList) {
        try {
            for (JsonElement deviceElement : deviceList) {
                if (deviceElement.isJsonObject()) {
                    JsonObject device = deviceElement.getAsJsonObject();
                    String deviceModel = getDeviceModel(device);
                    ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

                    /* create thing */
                    if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
                        DiscoveryResult discoveryResult = createResult(device);
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("error handlling CloudDevices", e);
        }
    }

    /**
     * GET DEVICEMODEL
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceModel
     */
    protected String getDeviceModel(JsonObject device) {
        try {
            String deviceModel = device.get(CLOUD_PROPERTY_MODEL).getAsString();
            deviceModel = deviceModel.replace(" ", "_");
            return deviceModel;
        } catch (Exception e) {
            logger.error("error getDeviceModel", e);
            return "";
        }
    }

    /**
     * GET DEVICE LABEL
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceLabel
     */
    protected String getDeviceLabel(JsonObject device) {
        try {
            String label = device.get(CLOUD_PROPERTY_NAME).getAsString();
            String deviceLabel = "";
            String deviceModel = getDeviceModel(device);
            ThingTypeUID deviceUID = new ThingTypeUID(BINDING_ID, deviceModel);

            if (SUPPORTED_SMART_PLUG_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SMART_PLUG;
            } else if (SUPPORTED_WHITE_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_WHITE_BULB;
            } else if (SUPPORTED_COLOR_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_COLOR_BULB;
            }
            return label + " " + deviceLabel;
        } catch (Exception e) {
            logger.error("error getDeviceLabel", e);
            return "";
        }
    }

    /*
     * private static final String ARP_GET_IP_HW = "arp -a";
     * 
     * public String getARPTable(String cmd) throws IOException {
     * Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
     * return s.hasNext() ? s.next() : "";
     * }
     * 
     * System.out.println(getARPTable(ARP_GET_IP_HW ));
     */
}
