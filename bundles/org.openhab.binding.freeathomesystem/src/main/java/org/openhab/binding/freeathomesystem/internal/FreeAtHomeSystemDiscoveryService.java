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
package org.openhab.binding.freeathomesystem.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathomesystem.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeSystemDiscoveryService} is responsible for performing discovery of things
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeSystemDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSystemDiscoveryService.class);

    private @NonNullByDefault({}) FreeAtHomeBridgeHandler bridgeHandler;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (bridgeHandler != null) {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();

                List<String> deviceList;
                try {
                    deviceList = bridgeHandler.getDeviceDeviceList();

                    for (int i = 0; i < deviceList.size(); i++) {
                        FreeAtHomeDeviceDescription device = bridgeHandler
                                .getFreeatHomeDeviceDescription(deviceList.get(i));

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
                } catch (FreeAtHomeHttpCommunicationException e) {
                    logger.debug("TCommunication error in device discovery with the bridge: {}",
                            bridgeHandler.getThing().getLabel());
                }
            }
        }
    };

    public FreeAtHomeSystemDiscoveryService(int timeout) {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, timeout, false);
    }

    public FreeAtHomeSystemDiscoveryService() {
        super(FreeAtHomeSystemBindingConstants.SUPPORTED_THING_TYPES_UIDS, 90, false);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(FreeAtHomeSystemBindingConstants.BRIDGE_TYPE_UID);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startScan() {
        this.removeOlderResults(Instant.now().getEpochSecond());

        scheduler.execute(runnable);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(Instant.now().getEpochSecond());
    }

    @Override
    public void deactivate() {
        removeOlderResults(Instant.now().getEpochSecond());
    }
}
