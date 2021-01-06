/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.prometheusexporter;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.prometheusexporter.internal.PrometheusExporterBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PrometheusExportDiscoveryService} class ...
 *
 * @author Robert Bach - Initial contribution
 */
@Component(service = DiscoveryService.class)
@NonNullByDefault
public class PrometheusExportDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PrometheusExportDiscoveryService.class);

    public PrometheusExportDiscoveryService() {
        super(PrometheusExporterBindingConstants.SUPPORTED_THING_TYPES_UIDS, 15);
    }

    @Override
    protected void startScan() {
        logger.debug("Adding prometheus exporter generic thing to inbox...");
        ThingUID uid = new ThingUID(PrometheusExporterBindingConstants.THING_TYPE_GENERIC,
                "prometheus-exporter-generic");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("Generic Prometheus exporter")
                .withProperties(Map.of("bundleStatePackageFilter", "org.openhab", "refreshInterval", 60)).build();
        thingDiscovered(result);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return PrometheusExporterBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }
}
