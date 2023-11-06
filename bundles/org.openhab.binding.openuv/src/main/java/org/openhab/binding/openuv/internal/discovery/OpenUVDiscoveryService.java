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
package org.openhab.binding.openuv.internal.discovery;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;
import static org.openhab.binding.openuv.internal.config.ReportConfiguration.LOCATION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openuv.internal.handler.OpenUVBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenUVDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class OpenUVDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(OpenUVDiscoveryService.class);

    private @Nullable OpenUVBridgeHandler bridgeHandler;

    public OpenUVDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof OpenUVBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
            this.i18nProvider = bridgeHandler.getI18nProvider();
            this.localeProvider = bridgeHandler.getLocaleProvider();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting OpenUV discovery scan");
        OpenUVBridgeHandler bridge = bridgeHandler;
        if (bridge != null) {
            PointType location = bridge.getLocation();
            if (location != null) {
                ThingUID bridgeUID = bridge.getThing().getUID();
                thingDiscovered(
                        DiscoveryResultBuilder.create(new ThingUID(LOCATION_REPORT_THING_TYPE, bridgeUID, LOCAL))
                                .withLabel("@text/discovery.openuv.uvreport.local.label")
                                .withProperty(LOCATION, location.toString()).withRepresentationProperty(LOCATION)
                                .withBridge(bridgeUID).build());
            } else {
                logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            }
        } else {
            logger.debug("OpenUV Bridge Handler is not set -> Will not provide any discovery results");
        }
    }
}
