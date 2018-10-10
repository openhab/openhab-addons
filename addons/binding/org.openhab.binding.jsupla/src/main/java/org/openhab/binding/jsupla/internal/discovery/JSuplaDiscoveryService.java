/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal.discovery;

import com.google.common.collect.ImmutableMap;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.jsupla.handler.JSuplaCloudBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.SUPLA_DEVICE_GUID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.SUPLA_DEVICE_TYPE;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.SUPPORTED_THING_TYPES_UIDS;

/**
 * @author Grzeslowski - Initial contribution
 */
public class JSuplaDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(JSuplaDiscoveryService.class);
    private final JSuplaCloudBridgeHandler jSuplaCloudBridgeHandler;

    public JSuplaDiscoveryService(final JSuplaCloudBridgeHandler jSuplaCloudBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.jSuplaCloudBridgeHandler = requireNonNull(jSuplaCloudBridgeHandler);
    }

    @Override
    protected void startScan() {
        logger.trace("Active scan started, but there is no active scan for jSupla ;)");
        stopScan();
    }

    public void addSuplaDevice(String guid, String name) {
        final ThingUID bridgeUID = jSuplaCloudBridgeHandler.getThing().getUID();

        ThingUID thingUID = new ThingUID(SUPLA_DEVICE_TYPE, bridgeUID, String.valueOf(guid));
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                                        .withBridge(bridgeUID)
                                                        .withProperties(buildProperties(guid))
                                                        .withLabel(String.format("%s (%s)", name, guid))
                                                        .build();
        logger.debug("Discovered thing with GUID [{}]", guid);
        thingDiscovered(discoveryResult);
    }

    private Map<String, Object> buildProperties(final String guid) {
        return ImmutableMap.of(SUPLA_DEVICE_GUID, guid);
    }
}
