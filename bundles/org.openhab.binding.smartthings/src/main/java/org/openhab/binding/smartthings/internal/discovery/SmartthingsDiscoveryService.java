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
package org.openhab.binding.smartthings.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartthingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Smartthings Discovery service
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private @Nullable SmartthingsBridgeHandler smartthingsBridgeHandler;
    private @Nullable SmartthingsTypeRegistry typeRegistry;

    /*
     * default constructor
     */
    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
    }

    public void setSmartthingsTypeRegistry(SmartthingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        try {
            doScan(true);
        } catch (SmartthingsException ex) {
            logger.error("Error during device scan: {}", ex.toString());
        }
    }

    private Gson gson = new Gson();

    public void doTest(Boolean addDevice) {
        String json = "{\"deviceId\":\"da30a39d-4025-726e-eb90-8d1f5979c0e1\",\"name\":\"[washer] Samsung\",\"label\":\"Wasmachine\",\"manufacturerName\":\"Samsung Electronics\",\"presentationId\":\"DA-WM-WM-01011\",\"deviceManufacturerCode\":\"Samsung Electronics\",\"deviceTypeName\":\"Samsung OCF Washer\",\"locationId\":\"ddf09b33-5632-4393-ab1a-7f9a6162f3e1\",\"ownerId\":\"11e1f5f1-5539-b075-0940-bec107a09dcd\",\"roomId\":\"99a9935d-c52e-4562-b040-6b28d64f35de\",\"createTime\":\"2024-01-21T19:00:53.963Z\",\"type\":\"OCF\",\"restrictionTier\":0,\"executionContext\":\"CLOUD\",\"profile\":{\"id\":\"2853d731-d923-3e3c-b773-402904ae9830\"},\"components\":[{\"id\":\"main\",\"label\":\"main\",\"capabilities\":[{\"id\":\"execute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"ocf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"powerConsumptionReport\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"refresh\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"remoteControlStatus\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"demandResponseLoadControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"logTrigger\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"switch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"washerOperatingState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.disabledCapabilities\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.dryerDryLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.dryerWrinklePrevent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.energyType\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.jobBeginningStatus\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.supportedOptions\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerAutoDetergent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerAutoSoftener\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerRinseCycles\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerSoilLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerSpinLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.washerWaterTemperature\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.audioVolumeLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.autoDispenseDetergent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.autoDispenseSoftener\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.detergentOrder\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.detergentState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.deviceIdentification\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.driverVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerDryingTime\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.detergentAutoReplenishment\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softenerAutoReplenishment\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.flexibleAutoDispenseDetergent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareUpdate\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.kidsLock\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softenerOrder\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softenerState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerBubbleSoak\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerCycle\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerCyclePreset\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerDelayEnd\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerFreezePrevent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerLabelScanCyclePreset\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerOperatingState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerWashingTime\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerWaterLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.washerWaterValve\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.welcomeMessage\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.waterConsumptionReport\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.clothingExtraCare\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.quickControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.energyPlanner\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.maintenanceMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.diagnosticsInformation\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.wifiConfiguration\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.smartthingsHub\",\"version\":\"1\",\"ephemeral\":true}],\"categories\":[{\"name\":\"Washer\",\"categoryType\":\"manufacturer\"}]},{\"id\":\"hca.main\",\"label\":\"hca.main\",\"capabilities\":[{\"id\":\"hca.washerMode\",\"version\":\"1\",\"ephemeral\":false}],\"categories\":[{\"name\":\"Other\",\"categoryType\":\"manufacturer\"}]}],\"ocf\":{\"ocfDeviceType\":\"oic.d.washer\",\"name\":\"[washer] Samsung\",\"specVersion\":\"core.1.1.0\",\"verticalDomainSpecVersion\":\"res.1.1.0,sh.1.1.0\",\"manufacturerName\":\"Samsung Electronics\",\"modelNumber\":\"DA_WM_TP1_21_COMMON|20314341|20010002001611524AA3025700000000\",\"platformVersion\":\"DAWIT 2.0\",\"platformOS\":\"TizenRT 3.1\",\"hwVersion\":\"Realtek\",\"firmwareVersion\":\"DA_WM_TP1_21_COMMON_30250508\",\"vendorId\":\"DA-WM-WM-01011\",\"lastSignupTime\":\"2024-01-21T19:00:53.883404Z\",\"transferCandidate\":false,\"additionalAuthCodeRequired\":false}}";
        SmartthingsDevice device1 = null;

        device1 = gson.fromJson(json, SmartthingsDevice.class);
        registerDevice(Objects.requireNonNull(device1), addDevice);

        json = "{\"deviceId\":\"e3c91b4e-1404-bdd3-ce2f-6176234bb2fc\",\"name\":\"[dryer] Samsung\",\"label\":\"Droger\",\"manufacturerName\":\"Samsung Electronics\",\"presentationId\":\"DA-WM-WD-01011\",\"deviceManufacturerCode\":\"Samsung Electronics\",\"deviceTypeName\":\"Samsung OCF Dryer\",\"locationId\":\"ddf09b33-5632-4393-ab1a-7f9a6162f3e1\",\"ownerId\":\"11e1f5f1-5539-b075-0940-bec107a09dcd\",\"roomId\":\"99a9935d-c52e-4562-b040-6b28d64f35de\",\"createTime\":\"2025-07-05T17:34:00.478Z\",\"type\":\"OCF\",\"restrictionTier\":0,\"executionContext\":\"CLOUD\",\"profile\":{\"id\":\"d07b1f90-c449-3ad4-99a4-eb9673452e1f\"},\"components\":[{\"id\":\"main\",\"label\":\"main\",\"capabilities\":[{\"id\":\"ocf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"execute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"refresh\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"switch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"remoteControlStatus\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"dryerOperatingState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"powerConsumptionReport\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"demandResponseLoadControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"logTrigger\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.disabledCapabilities\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.dryerDryLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.dryerWrinklePrevent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.energyType\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.jobBeginningStatus\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.supportedOptions\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.audioVolumeLevel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareUpdate\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.detergentOrder\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.detergentState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.deviceIdentification\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.driverVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerAutoCycleLink\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerCycle\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerCyclePreset\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerDelayEnd\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerDryingTemperature\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerDryingTime\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerFreezePrevent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerLabelScanCyclePreset\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dryerOperatingState\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.kidsLock\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.welcomeMessage\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.clothingExtraCare\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.selfCheck\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.quickControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.maintenanceMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.diagnosticsInformation\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.wifiConfiguration\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.smartthingsHub\",\"version\":\"1\",\"ephemeral\":true}],\"categories\":[{\"name\":\"Dryer\",\"categoryType\":\"manufacturer\"}]},{\"id\":\"hca.main\",\"label\":\"hca.main\",\"capabilities\":[{\"id\":\"hca.dryerMode\",\"version\":\"1\",\"ephemeral\":false}],\"categories\":[{\"name\":\"Other\",\"categoryType\":\"manufacturer\"}]}],\"ocf\":{\"ocfDeviceType\":\"oic.d.dryer\",\"name\":\"[dryer] Samsung\",\"specVersion\":\"core.1.1.0\",\"verticalDomainSpecVersion\":\"res.1.1.0,sh.1.1.0\",\"manufacturerName\":\"Samsung Electronics\",\"modelNumber\":\"DA_WM_TP1_21_COMMON|80010141|30010102001911004AA3039F00820000\",\"platformVersion\":\"DAWIT 2.0\",\"platformOS\":\"TizenRT 3.1\",\"hwVersion\":\"Realtek\",\"firmwareVersion\":\"DA_WM_TP1_21_COMMON_30250508\",\"vendorId\":\"DA-WM-WD-01011\",\"lastSignupTime\":\"2025-07-05T17:34:00.420642305Z\",\"transferCandidate\":false,\"additionalAuthCodeRequired\":false}}";
        device1 = gson.fromJson(json, SmartthingsDevice.class);
        registerDevice(Objects.requireNonNull(device1), addDevice);
    }

    public void doScan(Boolean addDevice) throws SmartthingsException {
        SmartthingsBridgeHandler bridge = smartthingsBridgeHandler;
        if (bridge == null) {
            return;
        }
        logger.trace("Start Discovery");

        SmartthingsApi api = bridge.getSmartthingsApi();
        SmartthingsDevice[] devices = api.getAllDevices();

        doTest(addDevice);

        for (SmartthingsDevice device : devices) {
            registerDevice(device, addDevice);

        }

        logger.trace("End Discovery");
    }

    public void registerDevice(SmartthingsDevice device, Boolean addDevice) {
        String name = device.name;
        String label = device.label;

        logger.trace("Find Device : {} / {}", device.name, device.label);

        if (device.components == null || device.components.length == 0) {
            return;
        }

        Boolean enabled = false;
        if ("Four".equals(label)) {
            enabled = false;
        }
        if ("Petrole".equals(label)) {
            enabled = false;
        }
        if (label.contains("cuisson")) {
            enabled = false;
        }

        if (label.contains("Plug")) {
            enabled = true;
        }

        enabled = true;

        if (!enabled) {
            return;
        }

        String deviceType = null;
        for (SmartthingsComponent component : device.components) {
            String compId = component.id;

            if (component.categories != null && component.categories.length > 0) {
                for (SmartthingsCategory cat : component.categories) {
                    String catId = cat.name;

                    if (SmartthingsBindingConstants.GROUPD_ID_MAIN.equals(compId)) {
                        deviceType = catId;
                    }
                }
            }
        }

        if (deviceType == null) {
            logger.info("unknow device, bypass");
            return;
        }

        if ("white-and-color-ambiance".equals(name)) {
            return;
        }

        deviceType = deviceType.toLowerCase();
        if (this.typeRegistry != null) {
            this.typeRegistry.register(deviceType, device);
        }
        if (addDevice) {
            createDevice(deviceType, Objects.requireNonNull(device));
        }
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData Device data from the hub
     */
    private void createDevice(String deviceType, SmartthingsDevice device) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", deviceType, device.name);

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String name = device.label; // Note: this is necessary for null analysis to work
        if (name == null) {
            logger.info(
                    "Unexpectedly received data for a device with no name. Check the Smartthings hub devices and make sure every device has a name");
            return;
        }
        String deviceNameNoSpaces = name.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        if (smartthingsBridgeHandler != null) {
            ThingUID bridgeUid = smartthingsBridgeHandler.getThing().getUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", deviceType, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(SmartthingsBindingConstants.SMARTTHINGS_NAME, name);
            properties.put(SmartthingsBindingConstants.DEVICE_ID, device.deviceId);
            properties.put(SmartthingsBindingConstants.DEVICE_LABEL, device.label);
            properties.put(SmartthingsBindingConstants.DEVICE_NAME, device.name);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr))
                    .withProperties(properties).withRepresentationProperty("deviceId").withBridge(bridgeUid)
                    .withLabel(name).build();

            thingDiscovered(discoveryResult);
        }
    }

    /**
     * Stops a running scan.
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Starts background scanning for attached devices.
     */
    @Override
    protected void startBackgroundDiscovery() {
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartthingsBridgeHandler smartthingsBridgeHandler) {
            this.smartthingsBridgeHandler = smartthingsBridgeHandler;
            this.smartthingsBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return smartthingsBridgeHandler;
    }
}
