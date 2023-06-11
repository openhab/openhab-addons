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
package org.openhab.binding.hue.internal.dto;

import java.util.Date;

/**
 * Collection of updates to a schedule.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author Samuel Leisering - refactor configuration updates
 */
public class ScheduleUpdate extends ConfigUpdate {

    /**
     * Set the name of the schedule.
     *
     * @param name new name
     * @return this object for chaining calls
     */
    public ScheduleUpdate setName(String name) {
        if (Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Schedule name can be at most 32 characters long");
        }

        commands.add(new Command("name", name));
        return this;
    }

    /**
     * Set the description of the schedule.
     *
     * @param description new description
     * @return this object for chaining calls
     */
    public ScheduleUpdate setDescription(String description) {
        if (Util.stringSize(description) > 64) {
            throw new IllegalArgumentException("Schedule description can be at most 64 characters long");
        }

        commands.add(new Command("description", description));
        return this;
    }

    /**
     * Set the time of the schedule.
     *
     * @param time new time
     * @return this object for chaining calls
     */
    public ScheduleUpdate setTime(Date time) {
        commands.add(new Command("time", time));
        return this;
    }
}
