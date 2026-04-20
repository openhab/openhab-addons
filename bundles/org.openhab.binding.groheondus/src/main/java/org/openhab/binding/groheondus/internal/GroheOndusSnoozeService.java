/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.github.floriansw.ondus.api.OndusService;

/**
 * Service for controlling Sense Guard pause/snooze functionality via the Grohe API.
 *
 * @author Michael Parment - Initial contribution
 */
@NonNullByDefault
public class GroheOndusSnoozeService {
    private final OndusService ondusService;

    public GroheOndusSnoozeService(OndusService ondusService) {
        this.ondusService = ondusService;
    }

    /**
     * Check if snooze/pause is currently active for the device
     */
    public boolean isSnoozeActive(String locationId, String roomId, String applianceId) throws IOException {
        return ondusService.applianceCommand(locationId, roomId, applianceId).isPresent();
    }

    /**
     * Set pause duration in minutes
     */
    public void setPause(String locationId, String roomId, String applianceId, int minutes) throws IOException {
        ondusService.setSnooze(locationId, roomId, applianceId, minutes);
    }

    /**
     * Delete/cancel the pause
     */
    public void deletePause(String locationId, String roomId, String applianceId) throws IOException {
        ondusService.deleteSnooze(locationId, roomId, applianceId);
    }
}
