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
package org.openhab.binding.mybmw.internal.handler.enums;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_AIR_CONDITIONING_START;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_AIR_CONDITIONING_STOP;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_DOOR_LOCK;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_DOOR_UNLOCK;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_HORN;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_LIGHT_FLASH;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_START_CHARGING;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_STOP_CHARGING;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.REMOTE_SERVICE_VEHICLE_FINDER;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * possible remote services
 *
 * @author Martin Grassl - initial contribution
 * @author Mark Herwege - electric charging commands
 */
@NonNullByDefault
public enum RemoteService {
    LIGHT_FLASH("Flash Lights", REMOTE_SERVICE_LIGHT_FLASH, REMOTE_SERVICE_LIGHT_FLASH, ""),
    VEHICLE_FINDER("Vehicle Finder", REMOTE_SERVICE_VEHICLE_FINDER, REMOTE_SERVICE_VEHICLE_FINDER, ""),
    DOOR_LOCK("Door Lock", REMOTE_SERVICE_DOOR_LOCK, REMOTE_SERVICE_DOOR_LOCK, ""),
    DOOR_UNLOCK("Door Unlock", REMOTE_SERVICE_DOOR_UNLOCK, REMOTE_SERVICE_DOOR_UNLOCK, ""),
    HORN_BLOW("Horn Blow", REMOTE_SERVICE_HORN, REMOTE_SERVICE_HORN, ""),
    CLIMATE_NOW_START("Start Climate", REMOTE_SERVICE_AIR_CONDITIONING_START, "climate-now", "{\"action\": \"START\"}"),
    CLIMATE_NOW_STOP("Stop Climate", REMOTE_SERVICE_AIR_CONDITIONING_STOP, "climate-now", "{\"action\": \"STOP\"}"),
    START_CHARGING("Start Charging", REMOTE_SERVICE_START_CHARGING, "start-charging", ""),
    STOP_CHARGING("Stop Charging", REMOTE_SERVICE_STOP_CHARGING, "stop-charging", "");

    private final String label;
    private final String id;
    private final String command;
    private final String body;

    RemoteService(final String label, final String id, String command, String body) {
        this.label = label;
        this.id = id;
        this.command = command;
        this.body = body;
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String getBody() {
        return body;
    }
}
