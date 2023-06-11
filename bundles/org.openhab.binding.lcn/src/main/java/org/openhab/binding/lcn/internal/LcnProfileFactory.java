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
package org.openhab.binding.lcn.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create Profile instances. Also provides the available ProfileTypes and gives advise which profile to use
 * by a given link.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class LcnProfileFactory implements ProfileFactory, ProfileTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(LcnProfileFactory.class);

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Collections.singleton(DimmerOutputProfile.UID);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Collections.singleton(ProfileTypeBuilder.newState(DimmerOutputProfile.UID, "Dimmer Output (%)")
                .withSupportedItemTypes(CoreItemFactory.DIMMER, CoreItemFactory.COLOR)
                .withSupportedChannelTypeUIDs(
                        new ChannelTypeUID(LcnBindingConstants.BINDING_ID, LcnChannelGroup.OUTPUT.name().toLowerCase()))
                .build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (profileTypeUID.equals(DimmerOutputProfile.UID)) {
            return new DimmerOutputProfile(callback, profileContext);
        } else {
            logger.warn("Could not create {}", profileTypeUID);
            return null;
        }
    }
}
