/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Mass;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetActivity;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetFeeding;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareTag;
import org.openhab.binding.surepetcare.internal.utils.ByteArrayFileCache;
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

    private static final String JPEG_CONTENT_TYPE = "image/jpeg";

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
                    logger.debug("Received location update command: {}", command.toFullString());
                    if (command instanceof StringType) {
                        synchronized (petcareAPI) {
                            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
                            if (pet != null) {
                                String newLocationIdStr = ((StringType) command).toString();
                                Integer newLocationId = Integer.valueOf(newLocationIdStr);
                                // Only update if location has changed. (Needed for Group:Switch item)
                                if ((pet.status.activity.where.equals(newLocationId)) || newLocationId.equals(0)) {
                                    logger.debug("Location has not changed, skip pet id: {} with loc id: {}", pet.id,
                                            newLocationId);
                                } else {
                                    try {
                                        logger.debug("Received new location: {}", newLocationId);
                                        petcareAPI.setPetLocation(pet, newLocationId, new Date());
                                        updateState(PET_CHANNEL_LOCATION,
                                                new StringType(pet.status.activity.where.toString()));
                                        updateState(PET_CHANNEL_LOCATION_CHANGED,
                                                new DateTimeType(pet.status.activity.getLocationChanged()));
                                    } catch (NumberFormatException e) {
                                        logger.warn("Invalid location id: {}, ignoring command", newLocationIdStr);
                                    } catch (SurePetcareApiException e) {
                                        logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                                newLocationIdStr, pet);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case PET_CHANNEL_LOCATION_TIMEOFFSET:
                    logger.debug("Received location time offset update command: {}", command.toFullString());
                    if (command instanceof StringType) {
                        synchronized (petcareAPI) {
                            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
                            if (pet != null) {
                                String commandIdStr = ((StringType) command).toString();
                                try {
                                    Integer commandId = Integer.valueOf(commandIdStr);
                                    Integer currentLocation = pet.status.activity.where;
                                    logger.debug("Received new location: {}", currentLocation == 1 ? 2 : 1);
                                    // We set the location to the opposite state.
                                    // We also set location to INSIDE (1) if currentLocation is Unknown (0)
                                    if (commandId == 10 || commandId == 30 || commandId == 60) {
                                        petcareAPI.setPetLocation(pet, currentLocation == 1 ? 2 : 1, new Date(
                                                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(commandId)));
                                    }
                                    updateState(PET_CHANNEL_LOCATION,
                                            new StringType(pet.status.activity.where.toString()));
                                    updateState(PET_CHANNEL_LOCATION_CHANGED,
                                            new DateTimeType(pet.status.activity.getLocationChanged()));
                                    updateState(PET_CHANNEL_LOCATION_TIMEOFFSET, UnDefType.UNDEF);
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid location id: {}, ignoring command", commandIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                            commandIdStr, pet);
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
                        pet.photo == null ? UnDefType.UNDEF : getPetPhotoImage(pet.photo.location));
                SurePetcarePetActivity loc = pet.status.activity;
                if (loc != null) {
                    updateState(PET_CHANNEL_LOCATION, new StringType(loc.where.toString()));
                    if (loc.getLocationChanged() != null) {
                        updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(loc.getLocationChanged()));
                    }
                }
                ZonedDateTime dob = pet.getDateOfBirthAsZonedDateTime();
                updateState(PET_CHANNEL_DATE_OF_BIRTH, dob == null ? UnDefType.UNDEF : new DateTimeType(dob));
                updateState(PET_CHANNEL_WEIGHT,
                        pet.weight == null ? UnDefType.UNDEF : new QuantityType<Mass>(pet.weight, SIUnits.KILOGRAM));
                if (pet.tagId != null) {
                    SurePetcareTag tag = petcareAPI.getTag(pet.tagId.toString());
                    if (tag != null) {
                        updateState(PET_CHANNEL_TAG_IDENTIFIER, new StringType(tag.tag));
                    }
                }
                if (pet.status.activity.deviceId != null) {
                    SurePetcareDevice device = petcareAPI.getDevice(pet.status.activity.deviceId.toString());
                    if (device != null) {
                        updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH, new StringType(device.name));
                    }
                } else if (pet.status.activity.userId != null) {
                    SurePetcareHousehold household = petcareAPI.getHousehold(pet.householdId.toString());
                    if (household != null) {
                        Long userId = pet.status.activity.userId;
                        household.householdUsers.stream().map(user -> user.user)
                                .filter(user -> userId.equals(user.userId))
                                .forEach(user -> updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH,
                                        new StringType(user.userName)));
                    }
                }
                SurePetcarePetFeeding feeding = pet.status.feeding;
                if (feeding != null) {
                    SurePetcareDevice device = petcareAPI.getDevice(feeding.deviceId.toString());
                    if (device != null) {
                        updateState(PET_CHANNEL_FEEDER_DEVICE, new StringType(device.name));
                        int bowlId = device.control.bowls.bowlId;
                        int numBowls = feeding.feedChange.size();
                        for (int i = 0; (i < 2) && (i < numBowls); i++) {
                            if (bowlId == 1) {
                                updateState(PET_CHANNEL_FEEDER_LAST_CHANGE,
                                        new QuantityType<Mass>(feeding.feedChange.get(i), SIUnits.GRAM));
                            } else if (bowlId == 4) {
                                if ((i + 1) == 1) {
                                    updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_LEFT,
                                            new QuantityType<Mass>(feeding.feedChange.get(i), SIUnits.GRAM));
                                }
                                if ((i + 1) == 2) {
                                    updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_RIGHT,
                                            new QuantityType<Mass>(feeding.feedChange.get(i), SIUnits.GRAM));
                                }
                            }
                        }
                        updateState(PET_CHANNEL_FEEDER_LASTFEEDING, new DateTimeType(feeding.getZonedFeedChangeAt()));
                    }
                }
            } else {
                logger.debug("Trying to update unknown pet: {}", thing.getUID().getId());
            }
        }
    }

    /**
     * Downloads the image for the given pet photo url.
     *
     * @param url the url of the pet photo
     * @return the pet image as {@link RawType}
     */
    public static RawType getPetPhotoImage(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Cannot download pet photo as image url is null.");
        }

        return downloadPetPhotoFromCache(String.format(url));
    }

    private static RawType downloadPetPhotoFromCache(String url) {
        if (IMAGE_CACHE.containsKey(url)) {
            return new RawType(IMAGE_CACHE.get(url), JPEG_CONTENT_TYPE);
        } else {
            RawType image = downloadPetPhoto(url);
            IMAGE_CACHE.put(url, image.getBytes());
            return image;
        }
    }

    private static RawType downloadPetPhoto(String url) {
        return HttpUtil.downloadImage(url);
    }

}
