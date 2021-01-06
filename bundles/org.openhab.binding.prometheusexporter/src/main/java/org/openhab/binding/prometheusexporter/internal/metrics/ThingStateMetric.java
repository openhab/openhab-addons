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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.prometheusexporter.internal.PrometheusExporterBindingConstants;
import org.openhab.core.thing.ThingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * The {@link ThingStateMetric} class implements a metric for the openHAB things states.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ThingStateMetric implements PrometheusMetric {
    private final Logger logger = LoggerFactory.getLogger(ThingStateMetric.class);
    public static final String METRIC_NAME = "openhab_thing_state";
    private final ThingRegistry thingRegistry;
    @Nullable
    private Gauge thingStateGauge = null;

    public ThingStateMetric(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        if (thingStateGauge == null) {
            thingStateGauge = Gauge.build(METRIC_NAME, "openHAB Things state").labelNames("thing").register();
        }
        return this;
    }

    @Override
    public void refresh() {
        if (thingStateGauge == null) {
            logger.debug("Skipping refresh request since measurement has not been started.");
            return;
        }
        thingRegistry.getAll().parallelStream().forEach(thing -> {
            Gauge.Child child = new Gauge.Child();
            child.set(thing.getStatus().ordinal());
            thingStateGauge.setChild(child, thing.getUID().getAsString());
        });
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        if (thingStateGauge != null) {
            CollectorRegistry.defaultRegistry.unregister(thingStateGauge);
            thingStateGauge = null;
        }
        return this;
    }

    @Override
    public Set<String> getMetricNames() {
        return Set.of(METRIC_NAME);
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_THING_STATE;
    }
}
