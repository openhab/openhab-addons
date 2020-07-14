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
package org.openhab.binding.tr064.profile.phonebook;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.openhab.binding.tr064.profile.phonebook.internal.PhonebookProfile;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhonebookProfileFactory} class is used to create phonebook profiles
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { ProfileFactory.class, ProfileTypeProvider.class,
        PhonebookProfileFactory.class })
public class PhonebookProfileFactory implements ProfileFactory, ProfileTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(PhonebookProfileFactory.class);
    private final Map<ThingUID, PhonebookProvider> phonebookProviders = new ConcurrentHashMap<>();

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new PhonebookProfile(callback, profileContext, phonebookProviders);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Collections.singleton(PhonebookProfile.PHONEBOOK_PROFILE_TYPE_UID);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Collections
                .singleton(ProfileTypeBuilder.newState(PhonebookProfile.PHONEBOOK_PROFILE_TYPE_UID,
                        PhonebookProfile.PHONEBOOK_PROFILE_TYPE_UID.getId()).build());
    }

    /**
     * register a phonebook provider
     *
     * @param phonebookProvider the provider that shall be added
     */
    public void registerPhonebookProvider(PhonebookProvider phonebookProvider) {
        if (phonebookProviders.put(phonebookProvider.getUID(), phonebookProvider) != null) {
            logger.warn("Tried to register a phonebook provider with UID '{}' for the second time.",
                    phonebookProvider.getUID());
        }
    }

    /**
     * unregister a phonebook provider
     *
     * @param phonebookProvider the provider that shall be removed
     */
    public void unregisterPhonebookProvider(PhonebookProvider phonebookProvider) {
        if (phonebookProviders.remove(phonebookProvider.getUID()) == null) {
            logger.warn("Tried to unregister a phonebook provider with UID '{}' but it was not found.",
                    phonebookProvider.getUID());
        }
    }
}
