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
package org.openhab.transform.geocoding.internal;

import static org.openhab.transform.geocoding.internal.GeoConstants.PROFILE_TYPE_UID;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.transform.geocoding.internal.profiles.OSMGeoProfile;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link GeoProfileFactory} Factory to create the profile
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class GeoProfileFactory implements ProfileFactory, ProfileTypeProvider {
    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;

    @Activate
    public GeoProfileFactory(final @Reference HttpClientFactory httpFactory, final @Reference LocaleProvider locale) {
        this.httpClientFactory = httpFactory;
        this.localeProvider = locale;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return List.of(ProfileTypeBuilder.newState(PROFILE_TYPE_UID, "Geo Coding")
                .withSupportedItemTypes(CoreItemFactory.STRING)
                .withSupportedItemTypesOfChannel(CoreItemFactory.LOCATION).build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback profileCallback,
            ProfileContext profileContext) {
        return new OSMGeoProfile(profileCallback, profileContext, httpClientFactory.getCommonHttpClient(), localeProvider);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return List.of(PROFILE_TYPE_UID);
    }
}
