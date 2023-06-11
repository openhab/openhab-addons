/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
