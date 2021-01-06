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
import org.openhab.core.config.discovery.inbox.Inbox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * The {@link InboxCountMetric} class implements a Gauge metric for the openHAB inbox count.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class InboxCountMetric implements PrometheusMetric {
    public static final String METRIC_NAME = "openhab_inbox_count";
    private final Logger logger = LoggerFactory.getLogger(InboxCountMetric.class);

    private final BundleContext bundleContext;
    @Nullable
    private Inbox inbox = null;
    @Nullable
    private ServiceReference<Inbox> serviceReference = null;
    @Nullable
    private static Gauge inboxCountGauge = null;

    public InboxCountMetric(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        ServiceReference<Inbox> serviceReference = bundleContext.getServiceReference(Inbox.class);
        if (serviceReference != null) {
            this.inbox = bundleContext.getService(serviceReference);
            logger.debug("Retrieved Inbox handle.");
        }
        if (inboxCountGauge == null) {
            inboxCountGauge = Gauge.build(METRIC_NAME, "openHAB inbox count").register();
        }
        return this;
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        if (serviceReference != null) {
            bundleContext.ungetService(serviceReference);
            this.serviceReference = null;
            this.inbox = null;
        }
        if (inboxCountGauge != null) {
            CollectorRegistry.defaultRegistry.unregister(inboxCountGauge);
            inboxCountGauge = null;
        }
        return this;
    }

    @Override
    public Set<String> getMetricNames() {
        return Set.of(METRIC_NAME);
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_INBOX_COUNT;
    }

    @Override
    public void refresh() {
        if (inbox == null || inboxCountGauge == null) {
            logger.debug("Skipping refresh request since measurement has not been started.");
            return;
        }
        Gauge.Child child = new Gauge.Child();
        int inboxCount = inbox.getAll().size();
        logger.trace("Setting inbox count metric to {}", inboxCount);
        child.set(inboxCount);
        inboxCountGauge.setChild(child);
    }
}
