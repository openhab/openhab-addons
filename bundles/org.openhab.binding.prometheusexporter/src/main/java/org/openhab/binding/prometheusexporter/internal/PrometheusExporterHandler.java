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
package org.openhab.binding.prometheusexporter.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.prometheusexporter.internal.metrics.BundleStateMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.EventCountMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.HotspotMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.InboxCountMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.PrometheusMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.ThingStateMetric;
import org.openhab.binding.prometheusexporter.internal.metrics.ThreadPoolMetric;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * The {@link PrometheusExporterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class PrometheusExporterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PrometheusExporterHandler.class);

    private @Nullable PrometheusExporterConfiguration config;

    private AtomicBoolean metricsInitialized = new AtomicBoolean(false);

    private @Nullable ScheduledFuture<?> updaterJob;
    private BundleContext bundleContext;
    private ThingRegistry thingRegistry;
    private List<PrometheusMetric> metrics = new LinkedList<>();

    public PrometheusExporterHandler(Thing thing, BundleContext bundleContext, ThingRegistry thingRegistry) {
        super(thing);
        this.bundleContext = bundleContext;
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no supported commands yet
    }

    @Override
    public void initialize() {
        config = getConfigAs(PrometheusExporterConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                initializeMetrics();
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.warn("Failed to initialize Prometheus metrics", e);
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    synchronized private void initializeMetrics() {
        logger.debug("Initializing Prometheus metrics...");
        cancelExistingUpdateJob();
        if (metricsInitialized.get()) {
            return;
        }

        // TODO only start the metrics if a channel of the corresponding type is bound
        metrics.add(new HotspotMetric().startMeasurement());
        metrics.add(new InboxCountMetric(bundleContext).startMeasurement());
        metrics.add(new ThreadPoolMetric().startMeasurement());
        metrics.add(new EventCountMetric(bundleContext).startMeasurement());
        metrics.add(new BundleStateMetric(bundleContext).startMeasurement());
        metrics.add(new ThingStateMetric(thingRegistry).startMeasurement());
        updaterJob = scheduler.scheduleWithFixedDelay(this::updater, 5, 15, TimeUnit.SECONDS);
        metricsInitialized.set(true);
    }

    synchronized private void cleanupMetrics() {
        for (PrometheusMetric metric : metrics) {
            metric.stopMeasurement();
        }
        metrics.clear();
    }

    @Override
    public void dispose() {
        this.cleanupMetrics();
        cancelExistingUpdateJob();
        super.dispose();
    }

    private void cancelExistingUpdateJob() {
        if (updaterJob != null) {
            updaterJob.cancel(true);
            updaterJob = null;
        }
    }

    private void updater() {
        try {
            for (PrometheusMetric metric : metrics) {
                refreshMetricAndUpdateChannel(metric);
            }
            updateAllMetricsChannel();
        } catch (IOException ioe) {
            logger.error("Error collecting prometheus metrics: ", ioe);
        }
    }

    private void updateAllMetricsChannel() throws IOException {
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        String metrics = writer.toString();
        updateState(PrometheusExporterBindingConstants.CHANNEL_ALL, StringType.valueOf(metrics));
    }

    private void refreshMetricAndUpdateChannel(PrometheusMetric metric) throws IOException {
        metric.refresh();
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer,
                CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(metric.getMetricNames()));
        updateState(metric.getChannel(), StringType.valueOf(writer.toString()));
    }
}
