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
package org.openhab.binding.zway.internal.discovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.zway.internal.ZWayBindingConstants;
import org.openhab.binding.zway.internal.handler.ZWayBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.devices.types.Camera;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorMultiline;
import de.fh_zwickau.informatik.sensor.model.devices.types.Text;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;

/**
 * The {@link ZWayDeviceDiscoveryService} is responsible for device discovery.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SEARCH_TIME = 60;
    private static final int INITIAL_DELAY = 15;
    private static final int SCAN_INTERVAL = 240;

    private ZWayBridgeHandler mBridgeHandler;
    private ZWayDeviceScan mZWayDeviceScanningRunnable;
    private ScheduledFuture<?> mZWayDeviceScanningJob;

    public ZWayDeviceDiscoveryService(ZWayBridgeHandler bridgeHandler) {
        super(ZWayBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("Initializing ZWayBridgeDiscoveryService");
        mBridgeHandler = bridgeHandler;
        mZWayDeviceScanningRunnable = new ZWayDeviceScan();
        activate(null);
    }

    private void scan() {
        logger.debug("Starting scan on Z-Way Server {}", mBridgeHandler.getThing().getUID());

        // Z-Way bridge have to be ONLINE because configuration is needed
        if (mBridgeHandler == null || !mBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        LocationList locationList = mBridgeHandler.getZWayApi().getLocations();

        DeviceList deviceList = mBridgeHandler.getZWayApi().getDevices();
        if (deviceList != null) {
            Map<Integer, List<Device>> physicalDevices = deviceList.getDevicesGroupByNodeId();
            for (Map.Entry<Integer, List<Device>> entry : physicalDevices.entrySet()) {
                final Integer nodeId = entry.getKey();
                List<Device> devices = entry.getValue();

                final ThingUID bridgeUID = mBridgeHandler.getThing().getUID();

                String location = "";

                String deviceTypes = "";
                Integer index = 0;
                for (Device device : devices) {
                    if (index != 0 && index != devices.size()) {
                        deviceTypes += ", ";
                    }
                    deviceTypes += device.getDeviceType();
                    index++;

                    // Add location, assuming that each (virtual) device is assigned to the same room
                    if (locationList != null) {
                        // Add only the location if this differs from globalRoom (with id 0)
                        if (device.getLocation() != -1 && device.getLocation() != 0) {
                            try {
                                location = locationList.getLocationById(device.getLocation()).getTitle();
                            } catch (NullPointerException npe) {
                                location = "";
                            }
                        }
                    }
                }
                logger.debug("Z-Way device found with {} virtual devices - device types: {}", devices.size(),
                        deviceTypes);

                ZWaveDevice zwaveDevice = mBridgeHandler.getZWayApi().getZWaveDevice(nodeId);
                if (zwaveDevice != null) {
                    String givenName = "Device " + nodeId;
                    if (!zwaveDevice.getData().getGivenName().getValue().equals("")) {
                        givenName = zwaveDevice.getData().getGivenName().getValue();
                    } else if (!zwaveDevice.getData().getDeviceTypeString().getValue().equals("")) {
                        givenName += " - " + zwaveDevice.getData().getDeviceTypeString().getValue();
                    }
                    // Add additional information as properties
                    String vendorString = zwaveDevice.getData().getVendorString().getValue();
                    if (!zwaveDevice.getData().getVendorString().getValue().equals("")) {
                        givenName += " (" + vendorString + ")";
                    }
                    String manufacturerId = zwaveDevice.getData().getManufacturerId().getValue();
                    String deviceType = zwaveDevice.getData().getDeviceTypeString().getValue();
                    String zddxmlfile = zwaveDevice.getData().getZDDXMLFile().getValue();
                    String sdk = zwaveDevice.getData().getSDK().getValue();

                    ThingUID thingUID = new ThingUID(ZWayBindingConstants.THING_TYPE_DEVICE,
                            mBridgeHandler.getThing().getUID(), nodeId.toString());

                    /*
                     * Properties
                     * - Configuration: DEVICE_CONFIG_NODE_ID
                     * - System properties:
                     * --- PROPERTY_VENDOR
                     * --- other default properties not available
                     * - Custom properties:
                     * --- DEVICE_LOCATION
                     * --- DEVICE_MANUFACTURER_ID
                     * --- DEVICE_DEVICE_TYPE
                     * --- DEVICE_ZDDXMLFILE
                     * --- DEVICE_SDK
                     */
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(givenName)
                            .withBridge(bridgeUID).withProperty(ZWayBindingConstants.DEVICE_CONFIG_NODE_ID, nodeId)
                            .withProperty(Thing.PROPERTY_VENDOR, vendorString)
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_LOCATION, location)
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_MANUFACTURER_ID, manufacturerId)
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_DEVICE_TYPE, deviceType)
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_ZDDXMLFILE, zddxmlfile)
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_SDK, sdk).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.warn("Z-Wave device not loaded");
                }
            }

            for (Device device : deviceList.getDevices()) {
                if (device.getVisibility() && !device.getPermanentlyHidden()) {
                    if (ZWayBindingConstants.DISCOVERY_IGNORED_DEVICES.contains(device.getDeviceId().split("_")[0])) {
                        logger.debug("Skip device: {}", device.getMetrics().getTitle());
                        continue;
                    }

                    if (device instanceof SensorMultiline || device instanceof Camera || device instanceof Text) {
                        logger.debug("Skip device because the device type is not supported: {}",
                                device.getMetrics().getTitle());
                        continue;
                    }

                    ThingUID bridgeUID = mBridgeHandler.getThing().getUID();

                    String location = "";
                    // Add location, assuming that each (virtual) device is assigned to the same room
                    if (locationList != null) {
                        // Add only the location if this differs from globalRoom (with id 0)
                        if (device.getLocation() != -1 && device.getLocation() != 0) {
                            try {
                                location = locationList.getLocationById(device.getLocation()).getTitle();
                            } catch (NullPointerException npe) {
                                location = "";
                            }
                        }
                    }

                    logger.debug("Z-Way virtual device found with device type: {} - {} - {}", device.getDeviceType(),
                            device.getMetrics().getProbeTitle(), device.getNodeId());

                    ThingUID thingUID = new ThingUID(ZWayBindingConstants.THING_TYPE_VIRTUAL_DEVICE,
                            mBridgeHandler.getThing().getUID(), device.getDeviceId());
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withLabel(device.getMetrics().getTitle()).withBridge(bridgeUID)
                            .withProperty(ZWayBindingConstants.DEVICE_CONFIG_VIRTUAL_DEVICE_ID, device.getDeviceId())
                            .withProperty(ZWayBindingConstants.DEVICE_PROP_LOCATION, location).build();
                    thingDiscovered(discoveryResult);
                }
            }
        } else {
            logger.warn("Devices not loaded");
        }
    }

    @Override
    protected void startScan() {
        scan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (mZWayDeviceScanningJob == null || mZWayDeviceScanningJob.isCancelled()) {
            logger.debug("Starting background scanning job");
            mZWayDeviceScanningJob = scheduler.scheduleWithFixedDelay(mZWayDeviceScanningRunnable, INITIAL_DELAY,
                    SCAN_INTERVAL, TimeUnit.SECONDS);
        } else {
            logger.debug("Scanning job is allready active");
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (mZWayDeviceScanningJob != null && !mZWayDeviceScanningJob.isCancelled()) {
            mZWayDeviceScanningJob.cancel(false);
            mZWayDeviceScanningJob = null;
        }
    }

    public class ZWayDeviceScan implements Runnable {
        @Override
        public void run() {
            scan();
        }
    }
}
