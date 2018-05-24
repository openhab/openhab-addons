/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.statedescription;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.amazonechocontrol.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists.PlayList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Dynamic channel state description provider.
 * Overrides the state description for the controls, which receive its configuration in the runtime.
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class,
        AmazonEchoDynamicStateDescriptionProvider.class }, immediate = true)
public class AmazonEchoDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(AmazonEchoDynamicStateDescriptionProvider.class);
    private @Nullable ThingRegistry thingRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable ThingHandler findHandler(Channel channel) {
        ThingRegistry thingRegistry = this.thingRegistry;
        if (thingRegistry == null) {
            return null;
        }
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }
        return thing.getHandler();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        if (originalStateDescription == null) {
            return null;
        }
        ThingRegistry thingRegistry = this.thingRegistry;
        if (thingRegistry == null) {
            return originalStateDescription;
        }
        if (CHANNEL_TYPE_BLUETHOOTH_MAC.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            BluetoothState bluetoothState = handler.findBluetoothState();
            if (bluetoothState == null) {
                return originalStateDescription;
            }
            PairedDevice[] pairedDeviceList = bluetoothState.pairedDeviceList;
            if (pairedDeviceList == null) {
                return originalStateDescription;
            }

            ArrayList<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            for (PairedDevice device : pairedDeviceList) {
                if (device == null) {
                    continue;
                }
                if (device.address != null && device.friendlyName != null) {
                    options.add(new StateOption(device.address, device.friendlyName));
                }
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;

        } else if (CHANNEL_TYPE_AMAZON_MUSIC_PLAY_LIST_ID.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            Connection connection = handler.findConnection();
            if (connection == null) {
                return originalStateDescription;
            }
            Device device = handler.findDevice();
            if (device == null) {
                return originalStateDescription;
            }
            JsonPlaylists playLists;
            try {
                playLists = connection.getPlaylists(device);
            } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException e) {
                logger.warn("Get playlist failed: {}", e);
                return originalStateDescription;
            }
            ArrayList<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            @Nullable
            Map<@NonNull String, @Nullable PlayList @Nullable []> playlistMap = playLists.playlists;
            if (playlistMap != null) {
                for (PlayList[] innerLists : playlistMap.values()) {
                    if (innerLists != null && innerLists.length > 0) {
                        PlayList playList = innerLists[0];
                        if (playList.playlistId != null && playList.title != null) {
                            options.add(new StateOption(playList.playlistId,
                                    String.format("%s (%d)", playList.title, playList.trackCount)));
                        }
                    }
                }
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        } else if (CHANNEL_TYPE_PLAY_ALARM_SOUND.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            Connection connection = handler.findConnection();
            if (connection == null) {
                return originalStateDescription;
            }
            Device device = handler.findDevice();
            if (device == null) {
                return originalStateDescription;
            }

            JsonNotificationSound[] notificationSounds;
            try {
                notificationSounds = connection.getNotificationSounds(device);
            } catch (IOException | HttpException | URISyntaxException | JsonSyntaxException | ConnectionException e) {
                logger.warn("Get notification sounds failed: {}", e);
                return originalStateDescription;
            }
            ArrayList<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));

            for (JsonNotificationSound notificationSound : notificationSounds) {
                if (notificationSound.folder == null && notificationSound.providerId != null
                        && notificationSound.id != null && notificationSound.displayName != null) {
                    String providerSoundId = notificationSound.providerId + ":" + notificationSound.id;
                    options.add(new StateOption(providerSoundId, notificationSound.displayName));
                }
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        } else if (CHANNEL_TYPE_CHANNEL_PLAY_ON_DEVICE.equals(channel.getChannelTypeUID())) {
            FlashBriefingProfileHandler handler = (FlashBriefingProfileHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            AccountHandler accountHandler = handler.findAccountHandler();
            if (accountHandler == null) {
                return originalStateDescription;
            }
            @NonNull
            List<@NonNull Device> devices = accountHandler.getLastKnownDevices();
            if (devices.size() == 0) {
                return originalStateDescription;
            }

            ArrayList<StateOption> options = new ArrayList<>();
            options.add(new StateOption("", ""));
            for (Device device : devices) {
                if (device.capabilities != null && Arrays.asList(device.capabilities).contains("FLASH_BRIEFING")) {
                    options.add(new StateOption(device.serialNumber, device.accountName));
                }
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        } else if (CHANNEL_TYPE_MUSIC_PROVIDER_ID.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            Connection connection = handler.findConnection();
            if (connection == null) {
                return originalStateDescription;
            }
            List<JsonMusicProvider> musicProviders = connection.getMusicProviders();

            ArrayList<StateOption> options = new ArrayList<>();
            for (JsonMusicProvider musicProvider : musicProviders) {
                @Nullable
                List<@Nullable String> properties = musicProvider.supportedProperties;
                String providerId = musicProvider.id;
                String displayName = musicProvider.displayName;
                if (properties != null && properties.contains("Alexa.Music.PlaySearchPhrase")
                        && StringUtils.isNotEmpty(providerId)
                        && StringUtils.equals(musicProvider.availability, "AVAILABLE")
                        && StringUtils.isNotEmpty(displayName)) {
                    options.add(new StateOption(providerId, displayName));
                }
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        } else if (CHANNEL_TYPE_START_COMMAND.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            if (handler == null) {
                return originalStateDescription;
            }
            AccountHandler account = handler.findAccount();
            if (account == null) {
                return originalStateDescription;
            }
            @NonNull
            List<@NonNull FlashBriefingProfileHandler> flashbriefings = account.getFlashBriefingProfileHandlers();
            if (flashbriefings.isEmpty()) {
                return originalStateDescription;
            }

            ArrayList<StateOption> options = new ArrayList<>();
            options.addAll(originalStateDescription.getOptions());

            for (FlashBriefingProfileHandler flashBriefing : flashbriefings) {
                String value = FLASH_BRIEFING_COMMAND_PREFIX + flashBriefing.getThing().getUID().getId();
                String displayName = flashBriefing.getThing().getLabel();
                options.add(new StateOption(value, displayName));
            }
            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        }
        return originalStateDescription;
    }
}
