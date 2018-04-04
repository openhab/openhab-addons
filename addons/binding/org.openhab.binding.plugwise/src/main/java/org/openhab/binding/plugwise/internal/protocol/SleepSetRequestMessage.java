/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.SLEEP_SET_REQUEST;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Sets when sleeping end devices (Scan, Sense, Switch) sleep and wake-up .
 *
 * @author Wouter Born - Initial contribution
 */
public class SleepSetRequestMessage extends Message {

    private static final Duration DEFAULT_SLEEP_DURATION = Duration.ofSeconds(5);

    private Duration wakeupDuration;
    private Duration sleepDuration;
    private Duration wakeupInterval;
    private int unknown;

    public SleepSetRequestMessage(MACAddress macAddress, Duration wakeupDuration, Duration sleepDuration,
            Duration wakeupInterval) {
        super(SLEEP_SET_REQUEST, macAddress);
        this.wakeupDuration = wakeupDuration;
        this.sleepDuration = sleepDuration;
        this.wakeupInterval = wakeupInterval;
    }

    public SleepSetRequestMessage(MACAddress macAddress, Duration wakeupDuration, Duration wakeupInterval) {
        this(macAddress, wakeupDuration, DEFAULT_SLEEP_DURATION, wakeupInterval);
    }

    @Override
    protected String payloadToHexString() {
        String wakeupDurationHex = String.format("%02X", wakeupDuration.getSeconds());
        String sleepDurationHex = String.format("%04X", sleepDuration.getSeconds());
        String wakeupIntervalHex = String.format("%04X", wakeupInterval.toMinutes());
        String unknownHex = String.format("%06X", unknown);
        return wakeupDurationHex + sleepDurationHex + wakeupIntervalHex + unknownHex;
    }

}
