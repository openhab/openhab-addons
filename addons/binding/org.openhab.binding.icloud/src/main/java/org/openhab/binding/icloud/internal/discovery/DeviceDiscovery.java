/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.discovery;

import static org.openhab.binding.icloud.BindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.icloud.handler.ICloudAccountBridgeHandler;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.json.DeviceInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each icloud device
 * found in the data received from {@link ICloudAccountBridgeHandler}.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class DeviceDiscovery extends AbstractDiscoveryService implements ICloudDeviceInformationListener {
    private final Logger logger = LoggerFactory.getLogger(DeviceDiscovery.class);
    private static final int TIMEOUT = 10;
    private ThingUID bridgeUID;
    private ICloudAccountBridgeHandler handler;

    public DeviceDiscovery(ICloudAccountBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);

        this.handler = bridgeHandler;
        this.bridgeUID = bridgeHandler.getThing().getUID();
    }

    @Override
    public void deviceInformationUpdate(List<DeviceInformation> deviceInformationList) {
        if (deviceInformationList != null) {
            for (DeviceInformation deviceInformationRecord : deviceInformationList) {

                String deviceTypeName = deviceInformationRecord.getDeviceDisplayName();
                String deviceOwnerName = deviceInformationRecord.getName();

                String thingLabel = deviceOwnerName + " (" + deviceTypeName + ")";
                String deviceId = deviceInformationRecord.getId();
                String deviceIdHash = Integer.toHexString(deviceId.hashCode());

                logger.debug("iCloud device discovery for [{}]", deviceInformationRecord.getDeviceDisplayName());

                ThingUID uid = new ThingUID(THING_TYPE_ICLOUDDEVICE, bridgeUID, deviceIdHash);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                        .withProperty(DEVICE_NAME, deviceOwnerName).withProperty(DEVICE_PROPERTY_ID, deviceId)
                        .withRepresentationProperty(DEVICE_PROPERTY_ID).withLabel(thingLabel).build();

                logger.debug("Device [{}, {}] found.", deviceIdHash, deviceId);

                thingDiscovered(result);

            }
        }
    }

    @Override
    protected void startScan() {
    }

    public void activate() {
        handler.registerListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        handler.unregisterListener(this);
    }

}
