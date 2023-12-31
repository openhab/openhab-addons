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
package org.openhab.binding.openuv.internal.discovery;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;
import static org.openhab.binding.openuv.internal.config.ReportConfiguration.LOCATION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openuv.internal.handler.OpenUVBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenUVDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OpenUVDiscoveryService.class)
@NonNullByDefault
public class OpenUVDiscoveryService extends AbstractThingHandlerDiscoveryService<OpenUVBridgeHandler> {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private @NonNullByDefault({}) LocationProvider locationProvider;

    private final Logger logger = LoggerFactory.getLogger(OpenUVDiscoveryService.class);

    public OpenUVDiscoveryService() {
        super(OpenUVBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    @Reference(unbind = "-")
    public void bindTranslationProvider(TranslationProvider translationProvider) {
        this.i18nProvider = translationProvider;
    }

    @Reference(unbind = "-")
    public void bindLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Reference(unbind = "-")
    public void bindLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting OpenUV discovery scan");
        PointType location = locationProvider.getLocation();
        if (location != null) {
            ThingUID bridgeUID = thingHandler.getThing().getUID();
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(LOCATION_REPORT_THING_TYPE, bridgeUID, LOCAL))
                    .withLabel("@text/discovery.openuv.uvreport.local.label")
                    .withProperty(LOCATION, location.toString()).withRepresentationProperty(LOCATION)
                    .withBridge(bridgeUID).build());
        } else {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
        }
    }
}
