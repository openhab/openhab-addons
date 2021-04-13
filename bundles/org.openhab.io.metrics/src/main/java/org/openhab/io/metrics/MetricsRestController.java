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
package org.openhab.io.metrics;

import java.util.Objects;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.Role;
import org.openhab.core.io.monitor.MeterRegistryProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * * The {@link MetricsRestController} class implements the REST endpoints for all pull based monitoring systems.
 *
 * @author Robert Bach - Initial contribution
 */
@Component(immediate = true, service = MetricsRestController.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + MetricsService.METRICS_APP_NAME + ")")
@Path("")
@JSONRequired
@RolesAllowed({ Role.USER, Role.ADMIN })
@Tag(name = MetricsRestController.PATH_METRICS)
@NonNullByDefault
public class MetricsRestController {
    private final Logger logger = LoggerFactory.getLogger(MetricsRestController.class);
    public static final String PATH_METRICS = "metrics";
    private @Nullable CompositeMeterRegistry meterRegistry = null;
    private final PrometheusMeterRegistry prometheusMeterRegistry = new PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT);

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
        if (meterRegistry != null) {
            Objects.requireNonNull(meterRegistry).remove(prometheusMeterRegistry);
        }
        meterRegistry = meterRegistryProvider.getOHMeterRegistry();
        Objects.requireNonNull(meterRegistry).add(prometheusMeterRegistry);
        logger.debug("Core metrics registry retrieved and Prometheus registry added successfully.");
    }
}
