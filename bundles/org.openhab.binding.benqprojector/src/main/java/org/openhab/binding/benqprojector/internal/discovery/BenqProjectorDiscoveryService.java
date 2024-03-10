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
package org.openhab.binding.benqprojector.internal.discovery;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BenqProjectorDiscoveryService} class implements a service
 * for discovering BenQ projectors using the AMX Device Discovery protocol.
 *
 * @author Mark Hilbush - Initial contribution
 * @author Michael Lobstein - Adapted for the BenQ Projector binding
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.benqprojector")
public class BenqProjectorDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(BenqProjectorDiscoveryService.class);
    private @Nullable ScheduledFuture<?> benqDiscoveryJob;

    // Discovery parameters
    public static final boolean BACKGROUND_DISCOVERY_ENABLED = true;
    public static final int BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC = 10;

    private NetworkAddressService networkAddressService;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final @Nullable Bundle bundle;

    private boolean terminate = false;

    @Activate
    public BenqProjectorDiscoveryService(@Reference NetworkAddressService networkAddressService,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, 0, BACKGROUND_DISCOVERY_ENABLED);
        this.networkAddressService = networkAddressService;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(BenqProjectorDiscoveryService.class);

        benqDiscoveryJob = null;
        terminate = false;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (benqDiscoveryJob == null) {
            terminate = false;
            logger.debug("Starting background discovery job in {} seconds", BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC);
            benqDiscoveryJob = scheduler.schedule(this::discover, BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> benqDiscoveryJob = this.benqDiscoveryJob;
        if (benqDiscoveryJob != null) {
            terminate = true;
            benqDiscoveryJob.cancel(false);
            this.benqDiscoveryJob = null;
        }
    }

    @Override
    public void startScan() {
    }

    @Override
    public void stopScan() {
    }

    private synchronized void discover() {
        logger.debug("Discovery job is running");
        MulticastListener benqMulticastListener;
        String local = "127.0.0.1";

        try {
            String ip = networkAddressService.getPrimaryIpv4HostAddress();
            benqMulticastListener = new MulticastListener((ip != null ? ip : local));
        } catch (IOException ioe) {
            logger.debug("Discovery job got IO exception creating multicast socket: {}", ioe.getMessage());
            return;
        }

        while (!terminate) {
            try {
                // Wait for a discovery beacon to return properties for a BenQ projector.
                Map<String, Object> thingProperties = benqMulticastListener.waitForBeacon();

                if (thingProperties != null) {
                    // The MulticastListener found a projector, add it as new thing
                    String uid = (String) thingProperties.get(Thing.PROPERTY_MAC_ADDRESS);
                    String ipAddress = (String) thingProperties.get(THING_PROPERTY_HOST);

                    if (uid != null) {
                        logger.trace("Projector with UID {} discovered at IP: {}", uid, ipAddress);

                        ThingUID thingUid = new ThingUID(THING_TYPE_PROJECTOR_TCP, uid);
                        logger.trace("Creating BenQ projector discovery result for: {}, IP={}", uid, ipAddress);
                        thingDiscovered(
                                DiscoveryResultBuilder.create(thingUid).withProperties(thingProperties)
                                        .withLabel(translationProvider.getText(bundle,
                                                "thing-type.benqprojector.discovery.label", "BenQ Projector",
                                                localeProvider.getLocale()) + " " + uid)
                                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build());
                    }
                }
            } catch (IOException ioe) {
                logger.debug("Discovery job got exception waiting for beacon: {}", ioe.getMessage());
            }
        }
        benqMulticastListener.shutdown();
        logger.debug("Discovery job is exiting");
    }
}
