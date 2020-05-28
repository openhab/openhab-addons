/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.action.internal.util;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class Timeslot {
    private final long timeslotStart;
    private final IntStatCounter statCounter;

    public Timeslot(long startTime, Timeslot previousTimeslot) {
        this(startTime);

        if (previousTimeslot != null && previousTimeslot.getStatCounter().hasValue()) {
            statCounter.add(previousTimeslot.getStatCounter().getLast());
        }
    }

    public Timeslot(long startTime) {
        this.timeslotStart = startTime;
        this.statCounter = new IntStatCounter();
    }

    public void add(int value) {
        statCounter.add(value);
    }

    public IntStatCounter getStatCounter() {
        return statCounter;
    }

    public long getStartTime() {
        return timeslotStart;
    }
}
