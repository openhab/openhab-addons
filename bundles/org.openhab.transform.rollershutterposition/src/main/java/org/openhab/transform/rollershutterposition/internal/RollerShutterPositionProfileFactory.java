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
package org.openhab.transform.rollershutterposition.internal;

import static org.openhab.transform.rollershutterposition.internal.RollerShutterPositionConstants.PROFILE_TYPE_UID;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * {@link RollerShutterPositionProfileFactory} Factory to create the profile
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class RollerShutterPositionProfileFactory implements ProfileFactory, ProfileTypeProvider {
    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return List.of(ProfileTypeBuilder.newState(PROFILE_TYPE_UID, PROFILE_TYPE_UID.getId())
                .withSupportedItemTypes(CoreItemFactory.ROLLERSHUTTER).build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new RollerShutterPositionProfile(callback, profileContext);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return List.of(PROFILE_TYPE_UID);
    }
}
