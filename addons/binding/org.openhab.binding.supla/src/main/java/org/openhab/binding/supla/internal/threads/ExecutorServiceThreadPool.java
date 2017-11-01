/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.threads;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class ExecutorServiceThreadPool implements ThreadPool {
    private final ExecutorService executorService;

    public ExecutorServiceThreadPool(ExecutorService executorService) {
        this.executorService = requireNonNull(executorService);
    }

    @Override
    public void submit(Runnable runnable) {
        executorService.submit(runnable);
    }
}
