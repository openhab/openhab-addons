/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.io.metrics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.Role;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.monitor.MeterRegistryProvider;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.io.metrics.exporters.InfluxMetricsExporter;
import org.openhab.io.metrics.exporters.JmxMetricsExporter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The {@link MetricsRestController} class implements the REST endpoints for all pull based monitoring systems.
 *
 * @author Robert Bach - Initial contribution
 */
@Component(configurationPid = "org.openhab.metrics", immediate = true, service = MetricsRestController.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@Path(MetricsRestController.PATH_METRICS)
@JSONRequired
@RolesAllowed({ Role.USER, Role.ADMIN })
@Tag(name = MetricsRestController.PATH_METRICS)
@NonNullByDefault
@ConfigurableService(category = "io", label = "Metrics Service", description_uri = "io:metrics")
public class MetricsRestController {
    private final Logger logger = LoggerFactory.getLogger(MetricsRestController.class);
    public static final String PATH_METRICS = "metrics";
    private @Nullable CompositeMeterRegistry meterRegistry;
    private final PrometheusMeterRegistry prometheusMeterRegistry = new PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT);
    private final Set<MetricsExporter> metricsExporters = new HashSet<>();
    private @Nullable MetricsConfiguration config;

    @GET
    @Path("/prometheus")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(operationId = "getPrometheusMetrics", summary = "Gets openHAB system and core metrics in a Prometheus compatible format.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))) })
    public String getPrometheusMetrics() {
        return prometheusMeterRegistry.scrape();
    }

    @Reference
    public void setMeterRegistryProvider(MeterRegistryProvider meterRegistryProvider) {
        CompositeMeterRegistry meterRegistry = this.meterRegistry;
        if (meterRegistry != null) {
            meterRegistry.remove(prometheusMeterRegistry);
        }
        meterRegistry = meterRegistryProvider.getOHMeterRegistry();
        meterRegistry.add(prometheusMeterRegistry);
        this.meterRegistry = meterRegistry;
        logger.debug("Core metrics registry retrieved and Prometheus registry added successfully.");
        updateMeterRegistry();
    }

    @Activate
    protected void activate(Map<@Nullable String, @Nullable Object> configuration) {
        logger.info("Metrics service activated, serving the following URL(s): /rest/metrics/prometheus");
        metricsExporters.add(new InfluxMetricsExporter());
        metricsExporters.add(new JmxMetricsExporter());
        updateConfig(configuration);
        updateMeterRegistry();
    }

    @Modified
    protected synchronized void modified(Map<@Nullable String, @Nullable Object> configuration) {
        updateConfig(configuration);
    }

    private void updateConfig(@Nullable Map<@Nullable String, @Nullable Object> configuration) {
        this.config = new Configuration(configuration).as(MetricsConfiguration.class);
        logger.debug("Configuration: {}", this.config);
        this.metricsExporters.forEach(e -> e.updateExporterState(config));
    }

    private void updateMeterRegistry() {
        this.metricsExporters.forEach(e -> e.setMeterRegistry(meterRegistry));
    }
}
