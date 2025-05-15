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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationSoundTO;
import org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.FlashBriefingProfileHandler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonEchoControlCommandDescriptionProvider} implements dynamic command description provider for the
 * amazonechocontrol binding
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class, AmazonEchoControlCommandDescriptionProvider.class })
public class AmazonEchoControlCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider {
    private final Logger logger = LoggerFactory.getLogger(AmazonEchoControlCommandDescriptionProvider.class);

    public void setFlashBriefingTargets(Collection<FlashBriefingProfileHandler> flashBriefingProfileHandlers,
            Collection<DeviceTO> targets) {
        List<CommandOption> options = new ArrayList<>();
        options.add(new CommandOption("", ""));
        for (DeviceTO device : targets) {
            final String value = device.serialNumber;
            if (value != null && device.capabilities.contains("FLASH_BRIEFING")) {
                options.add(new CommandOption(value, device.accountName));
            }
        }

        for (FlashBriefingProfileHandler flashBriefingProfileHandler : flashBriefingProfileHandlers) {
            ChannelUID channelUID = new ChannelUID(flashBriefingProfileHandler.getThing().getUID(),
                    CHANNEL_PLAY_ON_DEVICE);
            if (options.isEmpty()) {
                channelOptionsMap.remove(channelUID);
            } else {
                channelOptionsMap.put(channelUID, options);
            }
        }
    }

    public void setEchoHandlerStartCommands(Collection<EchoHandler> echoHandlers,
            Collection<FlashBriefingProfileHandler> flashBriefingProfileHandlers) {
        List<CommandOption> options = new ArrayList<>();
        options.add(new CommandOption("Weather", "Weather"));
        options.add(new CommandOption("Traffic", "Traffic"));
        options.add(new CommandOption("GoodMorning", "Good morning"));
        options.add(new CommandOption("SingASong", "Song"));
        options.add(new CommandOption("TellStory", "Story"));
        options.add(new CommandOption("FlashBriefing", "Flash briefing"));

        for (FlashBriefingProfileHandler flashBriefing : flashBriefingProfileHandlers) {
            String value = FLASH_BRIEFING_COMMAND_PREFIX + flashBriefing.getThing().getUID().getId();
            String displayName = flashBriefing.getThing().getLabel();
            options.add(new CommandOption(value, displayName));
        }

        for (EchoHandler echoHandler : echoHandlers) {
            ChannelUID channelUID = new ChannelUID(echoHandler.getThing().getUID(), CHANNEL_START_COMMAND);
            if (options.isEmpty()) {
                channelOptionsMap.remove(channelUID);
            } else {
                channelOptionsMap.put(channelUID, options);
            }
        }
    }

    public void setEchoHandlerAlarmSounds(EchoHandler echoHandler, List<NotificationSoundTO> alarmSounds) {
        List<CommandOption> options = new ArrayList<>();
        for (NotificationSoundTO notificationSound : alarmSounds) {
            if (notificationSound.folder == null && notificationSound.providerId != null && notificationSound.id != null
                    && notificationSound.displayName != null) {
                String providerSoundId = notificationSound.providerId + ":" + notificationSound.id;
                options.add(new CommandOption(providerSoundId, notificationSound.displayName));
            }
        }

        ChannelUID channelUID = new ChannelUID(echoHandler.getThing().getUID(), CHANNEL_PLAY_ALARM_SOUND);
        if (options.isEmpty()) {
            channelOptionsMap.remove(channelUID);
        } else {
            channelOptionsMap.put(channelUID, options);
        }
    }

    public void removeCommandDescriptionForThing(ThingUID thingUID) {
        logger.trace("removing state description for thing {}", thingUID);
        channelOptionsMap.entrySet().removeIf(entry -> entry.getKey().getThingUID().equals(thingUID));
    }
}
