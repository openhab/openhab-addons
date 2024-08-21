/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.scale.internal.profiles;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;
import org.openhab.transform.scale.internal.ScaleTransformationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link ProfileFactory} that creates the transformation profile for the {@link ScaleTransformationService}
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class ScaleTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    @NonNullByDefault({})
    private TransformationService service;

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return List.of(ProfileTypeBuilder.newState(ScaleTransformationProfile.PROFILE_TYPE_UID,
                ScaleTransformationProfile.PROFILE_TYPE_UID.getId()).build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new ScaleTransformationProfile(callback, profileContext, service);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return List.of(ScaleTransformationProfile.PROFILE_TYPE_UID);
    }

    @Reference(target = "(openhab.transform=SCALE)")
    public void addTransformationService(TransformationService service) {
        this.service = service;
    }

    public void removeTransformationService(TransformationService service) {
        this.service = null;
    }
}
