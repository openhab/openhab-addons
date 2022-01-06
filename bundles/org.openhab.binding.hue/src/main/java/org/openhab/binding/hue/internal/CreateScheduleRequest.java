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
package org.openhab.binding.hue.internal;

import java.util.Date;

/**
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
class CreateScheduleRequest {
    private String name;
    private String description;
    private ScheduleCommand command;
    private Date time;

    public CreateScheduleRequest(String name, String description, ScheduleCommand command, Date time) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Schedule name can be at most 32 characters long");
        }

        if (description != null && Util.stringSize(description) > 64) {
            throw new IllegalArgumentException("Schedule description can be at most 64 characters long");
        }

        if (command == null) {
            throw new IllegalArgumentException("No schedule command specified");
        }

        this.name = name;
        this.description = description;
        this.command = command;
        this.time = time;
    }
}
