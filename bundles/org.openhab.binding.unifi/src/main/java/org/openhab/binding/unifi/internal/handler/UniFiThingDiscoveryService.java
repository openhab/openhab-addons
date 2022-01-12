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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_CID;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_MAC_ADDRESS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_PORT_IDX;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_SITE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiPortTable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for detecting things connected to a UniFi controller.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class UniFiThingDiscoveryService extends AbstractDiscoveryService
        implements ThingHandlerService, DiscoveryService {

    /**
     * Timeout for discovery time.
     */
    private static final int UNIFI_DISCOVERY_TIMEOUT_SECONDS = 30;
    private static final long TTL_SECONDS = TimeUnit.MINUTES.toSeconds(5);

    private final Logger logger = LoggerFactory.getLogger(UniFiThingDiscoveryService.class);

    private @Nullable UniFiControllerThingHandler bridgeHandler;

    public UniFiThingDiscoveryService() {
        super(UniFiBindingConstants.THING_TYPE_SUPPORTED, UNIFI_DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(final ThingHandler handler) {
        if (handler instanceof UniFiControllerThingHandler) {
            bridgeHandler = (UniFiControllerThingHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        final UniFiControllerThingHandler bh = bridgeHandler;
        if (bh == null) {
            return;
        }
        final UniFiController controller = bh.getController();
        if (controller == null) {
            return;
        }
        try {
            controller.refresh();
            final ThingUID bridgeUID = bh.getThing().getUID();
            discoverClients(controller, bridgeUID);
            discoverPoePorts(controller, bridgeUID);
        } catch (final UniFiException e) {
            logger.debug("Exception during discovery of UniFi Things", e);
        }
    }

    private void discoverClients(final UniFiController controller, final ThingUID bridgeUID) {
        for (final UniFiClient uc : controller.getClients()) {
            final var thingTypeUID = uc.isWireless() ? UniFiBindingConstants.THING_TYPE_WIRELESS_CLIENT
                    : UniFiBindingConstants.THING_TYPE_WIRED_CLIENT;
            final ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, uc.getId());
            final Map<String, Object> properties = Map.of(PARAMETER_CID, uc.getMac(), PARAMETER_SITE,
                    uc.getSite().getName());

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withBridge(bridgeUID)
                    .withTTL(TTL_SECONDS).withProperties(properties).withLabel(uc.getAlias()).build());
        }
    }

    private void discoverPoePorts(final UniFiController controller, final ThingUID bridgeUID) {
        for (final Map<Integer, UniFiPortTable> uc : controller.getSwitchPorts()) {
            for (final Entry<Integer, UniFiPortTable> sp : uc.entrySet()) {
                final UniFiPortTable pt = sp.getValue();
                final String deviceMac = pt.getDevice().getMac();
                final String id = deviceMac.replaceAll(":", "") + "_" + pt.getPortIdx();
                final ThingUID thingUID = new ThingUID(UniFiBindingConstants.THING_TYPE_POE_PORT, bridgeUID, id);
                final Map<String, Object> properties = Map.of(PARAMETER_PORT_IDX, pt.getPortIdx(),
                        PARAMETER_MAC_ADDRESS, deviceMac);
                logger.debug("Found PoE PORT: {} ", properties);
                thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                        .withThingType(UniFiBindingConstants.THING_TYPE_POE_PORT).withBridge(bridgeUID)
                        .withTTL(TTL_SECONDS).withProperties(properties).withLabel(pt.getName()).build());
            }
        }
    }
}
