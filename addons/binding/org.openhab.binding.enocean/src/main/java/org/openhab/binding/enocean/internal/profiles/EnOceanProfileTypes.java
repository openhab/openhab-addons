/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;

/**
 * Class holds ProfileTypeUIDs and ProfileTypes of EnOcean profiles
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanProfileTypes {

    public static final ProfileTypeUID RockerSwitchToPlayPause = new ProfileTypeUID(BINDING_ID,
            "rockerswitch-to-play-pause");

    public static final ProfileTypeUID RockerSwitchFromOnOff = new ProfileTypeUID(BINDING_ID,
            "rockerswitch-from-on-off");

    public static final ProfileTypeUID RockerSwitchFromRollershutter = new ProfileTypeUID(BINDING_ID,
            "rockerswitch-from-rollershutter");

    public static final TriggerProfileType RockerSwitchToPlayPauseType = ProfileTypeBuilder
            .newTrigger(RockerSwitchToPlayPause, "Rocker switch to Play/Pause")
            .withSupportedItemTypes(CoreItemFactory.PLAYER)
            .withSupportedChannelTypeUIDs(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID()).build();

    public static final StateProfileType RockerSwitchFromOnOffType = ProfileTypeBuilder
            .newState(RockerSwitchFromOnOff, "Rocker switch from On/Off item")
            .withSupportedItemTypes(CoreItemFactory.SWITCH).withSupportedChannelTypeUIDs(VirtualRockerSwitchChannelType)
            .build();

    public static final StateProfileType RockerSwitchFromRollershutterType = ProfileTypeBuilder
            .newState(RockerSwitchFromRollershutter, "Rocker switch from rollershutter item")
            .withSupportedItemTypes(CoreItemFactory.ROLLERSHUTTER)
            .withSupportedChannelTypeUIDs(VirtualRockerSwitchChannelType).build();
}
