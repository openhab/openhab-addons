/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
