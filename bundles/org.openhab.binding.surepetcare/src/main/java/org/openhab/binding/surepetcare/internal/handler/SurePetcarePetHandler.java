/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.quantity.Mass;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetActivity;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetFeeding;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareTag;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcarePetHandler} is responsible for handling the things created to represent Sure Petcare pets.
 *
 * @author Rene Scherer - Initial Contribution
 * @author Holger Eisold - Added pet feeder status, location time offset
 */
@NonNullByDefault
public class SurePetcarePetHandler extends SurePetcareBaseObjectHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcarePetHandler.class);

    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.surepetcare");

    public SurePetcarePetHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        } else {
            switch (channelUID.getId()) {
                case PET_CHANNEL_LOCATION:
                    logger.debug("Received location update command: {}", command.toString());
                    if (command instanceof StringType commandAsStringType) {
                        synchronized (petcareAPI) {
                            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
                            if (pet != null) {
                                String newLocationIdStr = commandAsStringType.toString();
                                try {
                                    Integer newLocationId = Integer.valueOf(newLocationIdStr);
                                    // Only update if location has changed. (Needed for Group:Switch item)
                                    if ((pet.status.activity.where.equals(newLocationId)) || newLocationId.equals(0)) {
                                        logger.debug("Location has not changed, skip pet id: {} with loc id: {}",
                                                pet.id, newLocationId);
                                    } else {
                                        logger.debug("Received new location: {}", newLocationId);
                                        petcareAPI.setPetLocation(pet, newLocationId, ZonedDateTime.now());
                                        updateState(PET_CHANNEL_LOCATION,
                                                new StringType(pet.status.activity.where.toString()));
                                        updateState(PET_CHANNEL_LOCATION_CHANGED,
                                                new DateTimeType(pet.status.activity.since));
                                    }
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid location id: {}, ignoring command", newLocationIdStr, e);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                            newLocationIdStr, pet, e);
                                }
                            }
                        }
                    }
                    break;
                case PET_CHANNEL_LOCATION_TIMEOFFSET:
                    logger.debug("Received location time offset update command: {}", command.toString());
                    if (command instanceof StringType commandAsStringType) {
                        synchronized (petcareAPI) {
                            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
                            if (pet != null) {
                                String commandIdStr = commandAsStringType.toString();
                                try {
                                    Integer commandId = Integer.valueOf(commandIdStr);
                                    Integer currentLocation = pet.status.activity.where;
                                    logger.debug("Received new location: {}", currentLocation == 1 ? 2 : 1);
                                    // We set the location to the opposite state.
                                    // We also set location to INSIDE (1) if currentLocation is Unknown (0)
                                    if (commandId == 10 || commandId == 30 || commandId == 60) {
                                        ZonedDateTime time = ZonedDateTime.now().minusMinutes(commandId);
                                        petcareAPI.setPetLocation(pet, currentLocation == 1 ? 2 : 1, time);

                                    }
                                    updateState(PET_CHANNEL_LOCATION,
                                            new StringType(pet.status.activity.where.toString()));
                                    updateState(PET_CHANNEL_LOCATION_CHANGED,
                                            new DateTimeType(pet.status.activity.since));
                                    updateState(PET_CHANNEL_LOCATION_TIMEOFFSET, UnDefType.UNDEF);
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid location id: {}, ignoring command", commandIdStr, e);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                            commandIdStr, pet, e);
                                }
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
    protected void updateThing() {
        synchronized (petcareAPI) {
            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
            if (pet != null) {
                logger.debug("Updating all thing channels for pet : {}", pet);
                updateState(PET_CHANNEL_ID, new DecimalType(pet.id));
                updateState(PET_CHANNEL_NAME, pet.name == null ? UnDefType.UNDEF : new StringType(pet.name));
                updateState(PET_CHANNEL_COMMENT, pet.comments == null ? UnDefType.UNDEF : new StringType(pet.comments));
                updateState(PET_CHANNEL_GENDER,
                        pet.genderId == null ? UnDefType.UNDEF : new StringType(pet.genderId.toString()));
                updateState(PET_CHANNEL_BREED,
                        pet.breedId == null ? UnDefType.UNDEF : new StringType(pet.breedId.toString()));
                updateState(PET_CHANNEL_SPECIES,
                        pet.speciesId == null ? UnDefType.UNDEF : new StringType(pet.speciesId.toString()));
                updateState(PET_CHANNEL_PHOTO,
                        pet.photo == null ? UnDefType.UNDEF : getPetPhotoFromCache(pet.photo.location));

                SurePetcarePetActivity loc = pet.status.activity;
                if (loc != null) {
                    updateState(PET_CHANNEL_LOCATION, new StringType(loc.where.toString()));
                    if (loc.since != null) {
                        updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(loc.since));
                    }

                    if (loc.deviceId != null) {
                        SurePetcareDevice device = petcareAPI.getDevice(loc.deviceId.toString());
                        if (device != null) {
                            updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH, new StringType(device.name));
                        }
                    } else if (loc.userId != null) {
                        SurePetcareHousehold household = petcareAPI.getHousehold(pet.householdId.toString());
                        if (household != null) {
                            Long userId = loc.userId;
                            household.users.stream().map(user -> user.user).filter(user -> userId.equals(user.userId))
                                    .forEach(user -> updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH,
                                            new StringType(user.userName)));
                        }
                    }
                }
                updateState(PET_CHANNEL_DATE_OF_BIRTH, pet.dateOfBirth == null ? UnDefType.UNDEF
                        : new DateTimeType(pet.dateOfBirth.atStartOfDay(ZoneId.systemDefault())));
                updateState(PET_CHANNEL_WEIGHT,
                        pet.weight == null ? UnDefType.UNDEF : new QuantityType<Mass>(pet.weight, SIUnits.KILOGRAM));
                if (pet.tagId != null) {
                    SurePetcareTag tag = petcareAPI.getTag(pet.tagId.toString());
                    if (tag != null) {
                        updateState(PET_CHANNEL_TAG_IDENTIFIER, new StringType(tag.tag));
                    }
                }
                SurePetcarePetFeeding feeding = pet.status.feeding;
                if (feeding != null) {
                    SurePetcareDevice device = petcareAPI.getDevice(feeding.deviceId.toString());
                    if (device != null) {
                        updateState(PET_CHANNEL_FEEDER_DEVICE, new StringType(device.name));
                        int bowlId = device.control.bowls.bowlId;
                        int numBowls = feeding.feedChange.size();
                        if (numBowls > 0) {
                            if (bowlId == BOWL_ID_ONE_BOWL_USED) {
                                updateState(PET_CHANNEL_FEEDER_LAST_CHANGE,
                                        new QuantityType<Mass>(feeding.feedChange.get(0), SIUnits.GRAM));
                            } else if (bowlId == BOWL_ID_TWO_BOWLS_USED) {
                                updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_LEFT,
                                        new QuantityType<Mass>(feeding.feedChange.get(0), SIUnits.GRAM));
                                if (numBowls > 1) {
                                    updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_RIGHT,
                                            new QuantityType<Mass>(feeding.feedChange.get(1), SIUnits.GRAM));
                                }
                            }
                        }
                        updateState(PET_CHANNEL_FEEDER_LASTFEEDING, new DateTimeType(feeding.feedChangeAt));
                    }
                }
            } else {
                logger.debug("Trying to update unknown pet: {}", thing.getUID().getId());
            }
        }
    }

    /**
     * Tries to lookup image in cache. If not found, it tries to download the image from its URL.
     *
     * @param url the url of the pet photo
     * @return the pet image as {@link RawType} or UNDEF
     */
    private State getPetPhotoFromCache(@Nullable String url) {
        if (url == null) {
            return UnDefType.UNDEF;
        }
        if (IMAGE_CACHE.containsKey(url)) {
            try {
                byte[] bytes = IMAGE_CACHE.get(url);
                String contentType = HttpUtil.guessContentTypeFromData(bytes);
                return new RawType(bytes,
                        contentType == null || contentType.isEmpty() ? RawType.DEFAULT_MIME_TYPE : contentType);
            } catch (IOException e) {
                logger.trace("Failed to download the content of URL '{}'", url, e);
            }
        } else {
            // photo is not yet in cache, download and add
            RawType image = HttpUtil.downloadImage(url);
            if (image != null) {
                IMAGE_CACHE.put(url, image.getBytes());
                return image;
            }
        }
        return UnDefType.UNDEF;
    }
}
