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
package org.openhab.binding.rfxcom.internal.discovery;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.ID_DELIMITER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.handler.RFXComBridgeHandler;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RFXComDeviceDiscoveryService} class is used to discover RFXCOM
 * devices that send messages to RFXCOM bridge.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Laurent Garnier - use ThingHandlerService
 */
@Component(scope = ServiceScope.PROTOTYPE, service = RFXComDeviceDiscoveryService.class)
@NonNullByDefault
public class RFXComDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<RFXComBridgeHandler>
        implements DeviceMessageListener {
    private final Logger logger = LoggerFactory.getLogger(RFXComDeviceDiscoveryService.class);
    private final int DISCOVERY_TTL = 3600;

    public RFXComDeviceDiscoveryService() {
        super(RFXComBridgeHandler.class, null, 1, false);
    }

    @Override
    public void initialize() {
        thingHandler.registerDeviceStatusListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.unregisterDeviceStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RFXComBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        // this can be ignored here as we discover devices from received messages
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RFXComDeviceMessage message) throws RFXComException {
        logger.trace("Received: bridge: {} message: {}", bridge, message);

        String id = message.getDeviceId();
        ThingTypeUID uid = RFXComBindingConstants.PACKET_TYPE_THING_TYPE_UID_MAP.get(message.getPacketType());
        if (uid == null) {
            logger.debug("cannot find uid for message {}", message);
            return;
        }
        ThingUID thingUID = new ThingUID(uid, bridge, id.replace(ID_DELIMITER, "_"));

        if (!thingHandler.getConfiguration().disableDiscovery) {
            logger.trace("Adding new RFXCOM {} with id '{}' to inbox", thingUID, id);
            DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID).withBridge(bridge)
                    .withTTL(DISCOVERY_TTL);
            message.addDevicePropertiesTo(discoveryResultBuilder);

            thingDiscovered(discoveryResultBuilder.build());
        } else {
            logger.trace("Ignoring RFXCOM {} with id '{}' - discovery disabled", thingUID, id);
        }
    }
}
