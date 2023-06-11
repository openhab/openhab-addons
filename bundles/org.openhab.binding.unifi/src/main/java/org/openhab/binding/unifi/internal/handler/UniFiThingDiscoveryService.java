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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_CID;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_MAC_ADDRESS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_PORT_NUMBER;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_SID;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_SITE;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_WID;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.PARAMETER_WIFI_NAME;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiPortTuple;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.binding.unifi.internal.api.dto.UniFiSwitchPorts;
import org.openhab.binding.unifi.internal.api.dto.UniFiWlan;
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
    private static final int THING_ID_LENGTH = 8;
    private static final Pattern DEFAULT_PORTNAME = Pattern.compile("Port \\d+");

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
            final UniFiControllerCache cache = controller.getCache();
            final ThingUID bridgeUID = bh.getThing().getUID();

            discoverSites(cache, bridgeUID);
            discoverWlans(cache, bridgeUID);
            discoverClients(cache, bridgeUID);
            discoverPoePorts(cache, bridgeUID);
        } catch (final UniFiException e) {
            logger.debug("Exception during discovery of UniFi Things", e);
        }
    }

    private void discoverSites(final UniFiControllerCache cache, final ThingUID bridgeUID) {
        for (final UniFiSite site : cache.getSites()) {
            final ThingUID thingUID = new ThingUID(UniFiBindingConstants.THING_TYPE_SITE, bridgeUID,
                    stripIdShort(site.getId()));
            final Map<String, Object> properties = Map.of(PARAMETER_SID, site.getId());

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(UniFiBindingConstants.THING_TYPE_SITE)
                    .withBridge(bridgeUID).withRepresentationProperty(PARAMETER_SID).withTTL(TTL_SECONDS)
                    .withProperties(properties).withLabel(site.getName()).build());
        }
    }

    private void discoverWlans(final UniFiControllerCache cache, final ThingUID bridgeUID) {
        for (final UniFiWlan wlan : cache.getWlans()) {
            final ThingUID thingUID = new ThingUID(UniFiBindingConstants.THING_TYPE_WLAN, bridgeUID,
                    stripIdShort(wlan.getId()));
            final String siteName = wlan.getSite() == null ? "" : wlan.getSite().getName();
            final Map<String, Object> properties = Map.of(PARAMETER_WID, wlan.getId(), PARAMETER_SITE, siteName,
                    PARAMETER_WIFI_NAME, wlan.getName());

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(UniFiBindingConstants.THING_TYPE_WLAN)
                    .withBridge(bridgeUID).withRepresentationProperty(PARAMETER_WID).withTTL(TTL_SECONDS)
                    .withProperties(properties).withLabel(wlan.getName()).build());
        }
    }

    private void discoverClients(final UniFiControllerCache cache, final ThingUID bridgeUID) {
        for (final UniFiClient uc : cache.getClients()) {
            final var thingTypeUID = uc.isWireless() ? UniFiBindingConstants.THING_TYPE_WIRELESS_CLIENT
                    : UniFiBindingConstants.THING_TYPE_WIRED_CLIENT;
            final ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, stripIdShort(uc.getId()));
            final Map<String, Object> properties = Map.of(PARAMETER_CID, uc.getMac(), PARAMETER_SITE,
                    uc.getSite().getName());

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withBridge(bridgeUID)
                    .withRepresentationProperty(PARAMETER_CID).withTTL(TTL_SECONDS).withProperties(properties)
                    .withLabel(uc.getName()).build());
        }
    }

    /**
     * Shorten the id to make it a bit more comprehensible.
     *
     * @param id id to shorten.
     * @return shortened id or if to short the original id
     */
    private static String stripIdShort(final String id) {
        return id != null && id.length() > THING_ID_LENGTH ? id.substring(id.length() - THING_ID_LENGTH) : id;
    }

    private void discoverPoePorts(final UniFiControllerCache cache, final ThingUID bridgeUID) {
        for (final UniFiSwitchPorts uc : cache.getSwitchPorts()) {
            for (final UniFiPortTuple pt : uc.getPoePorts()) {
                final String deviceMac = pt.getDevice().getMac();
                final String id = deviceMac.replace(":", "") + "_" + pt.getPortIdx();
                final ThingUID thingUID = new ThingUID(UniFiBindingConstants.THING_TYPE_POE_PORT, bridgeUID, id);
                final Map<String, Object> properties = Map.of(PARAMETER_PORT_NUMBER, pt.getPortIdx(),
                        PARAMETER_MAC_ADDRESS, deviceMac);

                thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                        .withThingType(UniFiBindingConstants.THING_TYPE_POE_PORT).withBridge(bridgeUID)
                        .withTTL(TTL_SECONDS).withProperties(properties).withLabel(portName(pt)).build());
            }
        }
    }

    /**
     * If the PoE port hasn't it's own name, but is named Port with a number the name is prefixed with the device name.
     *
     * @param pt port object
     * @return label for the discovered PoE port
     */
    private @Nullable String portName(final UniFiPortTuple pt) {
        final String portName = pt.getTable().getName();

        return DEFAULT_PORTNAME.matcher(portName).find() ? pt.getDevice().getName() + " " + portName : portName;
    }
}
