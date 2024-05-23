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
package org.openhab.binding.ephemeris.internal.handler;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.io.*;
import java.time.*;
import java.util.Optional;

import org.eclipse.jdt.annotation.*;
import org.openhab.binding.ephemeris.internal.configuration.FileConfiguration;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;

/**
 * The {@link CustomHandler} delivers user defined Jollyday definition events.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CustomHandler extends JollydayHandler {
    private Optional<File> definitionFile = Optional.empty();

    public CustomHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing, ephemerisManager, zoneId);
    }

    @Override
    public void initialize() {
        FileConfiguration config = getConfigAs(FileConfiguration.class);
        definitionFile = Optional.of(new File(BINDING_DATA_PATH, config.fileName));
        super.initialize();
    }

    @Override
    protected @Nullable String internalUpdate(ZonedDateTime today) {
        if (definitionFile.isPresent()) {
            File file = definitionFile.get();
            if (file.exists()) {
                String event = getEvent(today);
                updateState(CHANNEL_EVENT_TODAY, OnOffType.from(event != null));

                event = getEvent(today.plusDays(1));
                updateState(CHANNEL_EVENT_TOMORROW, OnOffType.from(event != null));
                return super.internalUpdate(today);
            }
            return "Missing file: %s".formatted(file.getAbsolutePath());
        }
        throw new IllegalArgumentException("Initialization problem, please file a bug.");
    }

    @Override
    protected @Nullable String getEvent(ZonedDateTime day) {
        String path = definitionFile.get().getAbsolutePath();
        try {
            return ephemeris.getBankHolidayName(day, path);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Incorrect syntax");
        } catch (FileNotFoundException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "File is absent: " + path);
        }
        return null;
    }
}
