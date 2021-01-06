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

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The {@link ThreadPoolGaugeInfo} enum holds Gauges for thread pool figures to be used in the
 * {@link ThreadPoolMetric}
 * class.
 *
 * @author Robert Bach - Initial contribution
 */
enum ThreadPoolGaugeInfo {
    OPENHAB_ACTIVE_THREADS_COUNT("openhab_pool_threads_count_active",
            "approximate number of threads that are actively executing tasks", e -> Long.valueOf(e.getActiveCount())),
    OPENHAB_THREAD_POOL_SIZE("openhab_pool_threads_count_current", "current number of threads in the pool",
            e -> Long.valueOf(e.getPoolSize())),
    OPENHAB_THREAD_POOL_COMPLETED_TASK_COUNT("openhab_pool_tasks_count_completed",
            "approximate total number of tasks that have completed execution",
            ThreadPoolExecutor::getCompletedTaskCount),
    OPENHAB_LARGEST_THREAD_POOL_SIZE("openhab_pool_threads_count_largest",
            "largest number of threads that have ever simultaneously been in the pool",
            e -> Long.valueOf(e.getLargestPoolSize())),
    OPENHAB_MAXIMUN_THREAD_POOL_SIZE("openhab_pool_size_max", "maximum allowed number of threads in the pool",
            e -> Long.valueOf(e.getMaximumPoolSize())),
    OPENHAB_THREAD_POOL_TASK_COUNT("openhab_pool_tasks_count_total",
            "total number of tasks that have ever been scheduled for execution", ThreadPoolExecutor::getTaskCount),
    OPENHAB_CORE_POOL_SIZE("openhab_pool_size", "regular number of threads in the pool",
            e -> Long.valueOf(e.getCorePoolSize())),
    OPENHAB_THREAD_POOL_QUEUE_SIZE("openhab_pool_queue_count",
            "the number of tasks waiting to get executed by a thread", e -> Long.valueOf(e.getQueue().size())),
    OPENHAB_KEEP_ALIVE_TIME("openhab_pool_keepalive_time_seconds",
            "the amount of time that threads in excess of the regular pool size may remain idle before being terminated",
            e -> e.getKeepAliveTime(TimeUnit.SECONDS));

    private final String gaugeName;
    private final String description;
    private final Function<ThreadPoolExecutor, Long> extractor;

    ThreadPoolGaugeInfo(String gaugeName, String description, Function<ThreadPoolExecutor, Long> extractor) {
        this.extractor = extractor;
        this.gaugeName = gaugeName;
        this.description = description;
    }

    public String getGaugeName() {
        return gaugeName;
    }

    public String getDescription() {
        return description;
    }

    public Function<ThreadPoolExecutor, Long> getExtractor() {
        return extractor;
    }
}
