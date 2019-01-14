/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.internal.stick;

/**
 * @author Volker Bier - Initial contribution
 */
public class TimedCommand extends Command {
    private int duration;

    public TimedCommand(CommandType cmd, int cmdDuration, Integer[] channels) {
        super(cmd, channels);

        duration = cmdDuration;
    }

    public int getDuration() {
        return duration;
    }
}
