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
package org.openhab.binding.tr064.profile.phonebook.internal;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.profile.phonebook.PhonebookProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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

    private static final String THING_UID_PARAM = "thingUid";
    private static final String PHONEBOOK_NAME_PARAM = "phonebookName";
    private static final String MATCH_COUNT_PARAM = "matchCount";

    private final Logger logger = LoggerFactory.getLogger(PhonebookProfile.class);

    private final ProfileCallback callback;

    private final @Nullable String phonebookName;
    private final @Nullable ThingUID thingUID;
    private final Map<ThingUID, PhonebookProvider> phonebookProviders;
    private final int matchCount;

    public PhonebookProfile(ProfileCallback callback, ProfileContext context,
            Map<ThingUID, PhonebookProvider> phonebookProviders) {
        this.callback = callback;
        this.phonebookProviders = phonebookProviders;

        Configuration configuration = context.getConfiguration();
        Object thingUidParam = configuration.get(THING_UID_PARAM);
        Object phonebookNameParam = configuration.get(PHONEBOOK_NAME_PARAM);
        Object matchCountParam = configuration.get(MATCH_COUNT_PARAM);

        logger.debug("Profile configured with '{}'='{}', '{}'='{}', '{}'='{}'", THING_UID_PARAM, thingUidParam,
                PHONEBOOK_NAME_PARAM, phonebookNameParam, MATCH_COUNT_PARAM, matchCountParam);

        ThingUID thingUID;
        String phonebookName = null;
        int matchCount = 0;

        try {
            if (!(thingUidParam instanceof String)
                    || ((phonebookNameParam != null) && !(phonebookNameParam instanceof String))
                    || ((matchCountParam != null) && (matchCountParam instanceof String))) {
                throw new IllegalArgumentException("Parameters need to be Strings.");
            }
            thingUID = new ThingUID((String) thingUidParam);
            if (phonebookName != null) {
                phonebookName = (String) phonebookNameParam;
            }
            if (matchCountParam != null) {
                matchCount = Integer.valueOf((String) matchCountParam);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Could not initialize PHONEBOOK transformation profile: {}. Profile will be inactive.",
                    e.getMessage());
            thingUID = null;
        }

        this.thingUID = thingUID;
        this.phonebookName = phonebookName;
        this.matchCount = matchCount;
    }

    @Override
    public void onCommandFromItem(Command command) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        if (state instanceof StringType) {
            PhonebookProvider provider = phonebookProviders.get(thingUID);
            if (provider == null) {
                logger.warn("Could not get phonebook provider with thing UID '{}'.", thingUID);
                return;
            }
            final String phonebookName = this.phonebookName;
            Optional<String> match;
            if (phonebookName != null) {
                match = provider.getPhonebookByName(phonebookName).or(() -> {
                    logger.warn("Could not get phonebook '{}' from provider '{}'", phonebookName, thingUID);
                    return Optional.empty();
                }).flatMap(phonebook -> phonebook.lookupNumber(state.toString(), matchCount));
            } else {
                match = provider.getPhonebooks().stream().map(p -> p.lookupNumber(state.toString(), matchCount))
                        .filter(Optional::isPresent).map(Optional::get).findAny();
            }
            match.ifPresentOrElse(name -> callback.handleUpdate(new StringType(name)),
                    () -> logger.debug("Number '{}' not found in phonebook '{}' from provider '{}'", state,
                            phonebookName, thingUID));
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
