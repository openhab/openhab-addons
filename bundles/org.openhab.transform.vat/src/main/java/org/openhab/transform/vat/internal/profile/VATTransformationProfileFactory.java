/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.vat.internal.profile;

import static org.openhab.transform.vat.internal.VATTransformationConstants.PROFILE_TYPE_UID;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocalizedKey;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.i18n.ProfileTypeI18nLocalizationService;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.util.BundleResolver;
import org.openhab.transform.vat.internal.RateProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link VATTransformationProfileFactory} is responsible for creating profiles.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class VATTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private final LocaleProvider localeProvider;
    private final RateProvider rateProvider = new RateProvider();
    private final ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService;
    private final Map<LocalizedKey, ProfileType> localizedProfileTypeCache = new ConcurrentHashMap<>();
    @Nullable
    private final Bundle bundle;

    private @NonNullByDefault({}) TransformationService transformationService;

    @Activate
    public VATTransformationProfileFactory(final @Reference LocaleProvider localeProvider,
            final @Reference ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService,
            final @Reference BundleResolver bundleResolver) {
        this.localeProvider = localeProvider;
        this.profileTypeI18nLocalizationService = profileTypeI18nLocalizationService;
        this.bundle = bundleResolver.resolveBundle(VATTransformationProfileFactory.class);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Set.of(createLocalizedProfileType(ProfileTypeBuilder.newState(PROFILE_TYPE_UID, PROFILE_TYPE_UID.getId())
                .withSupportedItemTypes(CoreItemFactory.NUMBER).build(), locale));
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new VATTransformationProfile(callback, transformationService, profileContext, localeProvider,
                rateProvider);
    }

    private ProfileType createLocalizedProfileType(ProfileType profileType, @Nullable Locale locale) {
        final LocalizedKey localizedKey = new LocalizedKey(profileType.getUID(),
                locale != null ? locale.toLanguageTag() : null);

        final ProfileType cachedlocalizedProfileType = localizedProfileTypeCache.get(localizedKey);
        if (cachedlocalizedProfileType != null) {
            return cachedlocalizedProfileType;
        }

        final ProfileType localizedProfileType = profileTypeI18nLocalizationService.createLocalizedProfileType(bundle,
                profileType, locale);
        localizedProfileTypeCache.put(localizedKey, localizedProfileType);

        return localizedProfileType;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return List.of(PROFILE_TYPE_UID);
    }

    @Reference(target = "(openhab.transform=VAT)")
    public void addTransformationService(TransformationService transformationService) {
        this.transformationService = transformationService;
    }

    public void removeTransformationService(TransformationService transformationService) {
        this.transformationService = null;
    }
}
