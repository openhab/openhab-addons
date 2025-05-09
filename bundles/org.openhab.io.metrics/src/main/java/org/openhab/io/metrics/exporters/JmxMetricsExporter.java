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
package org.openhab.io.metrics.exporters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.metrics.MetricsConfiguration;
import org.openhab.io.metrics.MetricsExporter;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;

/**
 * The {@link JmxMetricsExporter} class implements a MetricsExporter for Java Management Extensions (JMX).
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class JmxMetricsExporter extends MetricsExporter {

    private @Nullable JmxMeterRegistry jmxMeterRegistry;
    private @Nullable CompositeMeterRegistry meterRegistry;

    @Override
    public void start(CompositeMeterRegistry meterRegistry, MetricsConfiguration metricsConfiguration) {
        jmxMeterRegistry = new JmxMeterRegistry(getJmxConfig(), Clock.SYSTEM);
        meterRegistry.add(jmxMeterRegistry);
    }

    @Override
    public void shutdown() {
        JmxMeterRegistry jmxMeterRegistry = this.jmxMeterRegistry;
        if (jmxMeterRegistry != null) {
            jmxMeterRegistry.stop();
            this.jmxMeterRegistry = null;
        }

        CompositeMeterRegistry meterRegistry = this.meterRegistry;
        if (meterRegistry != null) {
            meterRegistry.remove(jmxMeterRegistry);
            this.meterRegistry = null;
        }
    }

    private JmxConfig getJmxConfig() {
        return new JmxConfig() {
            @Override
            @Nullable
            public String get(@Nullable String k) {
                return null; // accept the rest of the defaults
            }
        };
    }

    @Override
    protected boolean isEnabled(MetricsConfiguration config) {
        return config.jmxMetricsEnabled;
    }
}
