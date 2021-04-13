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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.monitor.MeterRegistryProvider;
import org.openhab.io.metrics.exporters.InfluxMetricsExporter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

/**
 * The {@link MetricsService} class implements the central component controlling the different metric endpoints/syncers.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = MetricsService.class)
@ConfigurableService(category = "io", label = "Metrics service", description_uri = "io:metrics")
public class MetricsService {
    public static final String METRICS_APP_NAME = "Metrics";
    public static final String ROOT = "/metrics";
    private final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    private @Nullable ServiceRegistration<Application> restService = null;
    private @Nullable MetricsConfiguration config;
    @Reference
    protected @NonNullByDefault({}) MetricsRestController metrics;
    private Set<MetricsExporter> metricsExporters = new HashSet<>();
    private @Nullable CompositeMeterRegistry meterRegistry = null;

    @Activate
    protected void activate(Map<@Nullable String, @Nullable Object> configuration) {
        MetricsRestApplication app = new MetricsRestApplication();
        BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        restService = context.registerService(Application.class, app, getServiceProperties());
        logger.info("Metrics service available under {}.", ROOT);
        metricsExporters.add(new InfluxMetricsExporter());
        updateConfig(configuration);
        updateMeterRegistry();
    }

    @Modified
    protected synchronized void modified(Map<@Nullable String, @Nullable Object> configuration) {
        updateConfig(configuration);
    }

    @Deactivate
    protected void deactivate() {
        if (restService != null) {
            Objects.requireNonNull(restService).unregister();
        }
    }

    Dictionary<@Nullable String, @Nullable String> getServiceProperties() {
        Dictionary<@Nullable String, @Nullable String> dict = new Hashtable<>();
        dict.put(JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE, ROOT);
        return dict;
    }

    @JaxrsName(METRICS_APP_NAME)
    private class MetricsRestApplication extends Application {
        @NonNullByDefault({})
        @Override
        public Set<Object> getSingletons() {
            return Set.of(metrics);
        }
    }

    @Reference
    public void setMeterRegistryProvider(MeterRegistryProvider meterRegistryProvider) {
        meterRegistry = meterRegistryProvider.getOHMeterRegistry();
        updateMeterRegistry();
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
