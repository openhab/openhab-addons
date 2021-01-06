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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EventCountMetric} class implements a gauge metric for the openHAB events from the smarthome
 * topic.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class EventCountMetric implements PrometheusMetric {

    private final Logger logger = LoggerFactory.getLogger(EventCountMetric.class);
    public static final String METRIC_NAME = "event_count";
    private final BundleContext bundleContext;

    public EventCountMetric(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        @Nullable
        MonitoringEventSubscriber monitoringEventSubscriber = getMonitoringEventSubscriber();
        if (monitoringEventSubscriber != null) {
            monitoringEventSubscriber.startMeasurement();
        }
        return this;
    }

    private @Nullable MonitoringEventSubscriber getMonitoringEventSubscriber() {
        ServiceReference<MonitoringEventSubscriber> serviceReference = bundleContext
                .getServiceReference(MonitoringEventSubscriber.class);
        if (serviceReference != null) {
            MonitoringEventSubscriber monitoringEventSubscriber = bundleContext.getService(serviceReference);
            logger.debug("Retrieved monitoringEventSubscriber handle.");
            return monitoringEventSubscriber;
        }
        return null;
    }

    @Override
    public void refresh() {
        // already handled internally
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        @Nullable
        MonitoringEventSubscriber monitoringEventSubscriber = getMonitoringEventSubscriber();
        if (monitoringEventSubscriber != null) {
            monitoringEventSubscriber.stopMeasurement();
        }
        return this;
    }

    @Override
    public Set<String> getMetricNames() {
        return Set.of(METRIC_NAME);
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_EVENT_COUNT;
    }
}
