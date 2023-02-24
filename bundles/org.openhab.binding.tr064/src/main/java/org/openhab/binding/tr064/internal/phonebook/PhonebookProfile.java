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
package org.openhab.binding.tr064.internal.phonebook;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhonebookProfile} class provides a profile for resolving phone number strings to names
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class PhonebookProfile implements StateProfile {
    public static final ProfileTypeUID PHONEBOOK_PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "PHONEBOOK");
    public static final ProfileType PHONEBOOK_PROFILE_TYPE = ProfileTypeBuilder //
            .newState(PHONEBOOK_PROFILE_TYPE_UID, "Phonebook") //
            .withSupportedItemTypesOfChannel(CoreItemFactory.CALL, CoreItemFactory.STRING) //
            .withSupportedItemTypes(CoreItemFactory.STRING) //
            .build();

    public static final String PHONEBOOK_PARAM = "phonebook";
    public static final String MATCH_COUNT_PARAM = "matchCount";
    public static final String PHONE_NUMBER_INDEX_PARAM = "phoneNumberIndex";

    private final Logger logger = LoggerFactory.getLogger(PhonebookProfile.class);

    private final ProfileCallback callback;

    private final @Nullable String phonebookName;
    private final @Nullable ThingUID thingUID;
    private final Map<ThingUID, PhonebookProvider> phonebookProviders;
    private final int matchCount;
    private final int phoneNumberIndex;

    public PhonebookProfile(ProfileCallback callback, ProfileContext context,
            Map<ThingUID, PhonebookProvider> phonebookProviders) {
        this.callback = callback;
        this.phonebookProviders = phonebookProviders;

        Configuration configuration = context.getConfiguration();
        Object phonebookParam = configuration.get(PHONEBOOK_PARAM);
        Object matchCountParam = configuration.get(MATCH_COUNT_PARAM);
        Object phoneNumberIndexParam = configuration.get(PHONE_NUMBER_INDEX_PARAM);

        logger.debug("Profile configured with '{}'='{}', '{}'='{}', '{}'='{}'", PHONEBOOK_PARAM, phonebookParam,
                MATCH_COUNT_PARAM, matchCountParam, PHONE_NUMBER_INDEX_PARAM, phoneNumberIndexParam);

        ThingUID thingUID;
        String phonebookName = null;
        int matchCount = 0;
        int phoneNumberIndex = 0;

        try {
            if (!(phonebookParam instanceof String)) {
                throw new IllegalArgumentException("Parameter 'phonebook' need to be a String");
            }
            String[] phonebookParams = ((String) phonebookParam).split(":");
            if (phonebookParams.length > 2) {
                throw new IllegalArgumentException("Cannot split 'phonebook' parameter");
            }
            thingUID = new ThingUID(UIDUtils.decode(phonebookParams[0]));
            if (phonebookParams.length == 2) {
                phonebookName = UIDUtils.decode(phonebookParams[1]);
            }
            if (matchCountParam != null) {
                if (matchCountParam instanceof BigDecimal) {
                    matchCount = ((BigDecimal) matchCountParam).intValue();
                } else if (matchCountParam instanceof String) {
                    matchCount = Integer.parseInt((String) matchCountParam);
                }
            }
            if (phoneNumberIndexParam != null) {
                if (phoneNumberIndexParam instanceof BigDecimal) {
                    phoneNumberIndex = ((BigDecimal) phoneNumberIndexParam).intValue();
                } else if (phoneNumberIndexParam instanceof String) {
                    phoneNumberIndex = Integer.parseInt((String) phoneNumberIndexParam);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot initialize PHONEBOOK transformation profile: {}. Profile will be inactive.",
                    e.getMessage());
            thingUID = null;
        }

        this.thingUID = thingUID;
        this.phonebookName = phonebookName;
        this.matchCount = matchCount;
        this.phoneNumberIndex = phoneNumberIndex;
    }

    @Override
    public void onCommandFromItem(Command command) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void onStateUpdateFromHandler(State state) {
        if (state instanceof UnDefType) {
            // we cannot adjust UNDEF or NULL values, thus we simply apply them without reporting an error or warning
            callback.sendUpdate(state);
        }
        if (state instanceof StringType) {
            Optional<String> match = resolveNumber(state.toString());
            State newState = Objects.requireNonNull(match.map(name -> (State) new StringType(name)).orElse(state));
            if (newState.equals(state)) {
                logger.debug("Number '{}' not found in phonebook '{}' from provider '{}'", state, phonebookName,
                        thingUID);
            }
            callback.sendUpdate(newState);
        } else if (state instanceof StringListType stringList) {
            try {
                String phoneNumber = stringList.getValue(phoneNumberIndex);
                Optional<String> match = resolveNumber(phoneNumber);
                final State newState;
                if (match.isPresent()) {
                    newState = new StringType(match.get());
                } else {
                    logger.debug("Number '{}' not found in phonebook '{}' from provider '{}'", phoneNumber,
                            phonebookName, thingUID);
                    newState = new StringType(phoneNumber);
                }
                callback.sendUpdate(newState);
            } catch (IllegalArgumentException e) {
                logger.debug("StringListType does not contain a number at index {}", phoneNumberIndex);
            }
        }
    }

    private Optional<String> resolveNumber(String phoneNumber) {
        PhonebookProvider provider = phonebookProviders.get(thingUID);
        if (provider == null) {
            logger.warn("Could not get phonebook provider with thing UID '{}'.", thingUID);
            return Optional.empty();
        }
        final String phonebookName = this.phonebookName;
        if (phonebookName != null) {
            return provider.getPhonebookByName(phonebookName).or(() -> {
                logger.warn("Could not get phonebook '{}' from provider '{}'", phonebookName, thingUID);
                return Optional.empty();
            }).flatMap(phonebook -> phonebook.lookupNumber(phoneNumber, matchCount));
        } else {
            return provider.getPhonebooks().stream().map(p -> p.lookupNumber(phoneNumber, matchCount))
                    .filter(Optional::isPresent).map(Optional::get).findAny();
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PHONEBOOK_PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }
}
