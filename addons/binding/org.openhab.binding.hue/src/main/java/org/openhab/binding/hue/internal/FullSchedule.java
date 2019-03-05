/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import java.util.Date;

/**
 * Detailed schedule information.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullSchedule extends Schedule {
    private String description;
    private ScheduleCommand command; // Not really appropriate for exposure
    private Date time;

    /**
     * Returns the description of the schedule.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the scheduled command.
     *
     * @return command
     */
    public ScheduleCommand getCommand() {
        return command;
    }

    /**
     * Returns the time for which the command is scheduled to be ran.
     *
     * @return scheduled time
     */
    public Date getTime() {
        return time;
    }
}
