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
package org.openhab.transform.jinja.internal.profiles;

import java.util.Arrays;
import java.util.Collection;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Profilefactory that creates the transformation profile for the jinja transformation service
 *
 * @author Jochen Klein - initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class JinjaTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    @NonNullByDefault({})
    private TransformationService service;

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Arrays.asList(ProfileTypeBuilder.newState(JinjaTransformationProfile.PROFILE_TYPE_UID,
                JinjaTransformationProfile.PROFILE_TYPE_UID.getId()).build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new JinjaTransformationProfile(callback, profileContext, service);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Arrays.asList(JinjaTransformationProfile.PROFILE_TYPE_UID);
    }

    @Reference(target = "(openhab.transform=JINJA)")
    public void addTransformationService(TransformationService service) {
        this.service = service;
    }

    public void removeTransformationService(TransformationService service) {
        this.service = null;
    }
}
