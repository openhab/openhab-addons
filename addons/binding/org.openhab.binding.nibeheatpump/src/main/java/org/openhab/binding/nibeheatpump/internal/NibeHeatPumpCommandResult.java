/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;

/**
 * The {@link NibeHeatPumpCommandResult} implements a very simple {@link Future} for {@link NibeHeatPumpMessage}s.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpCommandResult implements Future<NibeHeatPumpMessage> {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private NibeHeatPumpMessage result = null;
    private boolean done = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        lock.lock();
        try {
            return done;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public NibeHeatPumpMessage get() throws InterruptedException {
        lock.lock();
        try {
            if (!done) {
                condition.await();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NibeHeatPumpMessage get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if (!done) {
                final boolean timedOut = !condition.await(timeout, unit);
                if (timedOut) {
                    throw new TimeoutException("waiting timed out");
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void set(final NibeHeatPumpMessage result) {
        lock.lock();
        try {
            this.result = result;
            this.done = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
