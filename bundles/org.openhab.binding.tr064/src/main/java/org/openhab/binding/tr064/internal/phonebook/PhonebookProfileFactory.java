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

import static java.util.Comparator.comparing;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.i18n.LocalizedKey;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.i18n.ProfileTypeI18nLocalizationService;
import org.openhab.core.util.BundleResolver;
import org.openhab.core.util.UIDUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhonebookProfileFactory} class is used to create phonebook profiles
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class, PhonebookProfileFactory.class,
        ConfigOptionProvider.class })
public class PhonebookProfileFactory implements ProfileFactory, ProfileTypeProvider, ConfigOptionProvider {
    private final Logger logger = LoggerFactory.getLogger(PhonebookProfileFactory.class);
    private final Map<ThingUID, PhonebookProvider> phonebookProviders = new ConcurrentHashMap<>();

    private final Map<LocalizedKey, ProfileType> localizedProfileTypeCache = new ConcurrentHashMap<>();

    private final ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService;
    private final Bundle bundle;

    @Activate
    public PhonebookProfileFactory(
            final @Reference ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService,
            final @Reference BundleResolver bundleResolver) {
        this.profileTypeI18nLocalizationService = profileTypeI18nLocalizationService;
        this.bundle = bundleResolver.resolveBundle(PhonebookProfileFactory.class);
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new PhonebookProfile(callback, profileContext, phonebookProviders);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Set.of(PhonebookProfile.PHONEBOOK_PROFILE_TYPE_UID);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Set.of(createLocalizedProfileType(PhonebookProfile.PHONEBOOK_PROFILE_TYPE, locale));
    }

    private ProfileType createLocalizedProfileType(ProfileType profileType, @Nullable Locale locale) {
        final LocalizedKey localizedKey = new LocalizedKey(profileType.getUID(),
                locale != null ? locale.toLanguageTag() : null);

        return Objects.requireNonNull(localizedProfileTypeCache.computeIfAbsent(localizedKey,
                key -> profileTypeI18nLocalizationService.createLocalizedProfileType(bundle, profileType, locale)));
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

    private List<ParameterOption> createPhonebookList(Map.Entry<ThingUID, PhonebookProvider> entry) {
        String thingUid = UIDUtils.encode(entry.getKey().toString());
        String thingName = entry.getValue().getFriendlyName();

        List<ParameterOption> parameterOptions = entry.getValue().getPhonebooks().stream()
                .map(phonebook -> new ParameterOption(thingUid + ":" + UIDUtils.encode(phonebook.getName()),
                        thingName + " - " + phonebook.getName()))
                .collect(Collectors.toList());

        if (!parameterOptions.isEmpty()) {
            parameterOptions.add(new ParameterOption(thingUid, thingName));
        }

        return parameterOptions;
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String s, @Nullable String s1,
            @Nullable Locale locale) {
        if (uri.getSchemeSpecificPart().equals(PhonebookProfile.PHONEBOOK_PROFILE_TYPE_UID.toString())
                && s.equals(PhonebookProfile.PHONEBOOK_PARAM)) {
            List<ParameterOption> parameterOptions = new ArrayList<>();
            for (Map.Entry<ThingUID, PhonebookProvider> entry : phonebookProviders.entrySet()) {
                parameterOptions.addAll(createPhonebookList(entry));
            }
            parameterOptions.sort(comparing(o -> o.getLabel()));
            return parameterOptions;
        }
        return null;
    }
}
