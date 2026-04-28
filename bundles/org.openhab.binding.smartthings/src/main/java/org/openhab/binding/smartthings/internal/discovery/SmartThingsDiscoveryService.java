/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.binding.smartthings.internal.type.UidUtils;
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
 * SmartThings discovery service
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 30;

    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    private final Logger logger = LoggerFactory.getLogger(SmartThingsDiscoveryService.class);

    private @Nullable SmartThingsBridgeHandler smartThingsBridgeHandler;
    private @Nullable SmartThingsTypeRegistry typeRegistry;

    /*
     * default constructor
     */
    public SmartThingsDiscoveryService() {
        super(SmartThingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SEC);
    }

    public void setSmartThingsTypeRegistry(SmartThingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        try {
            doScan(true);
        } catch (SmartThingsException ex) {
            logger.error("Error during device scan: {}", ex.toString());
        }
    }

    public void doScan(Boolean addDevice) throws SmartThingsException {
        SmartThingsBridgeHandler bridge = smartThingsBridgeHandler;
        if (bridge == null) {
            return;
        }
        logger.trace("Start Discovery");

        SmartThingsApi api = bridge.getSmartThingsApi();
        if (api != null) {
            SmartThingsDevice[] devices = api.getAllDevices();

            for (SmartThingsDevice device : devices) {
                registerDevice(device, addDevice);

            }
        }

        registerDevice(
                "{\"deviceId\":\"69c8f08f-5250-45f9-8c30-ff80be1e0ffe\",\"name\":\"[TV] Samsung 7 Series (43)\",\"label\":\"Samsung TV\",\"manufacturerName\":\"Samsung Electronics\",\"presentationId\":\"VD-STV_2018_K\",\"deviceManufacturerCode\":\"Samsung Electronics\",\"deviceTypeName\":\"Samsung OCF TV\",\"locationId\":\"1fc347a7-2269-4a3d-99af-155bba810a5f\",\"ownerId\":\"4a5da4a0-1cdc-103c-4ac1-be6d7175cead\",\"roomId\":\"cf568f88-c453-4d86-a3d4-97d038f0d6aa\",\"createTime\":\"2020-01-03T20:24:48Z\",\"type\":\"OCF\",\"restrictionTier\":0,\"executionContext\":\"CLOUD\",\"profile\":{\"id\":\"d2c56b57-56fa-3f30-9bb5-ae86a5b53d1f\"},\"components\":[{\"id\":\"main\",\"label\":\"main\",\"capabilities\":[{\"id\":\"ocf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"switch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"audioVolume\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"audioMute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"tvChannel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaInputSource\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaPlayback\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaTrackControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.error\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.picturemode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.soundmode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.accessibility\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.launchapp\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.recording\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.tvsearch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.disabledCapabilities\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambient\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambientContent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambient18\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.mediaInputSource\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"refresh\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"execute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.firmwareVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.supportsPowerOnByOcf\",\"version\":\"1\",\"ephemeral\":false}],\"categories\":[{\"name\":\"Television\",\"categoryType\":\"manufacturer\"}]}],\"ocf\":{\"ocfDeviceType\":\"oic.d.tv\",\"name\":\"[TV] Samsung 7 Series (43)\",\"specVersion\":\"core.1.1.0\",\"verticalDomainSpecVersion\":\"res.1.1.0,sh.1.1.0\",\"manufacturerName\":\"Samsung Electronics\",\"modelNumber\":\"UE43RU7099UXZG\",\"platformVersion\":\"Tizen 5.0\",\"platformOS\":\"4.1.10\",\"hwVersion\":\"0-0\",\"firmwareVersion\":\"T-MSLDEUC-1360.9\",\"vendorId\":\"VD-STV_2018_K\",\"lastSignupTime\":\"2020-01-03T20:24:45.405Z\",\"transferCandidate\":false,\"additionalAuthCodeRequired\":false}}",
                addDevice);
        registerDevice(
                "{\"deviceId\":\"8ae00f5a-1b0f-50c2-3fda-b5c11c01af44\",\"name\":\"Samsung The Frame\",\"label\":\"Samsung The Frame\",\"manufacturerName\":\"Samsung Electronics\",\"presentationId\":\"VD-STV_2018_K\",\"deviceManufacturerCode\":\"Samsung Electronics\",\"deviceTypeName\":\"Samsung OCF TV\",\"locationId\":\"1fc347a7-2269-4a3d-99af-155bba810a5f\",\"ownerId\":\"4a5da4a0-1cdc-103c-4ac1-be6d7175cead\",\"roomId\":\"9320b5b3-54f6-471e-b468-acf3b9d6533f\",\"createTime\":\"2022-10-01T17:49:14.622Z\",\"type\":\"OCF\",\"restrictionTier\":0,\"executionContext\":\"CLOUD\",\"profile\":{\"id\":\"361d5322-a827-3e41-9269-ed5129830ad3\"},\"components\":[{\"id\":\"main\",\"label\":\"main\",\"capabilities\":[{\"id\":\"ocf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"switch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"audioVolume\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"audioMute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"tvChannel\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaInputSource\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaPlayback\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"mediaTrackControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.error\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.picturemode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.soundmode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.accessibility\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.launchapp\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.recording\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.tvsearch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.disabledCapabilities\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.remoteControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambient\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambientContent\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.ambient18\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.mediaInputSource\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.deviceCategory\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"refresh\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"execute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.firmwareVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungvd.supportsPowerOnByOcf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"sec.diagnosticsInformation\",\"version\":\"1\",\"ephemeral\":false}],\"categories\":[{\"name\":\"Television\",\"categoryType\":\"manufacturer\"}]}],\"ocf\":{\"ocfDeviceType\":\"oic.d.tv\",\"name\":\"Samsung The Frame\",\"specVersion\":\"core.1.1.0\",\"verticalDomainSpecVersion\":\"res.1.1.0,sh.1.1.0\",\"manufacturerName\":\"Samsung Electronics\",\"modelNumber\":\"GQ65LS03TAUXZG\",\"platformVersion\":\"5.5\",\"platformOS\":\"Tizen\",\"hwVersion\":\"\",\"firmwareVersion\":\"T-NKMDEUC-2700.6\",\"vendorId\":\"VD-STV_2018_K\",\"lastSignupTime\":\"2022-10-01T17:49:10.010857Z\",\"transferCandidate\":false,\"additionalAuthCodeRequired\":false}}",
                addDevice);
        registerDevice(
                "{\"deviceId\":\"11d06d49-c67a-839b-9d40-a9858a08d394\",\"name\":\"Samsung Room A/C\",\"label\":\"Raumklimaanlage\",\"manufacturerName\":\"Samsung Electronics\",\"presentationId\":\"DA-AC-RAC-000003\",\"deviceManufacturerCode\":\"Samsung Electronics\",\"deviceTypeName\":\"Samsung OCF Air Conditioner\",\"locationId\":\"1fc347a7-2269-4a3d-99af-155bba810a5f\",\"ownerId\":\"4a5da4a0-1cdc-103c-4ac1-be6d7175cead\",\"roomId\":\"2e6d55a6-c805-404f-a467-bc3c3a4a12ed\",\"createTime\":\"2021-07-09T07:14:11.397Z\",\"type\":\"OCF\",\"restrictionTier\":0,\"executionContext\":\"CLOUD\",\"profile\":{\"id\":\"bb4a6df4-6e0f-303a-ac35-445ea78a41fe\"},\"components\":[{\"id\":\"main\",\"label\":\"main\",\"capabilities\":[{\"id\":\"ocf\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"switch\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"airConditionerMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"airConditionerFanMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"fanOscillationMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"temperatureMeasurement\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"thermostatCoolingSetpoint\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"relativeHumidityMeasurement\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"airQualitySensor\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"odorSensor\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"dustSensor\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"veryFineDustSensor\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"audioVolume\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"remoteControlStatus\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"powerConsumptionReport\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"demandResponseLoadControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"refresh\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"execute\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.spiMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.thermostatSetpointControl\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.airConditionerOptionalMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.airConditionerTropicalNightMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.autoCleaningMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.deviceReportStateConfiguration\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.energyType\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.dustFilter\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.veryFineDustFilter\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.deodorFilter\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.electricHepaFilter\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.doNotDisturbMode\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.periodicSensing\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.airConditionerOdorController\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.ocfResourceVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"custom.disabledCapabilities\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.alwaysOnSensing\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.deviceIdentification\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.dustFilterAlarm\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.driverVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareUpdate\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.softwareVersion\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.selfCheck\",\"version\":\"1\",\"ephemeral\":false},{\"id\":\"samsungce.individualControlLock\",\"version\":\"1\",\"ephemeral\":false}],\"categories\":[{\"name\":\"AirConditioner\",\"categoryType\":\"manufacturer\"}]}],\"ocf\":{\"ocfDeviceType\":\"oic.d.airconditioner\",\"name\":\"Samsung Room A/C\",\"specVersion\":\"core.1.1.0\",\"verticalDomainSpecVersion\":\"res.1.1.0,sh.1.1.0\",\"manufacturerName\":\"Samsung Electronics\",\"modelNumber\":\"ARTIK051_PRAC_20K|10217841|60010519001411010200001000000000\",\"platformVersion\":\"DAWIT 2.0\",\"platformOS\":\"TizenRT 1.0 + IPv6\",\"hwVersion\":\"ARTIK051\",\"firmwareVersion\":\"ARTIK051_PRAC_20K_11230313\",\"vendorId\":\"DA-AC-RAC-000003\",\"lastSignupTime\":\"2021-07-09T07:14:03.997292Z\",\"transferCandidate\":false,\"additionalAuthCodeRequired\":false}}",
                addDevice);
        // registerDevice("", addDevice);

        logger.trace("End Discovery");
    }

    public void registerDevice(String devDesc, Boolean addDevice) {
        Gson gson = new Gson();
        SmartThingsDevice dev = gson.fromJson(devDesc, SmartThingsDevice.class);
        if (dev != null) {
            registerDevice(dev, addDevice);
        }
    }

    public void registerDevice(SmartThingsDevice device, Boolean addDevice) {
        logger.trace("Find Device : {} / {}", device.name, device.label);

        if (device.components == null || device.components.length == 0) {
            return;
        }

        String deviceCategory = null;
        for (SmartThingsComponent component : device.components) {
            String compId = component.id;

            if (component.categories != null && component.categories.length > 0) {
                for (SmartThingsCategory cat : component.categories) {
                    String catId = cat.name;

                    if (SmartThingsBindingConstants.GROUPD_ID_MAIN.equals(compId)) {
                        deviceCategory = catId;
                    }
                }
            }
        }

        if (deviceCategory == null || deviceCategory.isEmpty()) {
            logger.debug("unknow device, bypass");
            return;
        }

        deviceCategory = deviceCategory.toLowerCase(Locale.ROOT);
        deviceCategory = UidUtils.sanitizeId(deviceCategory);

        SmartThingsTypeRegistry registry = this.typeRegistry;
        if (registry != null) {
            registry.register(deviceCategory, device);
        }
        if (addDevice) {
            createDevice(deviceCategory, Objects.requireNonNull(device));
        }
    }

    /**
     * Create a device with the data from the SmartThings account
     *
     * @param deviceData Device data from the account
     */
    private void createDevice(String deviceCategory, SmartThingsDevice device) {
        logger.trace("Discovery: Creating device: ThingType {} with name {}", deviceCategory, device.name);

        // Build the UID as a string "smartthings:{ThingType}:{BridgeName}:{DeviceName}"
        String label = device.label; // Note: this is necessary for null analysis to work
        if (label == null) {
            logger.warn(
                    "Unexpectedly received data for a device with no label. Check the SmartThings account and make sure every device has a name.");
            return;
        }
        String deviceNameNoSpaces = label.replaceAll("\\s", "_");
        String smartthingsDeviceName = findIllegalChars.matcher(deviceNameNoSpaces).replaceAll("");
        SmartThingsBridgeHandler bridgeHandler = smartThingsBridgeHandler;
        if (bridgeHandler != null) {
            ThingUID bridgeUid = bridgeHandler.getThing().getUID();
            String bridgeId = bridgeUid.getId();
            String uidStr = String.format("smartthings:%s:%s:%s", deviceCategory, bridgeId, smartthingsDeviceName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(SmartThingsBindingConstants.DEVICE_ID, device.deviceId);
            properties.put(SmartThingsBindingConstants.DEVICE_NAME, device.name);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr))
                    .withProperties(properties).withRepresentationProperty(SmartThingsBindingConstants.DEVICE_ID)
                    .withBridge(bridgeUid).withLabel(label).build();

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

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartThingsBridgeHandler smartthingsBridgeHandler) {
            this.smartThingsBridgeHandler = smartthingsBridgeHandler;
            smartthingsBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return smartThingsBridgeHandler;
    }
}
