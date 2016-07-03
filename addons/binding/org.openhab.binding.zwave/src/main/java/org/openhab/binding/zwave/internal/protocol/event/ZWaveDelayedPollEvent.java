/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

import java.util.concurrent.TimeUnit;

/**
 * ZWaveEvent to notify listeners that a node needs to be polled
 * for its value at some future time.
 * 
 * @author Dan Cunningham
 *
 */
public class ZWaveDelayedPollEvent extends ZWaveEvent {

    private long delay;
    private TimeUnit unit;

    public ZWaveDelayedPollEvent(int nodeId, int endpoint, long delay, TimeUnit unit) {
        super(nodeId, endpoint);
        this.delay = delay;
        this.unit = unit;
    }

    public ZWaveDelayedPollEvent(int nodeId, long delay, TimeUnit unit) {
        super(nodeId);
        this.delay = delay;
        this.unit = unit;
    }

    public long getDelay() {
        return delay;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
