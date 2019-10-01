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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.data.SurePetcareHousehold;

/**
 * The {@link SurePetcareHouseholdHandler} is responsible for handling things created to represent Sure Petcare
 * households.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcareHouseholdHandler extends SurePetcareBaseObjectHandler {

    // private final Logger logger = LoggerFactory.getLogger(SurePetcareHouseholdHandler.class);

    public SurePetcareHouseholdHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    public void updateThing() {
        SurePetcareHousehold household = petcareAPI.retrieveHousehold(thing.getUID().getId());
        if (household != null) {
            updateState(HOUSEHOLD_CHANNEL_ID, new DecimalType(household.getId()));
            updateState(HOUSEHOLD_CHANNEL_NAME, new StringType(household.getName()));
            updateState(HOUSEHOLD_CHANNEL_TIMEZONE_ID, new DecimalType(household.getTimezoneId()));
            updateState(HOUSEHOLD_CHANNEL_CREATED_AT, new DateTimeType(household.getCreatedAtAsZonedDateTime()));
            updateState(HOUSEHOLD_CHANNEL_UPDATED_AT, new DateTimeType(household.getUpdatedAtAsZonedDateTime()));
        }
    }

}
