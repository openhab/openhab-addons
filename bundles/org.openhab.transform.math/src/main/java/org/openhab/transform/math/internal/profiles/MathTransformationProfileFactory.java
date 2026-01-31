/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.transform.math.internal.profiles;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocalizedKey;
import org.openhab.core.items.ItemRegistry;
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
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MathTransformationProfileFactory} is responsible for creating profiles.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class MathTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private static final ProfileType ADD_PROFILE_TYPE = ProfileTypeBuilder
            .newState(AddTransformationProfile.PROFILE_TYPE_UID, AddTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();
    private static final ProfileType MULTIPLY_PROFILE_TYPE = ProfileTypeBuilder
            .newState(MultiplyTransformationProfile.PROFILE_TYPE_UID,
                    MultiplyTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();
    private static final ProfileType DIVIDE_PROFILE_TYPE = ProfileTypeBuilder
            .newState(DivideTransformationProfile.PROFILE_TYPE_UID,
                    DivideTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();
    private static final ProfileType BITAND_PROFILE_TYPE = ProfileTypeBuilder
            .newState(BitwiseAndTransformationProfile.PROFILE_TYPE_UID,
                    BitwiseAndTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();
    private static final ProfileType BITOR_PROFILE_TYPE = ProfileTypeBuilder
            .newState(BitwiseOrTransformationProfile.PROFILE_TYPE_UID,
                    BitwiseOrTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();
    private static final ProfileType BITXOR_PROFILE_TYPE = ProfileTypeBuilder
            .newState(BitwiseXorTransformationProfile.PROFILE_TYPE_UID,
                    BitwiseXorTransformationProfile.PROFILE_TYPE_UID.getId())
            .withSupportedItemTypes(CoreItemFactory.NUMBER).build();

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Set.of(
            AddTransformationProfile.PROFILE_TYPE_UID, MultiplyTransformationProfile.PROFILE_TYPE_UID,
            DivideTransformationProfile.PROFILE_TYPE_UID, BitwiseAndTransformationProfile.PROFILE_TYPE_UID,
            BitwiseOrTransformationProfile.PROFILE_TYPE_UID, BitwiseXorTransformationProfile.PROFILE_TYPE_UID);
    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Set.of(ADD_PROFILE_TYPE, MULTIPLY_PROFILE_TYPE,
            DIVIDE_PROFILE_TYPE, BITAND_PROFILE_TYPE, BITOR_PROFILE_TYPE, BITXOR_PROFILE_TYPE);

    private final Map<LocalizedKey, ProfileType> localizedProfileTypeCache = new ConcurrentHashMap<>();

    private final ItemRegistry itemRegistry;
    private final ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService;
    private final @Nullable Bundle bundle;

    private @NonNullByDefault({}) TransformationService addTransformationService;
    private @NonNullByDefault({}) TransformationService multiplyTransformationService;
    private @NonNullByDefault({}) TransformationService divideTransformationService;

    private @NonNullByDefault({}) TransformationService bitAndTransformationService;
    private @NonNullByDefault({}) TransformationService bitOrTransformationService;
    private @NonNullByDefault({}) TransformationService bitXorTransformationService;

    @Activate
    public MathTransformationProfileFactory(final @Reference ItemRegistry itemRegistry, //
            final @Reference ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService, //
            final @Reference BundleResolver bundleResolver) {
        this.itemRegistry = itemRegistry;
        this.profileTypeI18nLocalizationService = profileTypeI18nLocalizationService;
        this.bundle = bundleResolver.resolveBundle(MathTransformationProfileFactory.class);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES.stream().map(p -> createLocalizedProfileType(p, locale))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (AddTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new AddTransformationProfile(callback, profileContext, addTransformationService, itemRegistry);
        } else if (MultiplyTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new MultiplyTransformationProfile(callback, profileContext, multiplyTransformationService,
                    itemRegistry);
        } else if (DivideTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new DivideTransformationProfile(callback, profileContext, divideTransformationService, itemRegistry);
        } else if (BitwiseAndTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new BitwiseAndTransformationProfile(callback, profileContext, bitAndTransformationService);
        } else if (BitwiseOrTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new BitwiseOrTransformationProfile(callback, profileContext, bitOrTransformationService);
        } else if (BitwiseXorTransformationProfile.PROFILE_TYPE_UID.equals(profileTypeUID)) {
            return new BitwiseXorTransformationProfile(callback, profileContext, bitXorTransformationService);
        }
        return null;
    }

    private ProfileType createLocalizedProfileType(ProfileType profileType, @Nullable Locale locale) {
        final LocalizedKey localizedKey = new LocalizedKey(profileType.getUID(),
                locale != null ? locale.toLanguageTag() : null);

        final ProfileType cachedlocalizedProfileType = localizedProfileTypeCache.get(localizedKey);
        if (cachedlocalizedProfileType != null) {
            return cachedlocalizedProfileType;
        }

        if (bundle instanceof Bundle localBundle) {
            final ProfileType localizedProfileType = profileTypeI18nLocalizationService
                    .createLocalizedProfileType(localBundle, profileType, locale);
            localizedProfileTypeCache.put(localizedKey, localizedProfileType);
            return localizedProfileType;
        }
        return profileType;
    }

    @Reference(target = "(openhab.transform=ADD)")
    public void addAddTransformationService(TransformationService service) {
        this.addTransformationService = service;
    }

    public void removeAddTransformationService(TransformationService service) {
        this.addTransformationService = null;
    }

    @Reference(target = "(openhab.transform=MULTIPLY)")
    public void addMultiplyTransformationService(TransformationService service) {
        this.multiplyTransformationService = service;
    }

    public void removeMultiplyTransformationService(TransformationService service) {
        this.multiplyTransformationService = null;
    }

    @Reference(target = "(openhab.transform=DIVIDE)")
    public void addDivideTransformationService(TransformationService service) {
        this.divideTransformationService = service;
    }

    public void removeDivideTransformationService(TransformationService service) {
        this.divideTransformationService = null;
    }

    @Reference(target = "(openhab.transform=BITAND)")
    public void addBitAndTransformationService(TransformationService service) {
        this.bitAndTransformationService = service;
    }

    public void removeBitAndTransformationService(TransformationService service) {
        this.bitAndTransformationService = null;
    }

    @Reference(target = "(openhab.transform=BITOR)")
    public void addBitOrTransformationService(TransformationService service) {
        this.bitOrTransformationService = service;
    }

    public void removeBitOrTransformationService(TransformationService service) {
        this.bitOrTransformationService = null;
    }

    @Reference(target = "(openhab.transform=BITXOR)")
    public void addBitXorTransformationService(TransformationService service) {
        this.bitXorTransformationService = service;
    }

    public void removeBitXorTransformationService(TransformationService service) {
        this.bitXorTransformationService = null;
    }
}
