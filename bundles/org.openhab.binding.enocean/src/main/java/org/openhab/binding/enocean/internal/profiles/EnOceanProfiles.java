/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;

/**
 * The {@link EnOceanProfiles} class defines profile constants
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanProfiles {
    public static final ProfileTypeUID ROCKERSWITCHACTION_TOGGLE_SWITCH = new ProfileTypeUID(BINDING_ID,
            "rockerswitchaction-toggle-switch");

    public static final ProfileTypeUID ROCKERSWITCHACTION_TOGGLE_PLAYER = new ProfileTypeUID(BINDING_ID,
            "rockerswitchaction-toggle-player");

    static final ProfileType ROCKERSWITCHACTION_TOGGLE_SWITCH_TYPE = ProfileTypeBuilder
            .newTrigger(ROCKERSWITCHACTION_TOGGLE_SWITCH, "Rocker Switch Action Toggle Switch")
            .withSupportedItemTypes(CoreItemFactory.SWITCH)
            .withSupportedChannelTypeUIDs(CHANNELTYPE_ROCKERSWITCH_ACTION_UID).build();

    static final ProfileType ROCKERSWITCHACTION_TOGGLE_PLAYER_TYPE = ProfileTypeBuilder
            .newTrigger(ROCKERSWITCHACTION_TOGGLE_PLAYER, "Rocker Switch Action Toggle Player")
            .withSupportedItemTypes(CoreItemFactory.PLAYER)
            .withSupportedChannelTypeUIDs(CHANNELTYPE_ROCKERSWITCH_ACTION_UID).build();
}
