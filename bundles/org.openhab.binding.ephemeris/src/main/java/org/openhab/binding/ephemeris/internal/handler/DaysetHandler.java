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

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ephemeris.internal.EphemerisException;
import org.openhab.binding.ephemeris.internal.configuration.DaysetConfiguration;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;

/**
 * The {@link DaysetHandler} delivers system default dayset data.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class DaysetHandler extends BaseEphemerisHandler {
    private String dayset = "";

    public DaysetHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing, ephemerisManager, zoneId);
    }

    @Override
    public void initialize() {
        DaysetConfiguration config = getConfigAs(DaysetConfiguration.class);
        dayset = config.name;
        super.initialize();
    }

    @Override
    protected void internalUpdate(ZonedDateTime today) throws EphemerisException {
        updateState(CHANNEL_TODAY, OnOffType.from(ephemeris.isInDayset(dayset, today)));
        updateState(CHANNEL_TOMORROW, OnOffType.from(ephemeris.isInDayset(dayset, today.plusDays(1))));
    }
}
