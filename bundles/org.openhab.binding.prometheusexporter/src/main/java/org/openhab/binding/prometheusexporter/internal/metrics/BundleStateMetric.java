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
package org.openhab.binding.prometheusexporter.internal.metrics;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.prometheusexporter.internal.PrometheusExporterBindingConstants;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * The {@link BundleStateMetric} class implements a gauge metrics for the OSGI bundles states
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class BundleStateMetric implements PrometheusMetric {
    private final Logger logger = LoggerFactory.getLogger(BundleStateMetric.class);
    public static final String METRIC_NAME = "openhab_bundle_state";
    private final BundleContext bundleContext;
    @Nullable
    private Gauge bundleStateGauge;

    public BundleStateMetric(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        bundleStateGauge = null;
    }

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        bundleStateGauge = Gauge.build(METRIC_NAME, "openHAB OSGi bundles state").labelNames("bundle").register();
        return this;
    }

    @Override
    public void refresh() {
        logger.debug("Bundle state refresh() called.");
        if (bundleStateGauge == null) {
            logger.debug("Skipping refresh request since measurement has not been started.");
            return;
        }
        Stream.of(bundleContext.getBundles()).forEach(bundle -> {
            logger.debug("Checking bundle {}", bundle);
            // TODO make configurable
            if (bundle.getSymbolicName().startsWith("org.openhab")) {
                logger.debug("Adding bundle to metric.");
                Gauge.Child child = new Gauge.Child();
                child.set(bundle.getState());
                bundleStateGauge.setChild(child, bundle.getSymbolicName());
            }
        });
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        if (bundleStateGauge != null) {
            CollectorRegistry.defaultRegistry.unregister(bundleStateGauge);
            bundleStateGauge = null;
        }
        return this;
    }

    @Override
    public Set<String> getMetricNames() {
        return Set.of(METRIC_NAME);
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_BUNDLE_STATE;
    }
}
