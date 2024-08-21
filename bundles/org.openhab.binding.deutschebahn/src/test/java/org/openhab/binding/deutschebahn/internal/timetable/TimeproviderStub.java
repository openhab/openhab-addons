/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stub time provider.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public final class TimeproviderStub implements Supplier<Date> {

    public GregorianCalendar time = new GregorianCalendar();

    @Override
    public Date get() {
        return this.time.getTime();
    }

    public void moveAhead(int seconds) {
        this.time.set(Calendar.SECOND, time.get(Calendar.SECOND) + seconds);
    }
}
