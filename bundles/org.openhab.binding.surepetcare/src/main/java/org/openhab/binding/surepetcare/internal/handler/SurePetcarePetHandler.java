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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("DeviceHandler handleCommand called with command: {}", command.toString());

        if (command instanceof RefreshType) {
            updateThing();
        } else {
            switch (channelUID.getId()) {
                case SurePetcareConstants.PET_CHANNEL_LOCATION_ID:
                    String location = command.toFullString();
                    logger.debug("received location update command: {}", location);
                    if (command instanceof DecimalType) {
                        // binding specific logic goes here
                        // updateDeviceState(deviceSwitchState);
                        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
                        Integer newLocationId = ((DecimalType) command).intValue();
                        logger.debug("received new location: {}", newLocationId);
                        if (pet != null) {
                            try {
                                petcareAPI.setPetLocation(pet, newLocationId);
                            } catch (SurePetcareApiException e) {
                                logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                        newLocationId, pet.toString());
                            }
                        }
                    }
                    break;
                default:
                    logger.warn("Update on unsupported channel {}", channelUID.getId());
            }
        }
    }

    @Override
    public void updateThing() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            logger.debug("updating all thing channels for pet : {}", pet.toString());
            updateState(SurePetcareConstants.PET_CHANNEL_ID, new DecimalType(pet.getId()));
            updateState(SurePetcareConstants.PET_CHANNEL_NAME, new StringType(pet.getName()));
            updateState(SurePetcareConstants.PET_CHANNEL_COMMENT, new StringType(pet.getComments()));
            updateState(SurePetcareConstants.PET_CHANNEL_GENDER_ID, new DecimalType(pet.getGenderId()));
            updateState(SurePetcareConstants.PET_CHANNEL_BREED_ID, new DecimalType(pet.getBreedId()));
            updateState(SurePetcareConstants.PET_CHANNEL_SPECIES_ID, new DecimalType(pet.getSpeciesId()));
            updateState(SurePetcareConstants.PET_CHANNEL_PHOTO_URL, new StringType(pet.getPhoto().getLocation()));
            updateState(SurePetcareConstants.PET_CHANNEL_LOCATION_ID, new DecimalType(pet.getLocation().getWhere()));
            updateState(SurePetcareConstants.PET_CHANNEL_LOCATION_CHANGED,
                    new DateTimeType(pet.getLocation().getLocationChanged()));
        }
    }

    public void updatePetLocation() {
        SurePetcarePet pet = petcareAPI.retrievePet(thing.getUID().getId());
        if (pet != null) {
            updateState(SurePetcareConstants.PET_CHANNEL_LOCATION_ID, new DecimalType(pet.getLocation().getWhere()));
            updateState(SurePetcareConstants.PET_CHANNEL_LOCATION_CHANGED,
                    new DateTimeType(pet.getLocation().getLocationChanged()));
        }
    }
}
