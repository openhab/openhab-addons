/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility for timing operations
 *
 * @author Sami Salonen - initial contribution
 *
 */
@NonNullByDefault
public class AggregateStopWatch {
    /**
     * ID associated with this modbus operation
     */
    final String operationId;

    /**
     * Total operation time
     */
    final SimpleStopWatch total = new SimpleStopWatch();

    /**
     * Time for connection related actions
     */
    final SimpleStopWatch connection = new SimpleStopWatch();

    /**
     * Time for actual the actual transaction (read/write to slave)
     */
    final SimpleStopWatch transaction = new SimpleStopWatch();

    /**
     * Time for calling calling the callback
     */
    final SimpleStopWatch callback = new SimpleStopWatch();

    public AggregateStopWatch() {
        this.operationId = UUID.randomUUID().toString();
    }

    /**
     * Suspend all running stopwatches of this aggregate
     */
    public void suspendAllRunning() {
        for (SimpleStopWatch watch : new SimpleStopWatch[] { total, connection, transaction, callback }) {
            if (watch.isRunning()) {
                watch.suspend();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("{total: %d ms, connection: %d, transaction=%d, callback=%d}", total.getTotalTimeMillis(),
                connection.getTotalTimeMillis(), transaction.getTotalTimeMillis(), callback.getTotalTimeMillis());
    }
}
