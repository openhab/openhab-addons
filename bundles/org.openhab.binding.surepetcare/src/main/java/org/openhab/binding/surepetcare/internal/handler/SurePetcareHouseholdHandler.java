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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
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
            updateState(SurePetcareConstants.HOUSEHOLD_CHANNEL_ID, new DecimalType(household.getId()));
            updateState(SurePetcareConstants.HOUSEHOLD_CHANNEL_NAME, new StringType(household.getName()));
            updateState(SurePetcareConstants.HOUSEHOLD_CHANNEL_TIMEZONE,
                    new StringType(household.getTimezone().timezone));
            updateState(SurePetcareConstants.HOUSEHOLD_CHANNEL_TIMEZONE_UTC_OFFSET,
                    new DecimalType(household.getTimezone().utcOffset));
        }
    }

}
