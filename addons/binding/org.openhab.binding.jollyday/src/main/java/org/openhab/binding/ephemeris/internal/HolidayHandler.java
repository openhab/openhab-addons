/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ephemeris.internal;

import static org.openhab.binding.ephemeris.EphemerisBindingConstants.*;

import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;

import de.jollyday.Holiday;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;

/**
 * Handler for a Holiday event thing.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class HolidayHandler extends EphemerisHandler {

    public HolidayHandler(Thing thing, Locale systemLocale) {
        super(thing, systemLocale);
    }

    @Override
    public ManagerParameter getManagagerParameter() {
        return ManagerParameters.create(getCountry());
    }

    @Override
    public void handleUpdate(Set<Holiday> holidays) {
        boolean isOfficial = false;
        if (!holidays.isEmpty()) {
            Holiday holiday = holidays.iterator().next();
            String eventName = holiday.getDescription(systemLocale);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_EVENT_NAME), new StringType(eventName));
        } else {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_EVENT_NAME), UnDefType.NULL);
        }
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_EVENT_OFFICIAL),
                isOfficial ? OnOffType.ON : OnOffType.OFF);
    }

}
