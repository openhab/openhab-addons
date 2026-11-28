/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.AudioFadeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.AudioFadeResponseMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.AudioFormatSettingsMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ClientUpdatesConfigMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandOptionsOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ConfigureConnectionMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemMetadataOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CryptoPairingMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.GenericMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.GetKeyboardSessionMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.GetRemoteTextInputSessionMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.GetVolumeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.GetVolumeResultMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.KeyboardMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.LanguageOptionOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ModifyOutputContextRequestMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NotificationMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NowPlayingClientOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NowPlayingInfoOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NowPlayingPlayerOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.OriginClientPropertiesMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.OriginOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueCapabilitiesOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueContextOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueRequestMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlayerClientPropertiesMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlayerPathOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RegisterForGameControllerEventsMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RegisterHIDDeviceMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RegisterHIDDeviceResultMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RegisterVoiceInputDeviceMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RegisterVoiceInputDeviceResponseMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemoteTextInputMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemoveClientMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemoveEndpointsMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemoveOutputDevicesMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemovePlayerMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendButtonEventMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendHIDEventMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendPackedVirtualTouchEventMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendVoiceInputMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetArtworkMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetConnectionStateMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetDefaultSupportedCommandsMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetDiscoveryModeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetHiliteModeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetNowPlayingClientMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetNowPlayingPlayerMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetRecordingStateMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetStateMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetVolumeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SupportedCommandsOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.TextInputMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.TransactionKeyOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.TransactionMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.TransactionPacketOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.TransactionPacketsOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateClientMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateContentItemArtworkMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateContentItemMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateEndPointsMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateOutputDeviceMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdatePlayerPath;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VirtualTouchDeviceDescriptorMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VoiceInputDeviceDescriptorMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeControlAvailabilityMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeControlCapabilitiesDidChangeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.VolumeDidChangeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.WakeDeviceMessageOuterClass;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

/**
 * Extension handling for MRP {@link ProtocolMessage} envelopes.
 *
 * <p>
 * MRP wraps every message in a {@code ProtocolMessage} whose payload is a proto2
 * extension selected by the {@code type} field, including the special case where
 * {@code DEVICE_INFO_UPDATE_MESSAGE} reuses the {@code deviceInfoMessage} extension.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpExtensions {

    /**
     * Registry with every extension from all generated MRP proto files registered.
     * Must be passed to {@code ProtocolMessage.parseFrom} so inner messages are
     * parsed as extensions rather than kept as unknown fields.
     */
    public static final ExtensionRegistry EXTENSION_REGISTRY = createRegistry();

    /**
     * Maps a protocol message type to the extension carrying its inner payload. The value
     * type parameter is unbounded because one entry ({@code getKeyboardSessionMessage},
     * field 29) is a string-typed extension rather than a message-typed one.
     */
    private static final Map<ProtocolMessage.Type, GeneratedMessage.GeneratedExtension<ProtocolMessage, ?>> EXTENSION_LOOKUP = Map
            .ofEntries(Map.entry(ProtocolMessage.Type.AUDIO_FADE_MESSAGE, AudioFadeMessageOuterClass.audioFadeMessage),
                    Map.entry(ProtocolMessage.Type.AUDIO_FADE_RESPONSE_MESSAGE,
                            AudioFadeResponseMessageOuterClass.audioFadeResponseMessage),
                    Map.entry(ProtocolMessage.Type.CLIENT_UPDATES_CONFIG_MESSAGE,
                            ClientUpdatesConfigMessageOuterClass.clientUpdatesConfigMessage),
                    Map.entry(ProtocolMessage.Type.CONFIGURE_CONNECTION_MESSAGE,
                            ConfigureConnectionMessageOuterClass.configureConnectionMessage),
                    Map.entry(ProtocolMessage.Type.CRYPTO_PAIRING_MESSAGE,
                            CryptoPairingMessageOuterClass.cryptoPairingMessage),
                    Map.entry(ProtocolMessage.Type.DEVICE_INFO_MESSAGE, DeviceInfoMessageOuterClass.deviceInfoMessage),
                    Map.entry(ProtocolMessage.Type.DEVICE_INFO_UPDATE_MESSAGE,
                            DeviceInfoMessageOuterClass.deviceInfoMessage),
                    Map.entry(ProtocolMessage.Type.GENERIC_MESSAGE, GenericMessageOuterClass.genericMessage),
                    Map.entry(ProtocolMessage.Type.GET_KEYBOARD_SESSION_MESSAGE,
                            GetKeyboardSessionMessageOuterClass.getKeyboardSessionMessage),
                    Map.entry(ProtocolMessage.Type.GET_REMOTE_TEXT_INPUT_SESSION_MESSAGE,
                            GetRemoteTextInputSessionMessageOuterClass.getRemoteTextInputSessionMessage),
                    Map.entry(ProtocolMessage.Type.GET_VOLUME_MESSAGE, GetVolumeMessageOuterClass.getVolumeMessage),
                    Map.entry(ProtocolMessage.Type.GET_VOLUME_RESULT_MESSAGE,
                            GetVolumeResultMessageOuterClass.getVolumeResultMessage),
                    Map.entry(ProtocolMessage.Type.KEYBOARD_MESSAGE, KeyboardMessageOuterClass.keyboardMessage),
                    Map.entry(ProtocolMessage.Type.MODIFY_OUTPUT_CONTEXT_REQUEST_MESSAGE,
                            ModifyOutputContextRequestMessageOuterClass.modifyOutputContextRequestMessage),
                    Map.entry(ProtocolMessage.Type.NOTIFICATION_MESSAGE,
                            NotificationMessageOuterClass.notificationMessage),
                    Map.entry(ProtocolMessage.Type.ORIGIN_CLIENT_PROPERTIES_MESSAGE,
                            OriginClientPropertiesMessageOuterClass.originClientPropertiesMessage),
                    Map.entry(ProtocolMessage.Type.PLAYBACK_QUEUE_REQUEST_MESSAGE,
                            PlaybackQueueRequestMessageOuterClass.playbackQueueRequestMessage),
                    Map.entry(ProtocolMessage.Type.PLAYER_CLIENT_PROPERTIES_MESSAGE,
                            PlayerClientPropertiesMessageOuterClass.playerClientPropertiesMessage),
                    Map.entry(ProtocolMessage.Type.REGISTER_FOR_GAME_CONTROLLER_EVENTS_MESSAGE,
                            RegisterForGameControllerEventsMessageOuterClass.registerForGameControllerEventsMessage),
                    Map.entry(ProtocolMessage.Type.REGISTER_HID_DEVICE_MESSAGE,
                            RegisterHIDDeviceMessageOuterClass.registerHIDDeviceMessage),
                    Map.entry(ProtocolMessage.Type.REGISTER_HID_DEVICE_RESULT_MESSAGE,
                            RegisterHIDDeviceResultMessageOuterClass.registerHIDDeviceResultMessage),
                    Map.entry(ProtocolMessage.Type.REGISTER_VOICE_INPUT_DEVICE_MESSAGE,
                            RegisterVoiceInputDeviceMessageOuterClass.registerVoiceInputDeviceMessage),
                    Map.entry(ProtocolMessage.Type.REGISTER_VOICE_INPUT_DEVICE_RESPONSE_MESSAGE,
                            RegisterVoiceInputDeviceResponseMessageOuterClass.registerVoiceInputDeviceResponseMessage),
                    Map.entry(ProtocolMessage.Type.REMOTE_TEXT_INPUT_MESSAGE,
                            RemoteTextInputMessageOuterClass.remoteTextInputMessage),
                    Map.entry(ProtocolMessage.Type.REMOVE_CLIENT_MESSAGE,
                            RemoveClientMessageOuterClass.removeClientMessage),
                    Map.entry(ProtocolMessage.Type.REMOVE_ENDPOINTS_MESSAGE,
                            RemoveEndpointsMessageOuterClass.removeEndpointsMessage),
                    Map.entry(ProtocolMessage.Type.REMOVE_OUTPUT_DEVICES_MESSAGE,
                            RemoveOutputDevicesMessageOuterClass.removeOutputDevicesMessage),
                    Map.entry(ProtocolMessage.Type.REMOVE_PLAYER_MESSAGE,
                            RemovePlayerMessageOuterClass.removePlayerMessage),
                    Map.entry(ProtocolMessage.Type.SEND_BUTTON_EVENT_MESSAGE,
                            SendButtonEventMessageOuterClass.sendButtonEventMessage),
                    Map.entry(ProtocolMessage.Type.SEND_COMMAND_MESSAGE,
                            SendCommandMessageOuterClass.sendCommandMessage),
                    Map.entry(ProtocolMessage.Type.SEND_COMMAND_RESULT_MESSAGE,
                            SendCommandResultMessageOuterClass.sendCommandResultMessage),
                    Map.entry(ProtocolMessage.Type.SEND_HID_EVENT_MESSAGE,
                            SendHIDEventMessageOuterClass.sendHIDEventMessage),
                    Map.entry(ProtocolMessage.Type.SEND_PACKED_VIRTUAL_TOUCH_EVENT_MESSAGE,
                            SendPackedVirtualTouchEventMessageOuterClass.sendPackedVirtualTouchEventMessage),
                    Map.entry(ProtocolMessage.Type.SEND_VOICE_INPUT_MESSAGE,
                            SendVoiceInputMessageOuterClass.sendVoiceInputMessage),
                    Map.entry(ProtocolMessage.Type.SET_ARTWORK_MESSAGE, SetArtworkMessageOuterClass.setArtworkMessage),
                    Map.entry(ProtocolMessage.Type.SET_CONNECTION_STATE_MESSAGE,
                            SetConnectionStateMessageOuterClass.setConnectionStateMessage),
                    Map.entry(ProtocolMessage.Type.SET_DEFAULT_SUPPORTED_COMMANDS_MESSAGE,
                            SetDefaultSupportedCommandsMessageOuterClass.setDefaultSupportedCommandsMessage),
                    Map.entry(ProtocolMessage.Type.SET_DISCOVERY_MODE_MESSAGE,
                            SetDiscoveryModeMessageOuterClass.setDiscoveryModeMessage),
                    Map.entry(ProtocolMessage.Type.SET_HILITE_MODE_MESSAGE,
                            SetHiliteModeMessageOuterClass.setHiliteModeMessage),
                    Map.entry(ProtocolMessage.Type.SET_NOW_PLAYING_CLIENT_MESSAGE,
                            SetNowPlayingClientMessageOuterClass.setNowPlayingClientMessage),
                    Map.entry(ProtocolMessage.Type.SET_NOW_PLAYING_PLAYER_MESSAGE,
                            SetNowPlayingPlayerMessageOuterClass.setNowPlayingPlayerMessage),
                    Map.entry(ProtocolMessage.Type.SET_RECORDING_STATE_MESSAGE,
                            SetRecordingStateMessageOuterClass.setRecordingStateMessage),
                    Map.entry(ProtocolMessage.Type.SET_STATE_MESSAGE, SetStateMessageOuterClass.setStateMessage),
                    Map.entry(ProtocolMessage.Type.SET_VOLUME_MESSAGE, SetVolumeMessageOuterClass.setVolumeMessage),
                    Map.entry(ProtocolMessage.Type.TEXT_INPUT_MESSAGE, TextInputMessageOuterClass.textInputMessage),
                    Map.entry(ProtocolMessage.Type.TRANSACTION_MESSAGE,
                            TransactionMessageOuterClass.transactionMessage),
                    Map.entry(ProtocolMessage.Type.UPDATE_CLIENT_MESSAGE,
                            UpdateClientMessageOuterClass.updateClientMessage),
                    Map.entry(ProtocolMessage.Type.UPDATE_CONTENT_ITEM_ARTWORK_MESSAGE,
                            UpdateContentItemArtworkMessageOuterClass.updateContentItemArtworkMessage),
                    Map.entry(ProtocolMessage.Type.UPDATE_CONTENT_ITEM_MESSAGE,
                            UpdateContentItemMessageOuterClass.updateContentItemMessage),
                    Map.entry(ProtocolMessage.Type.UPDATE_END_POINTS_MESSAGE,
                            UpdateEndPointsMessageOuterClass.updateEndPointsMessage),
                    Map.entry(ProtocolMessage.Type.UPDATE_OUTPUT_DEVICE_MESSAGE,
                            UpdateOutputDeviceMessageOuterClass.updateOutputDeviceMessage),
                    Map.entry(ProtocolMessage.Type.VOLUME_CONTROL_AVAILABILITY_MESSAGE,
                            VolumeControlAvailabilityMessageOuterClass.volumeControlAvailabilityMessage),
                    Map.entry(ProtocolMessage.Type.VOLUME_CONTROL_CAPABILITIES_DID_CHANGE_MESSAGE,
                            VolumeControlCapabilitiesDidChangeMessageOuterClass.volumeControlCapabilitiesDidChangeMessage),
                    Map.entry(ProtocolMessage.Type.VOLUME_DID_CHANGE_MESSAGE,
                            VolumeDidChangeMessageOuterClass.volumeDidChangeMessage),
                    Map.entry(ProtocolMessage.Type.WAKE_DEVICE_MESSAGE, WakeDeviceMessageOuterClass.wakeDeviceMessage));

    private MrpExtensions() {
    }

    private static ExtensionRegistry createRegistry() {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        AudioFadeMessageOuterClass.registerAllExtensions(registry);
        AudioFadeResponseMessageOuterClass.registerAllExtensions(registry);
        AudioFormatSettingsMessage.registerAllExtensions(registry);
        ClientUpdatesConfigMessageOuterClass.registerAllExtensions(registry);
        CommandInfoOuterClass.registerAllExtensions(registry);
        CommandOptionsOuterClass.registerAllExtensions(registry);
        Common.registerAllExtensions(registry);
        ConfigureConnectionMessageOuterClass.registerAllExtensions(registry);
        ContentItemMetadataOuterClass.registerAllExtensions(registry);
        ContentItemOuterClass.registerAllExtensions(registry);
        CryptoPairingMessageOuterClass.registerAllExtensions(registry);
        DeviceInfoMessageOuterClass.registerAllExtensions(registry);
        GenericMessageOuterClass.registerAllExtensions(registry);
        GetKeyboardSessionMessageOuterClass.registerAllExtensions(registry);
        GetRemoteTextInputSessionMessageOuterClass.registerAllExtensions(registry);
        GetVolumeMessageOuterClass.registerAllExtensions(registry);
        GetVolumeResultMessageOuterClass.registerAllExtensions(registry);
        KeyboardMessageOuterClass.registerAllExtensions(registry);
        LanguageOptionOuterClass.registerAllExtensions(registry);
        ModifyOutputContextRequestMessageOuterClass.registerAllExtensions(registry);
        NotificationMessageOuterClass.registerAllExtensions(registry);
        NowPlayingClientOuterClass.registerAllExtensions(registry);
        NowPlayingInfoOuterClass.registerAllExtensions(registry);
        NowPlayingPlayerOuterClass.registerAllExtensions(registry);
        OriginClientPropertiesMessageOuterClass.registerAllExtensions(registry);
        OriginOuterClass.registerAllExtensions(registry);
        PlaybackQueueCapabilitiesOuterClass.registerAllExtensions(registry);
        PlaybackQueueContextOuterClass.registerAllExtensions(registry);
        PlaybackQueueOuterClass.registerAllExtensions(registry);
        PlaybackQueueRequestMessageOuterClass.registerAllExtensions(registry);
        PlayerClientPropertiesMessageOuterClass.registerAllExtensions(registry);
        PlayerPathOuterClass.registerAllExtensions(registry);
        ProtocolMessageOuterClass.registerAllExtensions(registry);
        RegisterForGameControllerEventsMessageOuterClass.registerAllExtensions(registry);
        RegisterHIDDeviceMessageOuterClass.registerAllExtensions(registry);
        RegisterHIDDeviceResultMessageOuterClass.registerAllExtensions(registry);
        RegisterVoiceInputDeviceMessageOuterClass.registerAllExtensions(registry);
        RegisterVoiceInputDeviceResponseMessageOuterClass.registerAllExtensions(registry);
        RemoteTextInputMessageOuterClass.registerAllExtensions(registry);
        RemoveClientMessageOuterClass.registerAllExtensions(registry);
        RemoveEndpointsMessageOuterClass.registerAllExtensions(registry);
        RemoveOutputDevicesMessageOuterClass.registerAllExtensions(registry);
        RemovePlayerMessageOuterClass.registerAllExtensions(registry);
        SendButtonEventMessageOuterClass.registerAllExtensions(registry);
        SendCommandMessageOuterClass.registerAllExtensions(registry);
        SendCommandResultMessageOuterClass.registerAllExtensions(registry);
        SendHIDEventMessageOuterClass.registerAllExtensions(registry);
        SendPackedVirtualTouchEventMessageOuterClass.registerAllExtensions(registry);
        SendVoiceInputMessageOuterClass.registerAllExtensions(registry);
        SetArtworkMessageOuterClass.registerAllExtensions(registry);
        SetConnectionStateMessageOuterClass.registerAllExtensions(registry);
        SetDefaultSupportedCommandsMessageOuterClass.registerAllExtensions(registry);
        SetDiscoveryModeMessageOuterClass.registerAllExtensions(registry);
        SetHiliteModeMessageOuterClass.registerAllExtensions(registry);
        SetNowPlayingClientMessageOuterClass.registerAllExtensions(registry);
        SetNowPlayingPlayerMessageOuterClass.registerAllExtensions(registry);
        SetRecordingStateMessageOuterClass.registerAllExtensions(registry);
        SetStateMessageOuterClass.registerAllExtensions(registry);
        SetVolumeMessageOuterClass.registerAllExtensions(registry);
        SupportedCommandsOuterClass.registerAllExtensions(registry);
        TextInputMessageOuterClass.registerAllExtensions(registry);
        TransactionKeyOuterClass.registerAllExtensions(registry);
        TransactionMessageOuterClass.registerAllExtensions(registry);
        TransactionPacketOuterClass.registerAllExtensions(registry);
        TransactionPacketsOuterClass.registerAllExtensions(registry);
        UpdateClientMessageOuterClass.registerAllExtensions(registry);
        UpdateContentItemArtworkMessageOuterClass.registerAllExtensions(registry);
        UpdateContentItemMessageOuterClass.registerAllExtensions(registry);
        UpdateEndPointsMessageOuterClass.registerAllExtensions(registry);
        UpdateOutputDeviceMessageOuterClass.registerAllExtensions(registry);
        UpdatePlayerPath.registerAllExtensions(registry);
        VirtualTouchDeviceDescriptorMessage.registerAllExtensions(registry);
        VoiceInputDeviceDescriptorMessage.registerAllExtensions(registry);
        VolumeControlAvailabilityMessageOuterClass.registerAllExtensions(registry);
        VolumeControlCapabilitiesDidChangeMessageOuterClass.registerAllExtensions(registry);
        VolumeDidChangeMessageOuterClass.registerAllExtensions(registry);
        WakeDeviceMessageOuterClass.registerAllExtensions(registry);
        return registry;
    }

    /**
     * Extracts the inner message of a protocol message based on its type.
     *
     * <p>
     * Returns the extension value, which is the default (empty) instance when the
     * extension is not set on the message.
     *
     * <p>
     * {@code GET_KEYBOARD_SESSION_MESSAGE} is the one type whose extension is a plain
     * string, not a message; use {@link #extractInnerValue(ProtocolMessage)} to read it.
     *
     * @param message the envelope to extract from
     * @return the inner extension message, never {@code null}
     * @throws IllegalArgumentException if no extension is known for the message type
     * @throws IllegalStateException if the extension for the type is not message-typed
     */
    public static Message extractInner(ProtocolMessage message) {
        if (extractInnerValue(message) instanceof Message inner) {
            return inner;
        }
        throw new IllegalStateException("extension for " + message.getType() + " is not a message");
    }

    /**
     * Extracts the inner extension value of a protocol message based on its type. The
     * result is a {@link Message} for all types except
     * {@code GET_KEYBOARD_SESSION_MESSAGE}, whose extension is a {@link String}.
     *
     * @param message the envelope to extract from
     * @return the inner extension value, never {@code null}
     * @throws IllegalArgumentException if no extension is known for the message type
     */
    public static Object extractInnerValue(ProtocolMessage message) {
        GeneratedMessage.GeneratedExtension<ProtocolMessage, ?> extension = EXTENSION_LOOKUP.get(message.getType());
        if (extension == null) {
            throw new IllegalArgumentException("unknown type: " + message.getType());
        }
        return message.getExtension(extension);
    }

    /**
     * Returns whether an inner message extension is known for the given type.
     *
     * @param type the protocol message type
     * @return {@code true} if {@link #extractInner(ProtocolMessage)} can handle it
     */
    public static boolean hasExtension(ProtocolMessage.Type type) {
        return EXTENSION_LOOKUP.containsKey(type);
    }
}
