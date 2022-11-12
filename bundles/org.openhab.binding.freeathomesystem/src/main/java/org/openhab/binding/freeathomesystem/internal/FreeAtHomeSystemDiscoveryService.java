/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeSystemDiscoveryService} is responsible for performing discovery of things
 *
 * @author Andras Uhrin
 */
@Component(service = DiscoveryService.class)
@NonNullByDefault
public class FreeAtHomeSystemDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSystemDiscoveryService.class);

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            FreeAtHomeBridgeHandler bridge = FreeAtHomeBridgeHandler.freeAtHomeSystemHandler;

            if (bridge != null) {
                ThingUID bridgeUID = bridge.getThing().getUID();

                List<String> deviceList = bridge.getDeviceDeviceList();

                if (deviceList == null) {
                    return;
                }

                for (int i = 0; i < deviceList.size(); i++) {

                    FreeAtHomeDeviceDescription device = bridge.getFreeatHomeDeviceDescription(deviceList.get(i));

                    boolean useGenericDevice = true;

                    if (useGenericDevice) {
                        ThingUID uid = new ThingUID(FreeAtHomeSystemBindingConstants.FREEATHOMEDEVICE_TYPE_UID,
                                bridgeUID, device.deviceId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.deviceId);
                        properties.put("interface", device.interfaceType);

                        String deviceLabel = device.deviceLabel;

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(deviceLabel)
                                .withBridge(bridgeUID).withProperties(properties).build();

                        thingDiscovered(discoveryResult);

                        logger.debug("Thing discovered - DeviceId: {} - Device label: {}", device.getDeviceId(),
                                device.getDeviceLabel());
                    }
                }

                stopScan();
            }
        }
    };

    public FreeAtHomeSystemDiscoveryService(int timeout) {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, 90, false);
    }

    public FreeAtHomeSystemDiscoveryService() {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, 90, false);
    }

    @Override
    protected void startScan() {

        this.removeOlderResults(getTimestampOfLastScan());

        scheduler.execute(runnable);
    }
}
