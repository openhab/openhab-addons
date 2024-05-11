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
package org.openhab.binding.salus.internal.discovery;

import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_DEVICE_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_IT600_DEVICE_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.IT_600;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.OEM_MODEL;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.handler.CloudApi;
import org.openhab.binding.salus.internal.handler.CloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.SalusApiException;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class CloudDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CloudDiscovery.class);
    private final CloudApi cloudApi;
    private final ThingUID bridgeUid;

    public CloudDiscovery(CloudBridgeHandler bridgeHandler, CloudApi cloudApi, ThingUID bridgeUid)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.cloudApi = cloudApi;
        this.bridgeUid = bridgeUid;
    }

    @Override
    protected void startScan() {
        try {
            var devices = cloudApi.findDevices();
            logger.debug("Found {} devices while scanning", devices.size());
            devices.stream().filter(Device::isConnected).forEach(this::addThing);
        } catch (SalusApiException e) {
            logger.warn("Error while scanning", e);
            stopScan();
        }
    }

    private void addThing(Device device) {
        logger.debug("Adding device \"{}\" ({}) to found things", device.name(), device.dsn());
        var thingUID = new ThingUID(findDeviceType(device), bridgeUid, device.dsn());
        var discoveryResult = createDiscoveryResult(thingUID, buildThingLabel(device), buildThingProperties(device));
        thingDiscovered(discoveryResult);
    }

    private static ThingTypeUID findDeviceType(Device device) {
        var props = device.properties();
        if (props.containsKey(OEM_MODEL)) {
            var model = props.get(OEM_MODEL);
            if (model != null) {
                if (model.toString().toLowerCase(Locale.ENGLISH).contains(IT_600)) {
                    return SALUS_IT600_DEVICE_TYPE;
                }
            }
        }
        return SALUS_DEVICE_TYPE;
    }

    private DiscoveryResult createDiscoveryResult(ThingUID thingUID, String label, Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUid).withProperties(properties).withLabel(label)
                .withRepresentationProperty(DSN).build();
    }

    private String buildThingLabel(Device device) {
        var name = device.name();
        return (!"".equals(name)) ? name : device.dsn();
    }

    private Map<String, Object> buildThingProperties(Device device) {
        return Map.of(DSN, device.dsn());
    }
}
