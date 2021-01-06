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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.prometheusexporter.internal.PrometheusExporterBindingConstants;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * The {@link ThreadPoolMetric} class implements metrics concerning openHAB thread pool figures
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ThreadPoolMetric implements PrometheusMetric {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolMetric.class);
    Map<Gauge, ThreadPoolGaugeInfo> gauge2gaugeInfo = new HashMap<>();

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        if (gauge2gaugeInfo.isEmpty()) {
            for (ThreadPoolGaugeInfo gaugeHolder : ThreadPoolGaugeInfo.values()) {
                logger.debug("Registering prometheus thread pool metric {}.", gaugeHolder.getGaugeName());
                gauge2gaugeInfo.put(Gauge.build(gaugeHolder.getGaugeName(), gaugeHolder.getDescription())
                        .labelNames("pool").register(), gaugeHolder);
            }
        }
        return this;
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        if (!gauge2gaugeInfo.isEmpty()) {
            for (Gauge gauge : gauge2gaugeInfo.keySet()) {
                CollectorRegistry.defaultRegistry.unregister(gauge);
            }
            gauge2gaugeInfo.clear();
        }
        return this;
    }

    @Override
    public void refresh() {
        if (gauge2gaugeInfo.isEmpty()) {
            logger.debug("Skipping refresh request since measurement has not been started.");
            return;
        }
        try {
            ThreadPoolManager.getPoolNames().forEach(this::addPoolMetrics);
        } catch (NoSuchMethodError nsme) {
            logger.info("A newer version of openHAB is required for thread pool metrics to work.");
        }
    }

    @Override
    public Set<String> getMetricNames() {
        return Stream.of(ThreadPoolGaugeInfo.values()).map(ThreadPoolGaugeInfo::getGaugeName)
                .collect(Collectors.toSet());
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_THREAD_POOLS;
    }

    private void addPoolMetrics(String poolName) {
        ExecutorService es = ThreadPoolManager.getPool(poolName);
        if (es == null) {
            return;
        }

        for (Gauge gauge : gauge2gaugeInfo.keySet()) {
            Gauge.Child child = new Gauge.Child();
            ThreadPoolGaugeInfo gaugeInfo = gauge2gaugeInfo.get(gauge);
            child.set(gaugeInfo.getExtractor().apply((ThreadPoolExecutor) es));
            gauge.setChild(child, poolName);
        }
    }
}
