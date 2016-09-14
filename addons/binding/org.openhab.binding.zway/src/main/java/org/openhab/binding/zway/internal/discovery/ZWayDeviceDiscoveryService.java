/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.internal.discovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zway.ZWayBindingConstants;
import org.openhab.binding.zway.handler.ZWayBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.devices.zwaveapi.ZWaveDevice;

/**
 * The {@link ZWayDeviceDiscoveryService} is responsible for device discovery.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static int SEARCH_TIME = 60;
    private final static int INITIAL_DELAY = 15;
    private final static int SCAN_INTERVAL = 240;

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

        DeviceList deviceList = mBridgeHandler.getZWayApi().getDevices();
        if (deviceList != null) {
            Map<Integer, List<Device>> physicalDevices = deviceList.getDevicesGroupByNodeId();
            for (Map.Entry<Integer, List<Device>> entry : physicalDevices.entrySet()) {
                final Integer nodeId = entry.getKey();
                List<Device> devices = entry.getValue();

                final ThingUID bridgeUID = mBridgeHandler.getThing().getUID();

                String deviceTypes = "";
                Integer index = 0;
                for (Device device : devices) {
                    if (index != 0 && index != devices.size()) {
                        deviceTypes += ", ";
                    }
                    deviceTypes += device.getDeviceType();
                    index++;
                }
                logger.debug("Z-Way device found with {} virtual devices - device types: {}", devices.size(),
                        deviceTypes);

                ZWaveDevice zwaveDevice = mBridgeHandler.getZWayApi().getZWaveDevice(nodeId);
                if (zwaveDevice != null) {
                    String givenName = "Device " + nodeId;
                    if (!zwaveDevice.getGivenName().getValue().equals("")) {
                        givenName = zwaveDevice.getGivenName().getValue();
                    }

                    ThingUID thingUID = new ThingUID(ZWayBindingConstants.THING_TYPE_DEVICE, nodeId.toString());
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(givenName)
                            .withBridge(bridgeUID).withProperty(ZWayBindingConstants.DEVICE_CONFIG_NODE_ID, nodeId)
                            .build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.warn("Z-Wave device not loaded");
                }
            }

            for (Device device : deviceList.getDevices()) {
                if (device.getVisibility() && !device.getPermanentlyHidden()) {
                    ThingUID bridgeUID = mBridgeHandler.getThing().getUID();

                    logger.debug("Z-Way virtual device found with device type: {} - {} - {}", device.getDeviceType(),
                            device.getMetrics().getProbeTitle(), device.getNodeId());

                    ThingUID thingUID = new ThingUID(ZWayBindingConstants.THING_TYPE_VIRTUAL_DEVICE,
                            device.getDeviceId());
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withLabel(device.getMetrics().getTitle()).withBridge(bridgeUID)
                            .withProperty(ZWayBindingConstants.DEVICE_CONFIG_VIRTUAL_DEVICE_ID, device.getDeviceId())
                            .build();
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
            mZWayDeviceScanningJob = AbstractDiscoveryService.scheduler.scheduleWithFixedDelay(
                    mZWayDeviceScanningRunnable, INITIAL_DELAY, SCAN_INTERVAL, TimeUnit.SECONDS);
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
