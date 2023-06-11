/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

/**
 * {@link MetricsExporter} provides the interface for components exporting metrics to push based monitoring systems.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public abstract class MetricsExporter {

    private final Logger logger = LoggerFactory.getLogger(MetricsExporter.class);
    private boolean active = false;
    protected @Nullable CompositeMeterRegistry meterRegistry;
    protected @Nullable MetricsConfiguration config;

    protected abstract void start(CompositeMeterRegistry meterRegistry, MetricsConfiguration metricsConfiguration);

    protected abstract void shutdown();

    protected abstract boolean isEnabled(MetricsConfiguration config);

    public void updateExporterState(@Nullable MetricsConfiguration config) {
        this.config = config;
        if (config != null && isEnabled(config) && meterRegistry != null) {
            if (!active) {
                logger.debug("Activating exporter {} ", this.getClass().getSimpleName());
                active = true;
                start(Objects.requireNonNull(meterRegistry), config);
            } else {
                logger.trace("Exporter {} already active.", this.getClass().getSimpleName());
            }
        } else {
            if (active) {
                logger.debug("Shutting down exporter {} ", this.getClass().getSimpleName());
                shutdown();
                active = false;
            } else {
                logger.trace("Exporter {} already shut down.", this.getClass().getSimpleName());
            }
        }
    }

    public void setMeterRegistry(@Nullable CompositeMeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        updateExporterState(config);
    }
}
