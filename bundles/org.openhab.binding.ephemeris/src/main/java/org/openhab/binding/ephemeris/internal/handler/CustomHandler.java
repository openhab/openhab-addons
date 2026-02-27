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
package org.openhab.binding.ephemeris.internal.handler;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.configuration.FileConfiguration;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link CustomHandler} delivers user defined Jollyday definition events.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CustomHandler extends JollydayHandler {
    private @Nullable String path;

    public CustomHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing, ephemerisManager, zoneId, CHANNEL_EVENT_TODAY, CHANNEL_EVENT_TOMORROW);
    }

    @Override
    public void initialize() {
        super.initialize();
        String fileName = getConfigAs(FileConfiguration.class).fileName;

        if (fileName.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "'fileName' can not be blank or empty");
            return;
        }

        File file = new File(BINDING_DATA_PATH, fileName);
        if (!file.exists()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing file: %s".formatted(file.getAbsolutePath()));
            return;
        }

        path = file.getAbsolutePath();
    }

    @Override
    protected @Nullable String getEvent(ZonedDateTime day) {
        if (path instanceof String filePath) {
            try {
                return ephemeris.getBankHolidayName(day, filePath);
            } catch (IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Syntex error in %s".formatted(path));
            } catch (FileNotFoundException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Missing file: %s".formatted(path));
            }
            return null;
        } else {
            throw new IllegalArgumentException("path should not be null");
        }
    }
}
