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
package org.openhab.binding.cbus.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;

import com.daveoxley.cbus.CGateThreadPool;
import com.daveoxley.cbus.CGateThreadPoolExecutor;

/**
 * The {@link CBusThreadPool} is responsible for executing jobs from a threadpool
 *
 * @author John Harvey - Initial contribution
 */
@NonNullByDefault
public class CBusThreadPool extends CGateThreadPool {

    private final Map<String, CGateThreadPoolExecutor> executorMap = new HashMap<>();

    @Override
    protected synchronized CGateThreadPoolExecutor CreateExecutor(@Nullable String name) {
        String nullSafeName = name == null || name.isEmpty() ? "_default" : name;
        CGateThreadPoolExecutor executor = executorMap.get(nullSafeName);
        if (executor == null) {
            executor = new CBusThreadPoolExecutor(nullSafeName);
            executorMap.put(nullSafeName, executor);
        }
        return executor;
    }

    public class CBusThreadPoolExecutor extends CGateThreadPoolExecutor {
        private final ExecutorService threadPool;

        public CBusThreadPoolExecutor(@Nullable String poolName) {
            threadPool = ThreadPoolManager.getPool("binding.cbus-" + poolName);
        }

        @Override
        protected void execute(@Nullable Runnable runnable) {
            if (runnable != null) {
                threadPool.execute(runnable);
            }
        }
    }
}
