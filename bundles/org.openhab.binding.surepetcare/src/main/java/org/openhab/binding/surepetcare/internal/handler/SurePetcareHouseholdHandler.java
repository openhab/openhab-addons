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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareHouseholdHandler} is responsible for handling things created to represent Sure Petcare
 * households.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcareHouseholdHandler extends SurePetcareBaseObjectHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareHouseholdHandler.class);

    public SurePetcareHouseholdHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    protected void updateThing() {
        SurePetcareHousehold household = petcareAPI.getHousehold(thing.getUID().getId());
        if (household != null) {
            logger.debug("Updating all thing channels for household : {}", household);
            updateState(HOUSEHOLD_CHANNEL_ID, new DecimalType(household.id));
            updateState(HOUSEHOLD_CHANNEL_NAME, new StringType(household.name));
            updateState(HOUSEHOLD_CHANNEL_TIMEZONE_ID, new DecimalType(household.timezoneId));
        } else {
            logger.debug("Trying to update unknown household: {}", thing.getUID().getId());
        }
    }
}
