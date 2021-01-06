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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.VersionInfoExports;

/**
 * The {@link HotspotMetric} class holds several JVM related metrics.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class HotspotMetric implements PrometheusMetric {

    @Nullable
    private StandardExports standardExportsCollector = null;
    @Nullable
    private MemoryPoolsExports memoryPoolsExportsCollector = null;
    @Nullable
    private MemoryAllocationExports memoryAllocationExportsCollector = null;
    @Nullable
    private BufferPoolsExports bufferPoolsExportsCollector = null;
    @Nullable
    private GarbageCollectorExports garbageCollectorExportsCollector = null;
    @Nullable
    private ThreadExports threadExportsCollector = null;
    @Nullable
    private ClassLoadingExports classLoadingExportsCollector = null;
    @Nullable
    private VersionInfoExports versionInfoExportsCollector = null;

    @Override
    public synchronized PrometheusMetric startMeasurement() {
        standardExportsCollector = new StandardExports();
        memoryPoolsExportsCollector = new MemoryPoolsExports();
        memoryAllocationExportsCollector = new MemoryAllocationExports();
        bufferPoolsExportsCollector = new BufferPoolsExports();
        garbageCollectorExportsCollector = new GarbageCollectorExports();
        threadExportsCollector = new ThreadExports();
        classLoadingExportsCollector = new ClassLoadingExports();
        versionInfoExportsCollector = new VersionInfoExports();
        standardExportsCollector.register();
        memoryPoolsExportsCollector.register();
        memoryAllocationExportsCollector.register();
        bufferPoolsExportsCollector.register();
        garbageCollectorExportsCollector.register();
        threadExportsCollector.register();
        classLoadingExportsCollector.register();
        versionInfoExportsCollector.register();
        return this;
    }

    @Override
    public void refresh() {
        // handled internally
    }

    @Override
    public synchronized PrometheusMetric stopMeasurement() {
        CollectorRegistry.defaultRegistry.unregister(standardExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(memoryPoolsExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(memoryAllocationExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(bufferPoolsExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(garbageCollectorExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(threadExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(classLoadingExportsCollector);
        CollectorRegistry.defaultRegistry.unregister(versionInfoExportsCollector);
        return this;
    }

    @Override
    public Set<String> getMetricNames() {
        return Set.of("process_cpu_seconds_total", "process_start_time_seconds", "process_open_fds", "process_max_fds",
                "process_virtual_memory_bytes", "process_resident_memory_bytes", "jvm_memory_bytes_used",
                "jvm_memory_bytes_committed", "jvm_memory_bytes_max", "jvm_memory_bytes_init",
                "jvm_memory_pool_bytes_used", "jvm_memory_pool_bytes_committed", "jvm_memory_pool_bytes_max",
                "jvm_memory_pool_bytes_init", "jvm_memory_pool_allocated_bytes_total", "jvm_buffer_pool_used_bytes",
                "jvm_buffer_pool_capacity_bytes", "jvm_buffer_pool_used_buffers", "jvm_gc_collection_seconds",
                "jvm_threads_current", "jvm_threads_daemon", "jvm_threads_peak", "jvm_threads_started_total",
                "jvm_threads_deadlocked", "jvm_threads_deadlocked_monitor", "jvm_threads_state", "jvm_classes_loaded",
                "jvm_classes_loaded_total", "jvm_classes_unloaded_total", "jvm_info");
    }

    @Override
    public String getChannel() {
        return PrometheusExporterBindingConstants.CHANNEL_JVM;
    }
}
