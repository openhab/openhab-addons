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
package org.openhab.binding.modbus.internal.profiles;

import static org.openhab.binding.modbus.internal.profiles.ModbusProfiles.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * A factory and advisor for modbus profiles.
 *
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class ModbusProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Set.of(GAIN_OFFSET_TYPE);

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Set.of(GAIN_OFFSET);

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext context) {
        if (GAIN_OFFSET.equals(profileTypeUID)) {
            return new ModbusGainOffsetProfile<>(callback, context);
        } else {
            return null;
        }
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }
}
