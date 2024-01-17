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
package org.openhab.binding.knx.internal.discovery;

import static org.openhab.binding.knx.internal.KNXBindingConstants.THING_TYPE_IP_BRIDGE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.Discoverer.Result;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB.ServiceFamily;

/**
 * Discovers KNXnet/IP interfaces or routers and adds the results to the inbox.
 * Several items per device might be created, as routers typically support routing and tunneling.
 * Discovery uses multicast traffic to IP 224.0.23.12, port 3671.
 *
 * @implNote Discovery is based on the functionality provided by Calimero library.
 * @author Holger Friedrich - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.knx")
@NonNullByDefault
public class KNXnetDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(KNXnetDiscoveryService.class);

    private @Nullable Future<?> scanFuture = null;

    public KNXnetDiscoveryService() {
        super(Set.of(THING_TYPE_IP_BRIDGE), 3, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        // only start once at startup
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    @Override
    protected void startScan() {
        if (scanFuture == null) {
            scanFuture = scheduler.submit(this::startDiscovery);
        } else {
            logger.debug("KNXnet/IP background discovery scan in progress");
        }
    }

    @Override
    protected void stopScan() {
        Future<?> tmpScanFuture = scanFuture;
        if (tmpScanFuture != null) {
            tmpScanFuture.cancel(false);
            scanFuture = null;
        }
    }

    private synchronized void startDiscovery() {
        try {
            logger.debug("Starting KNXnet/IP discovery scan");
            Discoverer discovererUdp = new Discoverer(0, false);
            discovererUdp.startSearch(3, true);

            List<Result<SearchResponse>> responses = discovererUdp.getSearchResponses();

            for (Result<SearchResponse> r : responses) {
                @Nullable
                SearchResponse response = r.getResponse();
                Map<ServiceFamily, Integer> services = response.getServiceFamilies().families();

                if (services.containsKey(ServiceFamiliesDIB.ServiceFamily.Tunneling)
                        || services.containsKey(ServiceFamiliesDIB.ServiceFamily.Routing)) {
                    String serial = Objects.toString(response.getDevice().serialNumber()).replace(':', '-');

                    if (logger.isTraceEnabled()) {
                        logger.trace("Discovered device {}", response);
                    } else {
                        logger.debug("Discovered device {}, {}, {}", response.getDevice().getName(), serial,
                                response.getDevice().getMACAddressString());
                    }

                    if (services.containsKey(ServiceFamiliesDIB.ServiceFamily.Tunneling)) {
                        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_IP_BRIDGE, serial))
                                .withLabel(response.getDevice().getName()).withProperty("serialNumber", serial)
                                .withProperty("type", "TUNNEL")
                                .withProperty("ipAddress",
                                        "" + response.getControlEndpoint().getAddress().getHostAddress())
                                .withProperty("port", "" + response.getControlEndpoint().getPort())
                                .withRepresentationProperty("serialNumber").build());
                    }
                    if (services.containsKey(ServiceFamiliesDIB.ServiceFamily.Routing)) {
                        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_IP_BRIDGE, serial))
                                .withLabel(response.getDevice().getName() + " (router mode)")
                                .withProperty("serialNumber", serial + "-r").withProperty("type", "ROUTER")
                                .withProperty("ipAddress", "224.0.23.12")
                                .withProperty("port", "" + response.getControlEndpoint().getPort())
                                .withRepresentationProperty("serialNumber").build());
                    }
                } else {
                    logger.trace("Ignoring device {}", response);
                }
            }
            logger.debug("Completed KNXnet/IP discovery scan");
        } catch (Exception ex) {
            logger.warn("An error occurred during KNXnet/IP discovery {}", ex.getMessage(), ex);
        } finally {
            scanFuture = null;
            removeOlderResults(getTimestampOfLastScan());
        }
    }
}
