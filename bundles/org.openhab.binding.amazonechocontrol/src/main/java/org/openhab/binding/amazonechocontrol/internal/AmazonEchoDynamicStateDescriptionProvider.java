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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists.PlayList;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Dynamic channel state description provider.
 * Overrides the state description for the controls, which receive its configuration in the runtime.
 *
 * @author Michael Geramb - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, AmazonEchoDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class AmazonEchoDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public AmazonEchoDynamicStateDescriptionProvider(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable ThingHandler findHandler(Channel channel) {
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }
        ThingUID accountThingId = thing.getBridgeUID();
        if (accountThingId == null) {
            return null;
        }
        Thing accountThing = thingRegistry.get(accountThingId);
        if (accountThing == null) {
            return null;
        }
        AccountHandler accountHandler = (AccountHandler) accountThing.getHandler();
        if (accountHandler == null) {
            return null;
        }
        Connection connection = accountHandler.findConnection();
        if (connection == null || !connection.getIsLoggedIn()) {
            return null;
        }
        return thing.getHandler();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null || !BINDING_ID.equals(channelTypeUID.getBindingId())) {
            return null;
        }
        if (originalStateDescription == null) {
            return null;
        }

        if (CHANNEL_TYPE_BLUETHOOTH_MAC.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }
            BluetoothState bluetoothState = handler.findBluetoothState();
            if (bluetoothState == null) {
                return null;
            }
            List<PairedDevice> pairedDeviceList = bluetoothState.getPairedDeviceList();
            if (pairedDeviceList.isEmpty()) {
                return null;
            }
            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            for (PairedDevice device : pairedDeviceList) {
                final String value = device.address;
                if (value != null && device.friendlyName != null) {
                    options.add(new StateOption(value, device.friendlyName));
                }
            }
            StateDescription result = StateDescriptionFragmentBuilder.create(originalStateDescription)
                    .withOptions(options).build().toStateDescription();
            return result;
        } else if (CHANNEL_TYPE_AMAZON_MUSIC_PLAY_LIST_ID.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }

            JsonPlaylists playLists = handler.findPlaylists();
            if (playLists == null) {
                return null;
            }

            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            Map<String, PlayList @Nullable []> playlistMap = playLists.playlists;
            if (playlistMap != null) {
                for (PlayList[] innerLists : playlistMap.values()) {
                    if (innerLists != null && innerLists.length > 0) {
                        PlayList playList = innerLists[0];
                        final String value = playList.playlistId;
                        if (value != null && playList.title != null) {
                            options.add(new StateOption(value,
                                    String.format("%s (%d)", playList.title, playList.trackCount)));
                        }
                    }
                }
            }
            StateDescription result = StateDescriptionFragmentBuilder.create(originalStateDescription)
                    .withOptions(options).build().toStateDescription();
            return result;
        } else if (CHANNEL_TYPE_PLAY_ALARM_SOUND.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }

            List<JsonNotificationSound> notificationSounds = handler.findAlarmSounds();
            if (notificationSounds.isEmpty()) {
                return null;
            }

            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));

            for (JsonNotificationSound notificationSound : notificationSounds) {
                if (notificationSound.folder == null && notificationSound.providerId != null
                        && notificationSound.id != null && notificationSound.displayName != null) {
                    String providerSoundId = notificationSound.providerId + ":" + notificationSound.id;
                    options.add(new StateOption(providerSoundId, notificationSound.displayName));
                }
            }
            StateDescription result = StateDescriptionFragmentBuilder.create(originalStateDescription)
                    .withOptions(options).build().toStateDescription();
            return result;
        } else if (CHANNEL_TYPE_CHANNEL_PLAY_ON_DEVICE.equals(channel.getChannelTypeUID())) {
            FlashBriefingProfileHandler handler = (FlashBriefingProfileHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }
            AccountHandler accountHandler = handler.findAccountHandler();
            if (accountHandler == null) {
                return null;
            }
            List<Device> devices = accountHandler.getLastKnownDevices();
            if (devices.isEmpty()) {
                return null;
            }

            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            for (Device device : devices) {
                final String value = device.serialNumber;
                if (value != null && device.getCapabilities().contains("FLASH_BRIEFING")) {
                    options.add(new StateOption(value, device.accountName));
                }
            }
            return StateDescriptionFragmentBuilder.create(originalStateDescription).withOptions(options).build()
                    .toStateDescription();
        } else if (CHANNEL_TYPE_MUSIC_PROVIDER_ID.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }
            List<JsonMusicProvider> musicProviders = handler.findMusicProviders();
            if (musicProviders.isEmpty()) {
                return null;
            }

            List<StateOption> options = new ArrayList<>();
            for (JsonMusicProvider musicProvider : musicProviders) {
                List<String> properties = musicProvider.supportedProperties;
                String providerId = musicProvider.id;
                String displayName = musicProvider.displayName;
                if (properties != null && properties.contains("Alexa.Music.PlaySearchPhrase") && providerId != null
                        && !providerId.isEmpty() && "AVAILABLE".equals(musicProvider.availability)
                        && displayName != null && !displayName.isEmpty()) {
                    options.add(new StateOption(providerId, displayName));
                }
            }
            return StateDescriptionFragmentBuilder.create(originalStateDescription).withOptions(options).build()
                    .toStateDescription();
        } else if (CHANNEL_TYPE_START_COMMAND.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return null;
            }
            AccountHandler account = handler.findAccount();
            if (account == null) {
                return null;
            }
            List<FlashBriefingProfileHandler> flashbriefings = account.getFlashBriefingProfileHandlers();
            if (flashbriefings.isEmpty()) {
                return null;
            }

            List<StateOption> options = new ArrayList<>();
            options.addAll(originalStateDescription.getOptions());

            for (FlashBriefingProfileHandler flashBriefing : flashbriefings) {
                String value = FLASH_BRIEFING_COMMAND_PREFIX + flashBriefing.getThing().getUID().getId();
                String displayName = flashBriefing.getThing().getLabel();
                options.add(new StateOption(value, displayName));
            }
            return StateDescriptionFragmentBuilder.create(originalStateDescription).withOptions(options).build()
                    .toStateDescription();
        }
        return null;
    }
}
