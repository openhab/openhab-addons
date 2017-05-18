/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.factory;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

public class KNXThreadPoolFactory {

    protected static final long THREAD_TIMEOUT = 65L;
    protected static final long THREAD_MONITOR_SLEEP = 60000;

    protected static Map<String, ExecutorService> pools = new WeakHashMap<>();

    public static ScheduledExecutorService getPrioritizedScheduledPool(String poolName, int coreThreads) {
        ExecutorService pool = pools.get(poolName);
        if (pool == null) {
            synchronized (pools) {
                // do a double check if it is still null or if another thread might have created it meanwhile
                pool = pools.get(poolName);
                if (pool == null && coreThreads > 1) {
                    pool = Executors.newScheduledThreadPool(coreThreads,
                            new NamedThreadFactory(poolName, Thread.MAX_PRIORITY));
                    ((ThreadPoolExecutor) pool).setKeepAliveTime(THREAD_TIMEOUT, TimeUnit.SECONDS);
                    ((ThreadPoolExecutor) pool).allowCoreThreadTimeOut(true);
                    pools.put(poolName, pool);
                    LoggerFactory.getLogger(KNXThreadPoolFactory.class).debug(
                            "Created scheduled thread pool '{}' of size {}", new Object[] { poolName, coreThreads });
                }
            }
        }
        if (pool instanceof ScheduledExecutorService) {
            if (coreThreads > 1) {
                LoggerFactory.getLogger(KNXThreadPoolFactory.class).debug(
                        "Updated the scheduled thread pool '{}' to a size of {}",
                        new Object[] { poolName, coreThreads });
                ((ThreadPoolExecutor) pool).setCorePoolSize(coreThreads);
            }
            return (ScheduledExecutorService) pool;
        } else {
            throw new IllegalArgumentException("Pool " + poolName + " is not a scheduled pool!");
        }
    }

    protected static class NamedThreadFactory implements ThreadFactory {

        protected final ThreadGroup group;
        protected final AtomicInteger threadNumber = new AtomicInteger(1);
        protected final String namePrefix;
        protected final String name;
        protected final int priority;

        public NamedThreadFactory(String threadPool, int priority) {
            this.name = threadPool;
            this.namePrefix = "ESH-" + threadPool + "-";
            this.priority = priority;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != priority) {
                t.setPriority(priority);
            }

            return t;
        }

        public String getName() {
            return name;
        }
    }

}
