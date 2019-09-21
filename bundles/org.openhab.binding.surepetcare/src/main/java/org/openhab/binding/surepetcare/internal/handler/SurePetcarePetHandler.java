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
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcarePetHandler} is responsible for handling the things created to represent Sure Petcare pets.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcarePetHandler extends SurePetcareBaseObjectHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcarePetHandler.class);

    public SurePetcarePetHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    public void updateThing() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            logger.debug("updating all thing channels for pet : {}", pet.toString());
            updateState("id", new DecimalType(pet.getId()));
            updateState("name", new StringType(pet.getName()));
            updateState("comment", new StringType(pet.getComments()));
            updateState("gender", new StringType(pet.getGenderName()));
            updateState("breed", new StringType(pet.getBreedName()));
            updateState("species", new StringType(pet.getSpeciesName()));
            updateState("photoURL", new StringType(pet.getPhoto().getLocation()));
            updateState("location", new StringType(pet.getLocation().getLocationName()));
            updateState("locationChanged", new DateTimeType(pet.getLocation().getLocationChanged()));
        }
    }

    public void updatePetLocation() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            updateState("location", new StringType(pet.getLocation().getLocationName()));
            updateState("locationChanged", new DateTimeType(pet.getLocation().getLocationChanged()));
        }
    }
}
