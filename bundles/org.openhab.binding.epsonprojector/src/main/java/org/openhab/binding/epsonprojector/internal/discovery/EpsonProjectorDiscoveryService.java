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
package org.openhab.binding.epsonprojector.internal.discovery;

import static org.openhab.binding.epsonprojector.internal.EpsonProjectorBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpsonProjectoreDiscoveryService} class implements a service
 * for discovering Epson projectors using the AMX Device Discovery protocol.
 *
 * @author Mark Hilbush - Initial contribution
 * @author Michael Lobstein - Adapted for the Epson Projector binding
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.epsonprojector")
public class EpsonProjectorDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorDiscoveryService.class);
    private @Nullable ScheduledFuture<?> epsonDiscoveryJob;

    // Discovery parameters
    public static final boolean BACKGROUND_DISCOVERY_ENABLED = true;
    public static final int BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC = 10;

    private NetworkAddressService networkAddressService;

    private boolean terminate = false;

    @Activate
    public EpsonProjectorDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(SUPPORTED_THING_TYPES_UIDS, 0, BACKGROUND_DISCOVERY_ENABLED);
        this.networkAddressService = networkAddressService;
        epsonDiscoveryJob = null;
        terminate = false;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (epsonDiscoveryJob == null) {
            terminate = false;
            logger.debug("Starting background discovery job in {} seconds", BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC);
            epsonDiscoveryJob = scheduler.schedule(this::discover, BACKGROUND_DISCOVERY_DELAY_TIMEOUT_SEC,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> epsonDiscoveryJob = this.epsonDiscoveryJob;
        if (epsonDiscoveryJob != null) {
            terminate = true;
            epsonDiscoveryJob.cancel(false);
            this.epsonDiscoveryJob = null;
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
        MulticastListener epsonMulticastListener;
        String local = "127.0.0.1";

        try {
            String ip = networkAddressService.getPrimaryIpv4HostAddress();
            epsonMulticastListener = new MulticastListener((ip != null ? ip : local));
        } catch (SocketException se) {
            logger.debug("Discovery job got Socket exception creating multicast socket: {}", se.getMessage());
            return;
        } catch (IOException ioe) {
            logger.debug("Discovery job got IO exception creating multicast socket: {}", ioe.getMessage());
            return;
        }

        while (!terminate) {
            try {
                // Wait for a discovery beacon to return properties for an Epson projector.
                Map<String, Object> thingProperties = epsonMulticastListener.waitForBeacon();

                if (thingProperties != null) {
                    // The MulticastListener found a projector, add it as new thing
                    String uid = (String) thingProperties.get(THING_PROPERTY_MAC);
                    String ipAddress = (String) thingProperties.get(THING_PROPERTY_HOST);

                    if (uid != null) {
                        logger.trace("Projector with UID {} discovered at IP: {}", uid, ipAddress);

                        ThingUID thingUid = new ThingUID(THING_TYPE_PROJECTOR_TCP, uid);
                        logger.trace("Creating epson projector discovery result for: {}, IP={}", uid, ipAddress);
                        thingDiscovered(DiscoveryResultBuilder.create(thingUid).withProperties(thingProperties)
                                .withLabel("Epson Projector " + uid).withRepresentationProperty(THING_PROPERTY_MAC)
                                .build());
                    }
                }
            } catch (IOException ioe) {
                logger.debug("Discovery job got exception waiting for beacon: {}", ioe.getMessage());
            }
        }
        epsonMulticastListener.shutdown();
        logger.debug("Discovery job is exiting");
    }
}
