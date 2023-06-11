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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Volker Bier - Initial contribution
 */
public class DelayedCommand extends Command {
    private final long origin;
    private final long delay;

    public DelayedCommand(CommandType cmd, long delayInMillis, int priority, Integer... channels) {
        super(cmd, priority, channels);

        delay = delayInMillis;
        origin = System.currentTimeMillis();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public int compareTo(Delayed delayed) {
        if (delayed == this) {
            return 0;
        }

        return Long.compare(getDelay(TimeUnit.MILLISECONDS), delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delay - (System.currentTimeMillis() - origin), TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return super.toString() + " and delay " + getDelay(TimeUnit.MILLISECONDS);
    }
}
