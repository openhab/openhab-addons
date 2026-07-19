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

import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ClientUpdatesConfigMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ClientUpdatesConfigMessageOuterClass.ClientUpdatesConfigMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.Command;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandOptionsOuterClass.CommandOptions;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.DeviceClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.RepeatMode;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.ShuffleMode;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CryptoPairingMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CryptoPairingMessageOuterClass.CryptoPairingMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.DeviceInfoMessageOuterClass.DeviceInfoMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ModifyOutputContextRequestMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ModifyOutputContextRequestMessageOuterClass.ModifyOutputContextRequestMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ModifyOutputContextRequestMessageOuterClass.ModifyOutputContextRequestType;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueRequestMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueRequestMessageOuterClass.PlaybackQueueRequestMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ErrorCode;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendButtonEventMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendButtonEventMessageOuterClass.SendButtonEventMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandMessageOuterClass.SendCommandMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass.HandlerReturnStatus;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass.SendCommandResultMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendCommandResultMessageOuterClass.SendError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendHIDEventMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SendHIDEventMessageOuterClass.SendHIDEventMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetConnectionStateMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetConnectionStateMessageOuterClass.SetConnectionStateMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetVolumeMessageOuterClass;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetVolumeMessageOuterClass.SetVolumeMessage;
import org.openhab.binding.atv.internal.client.settings.InfoSettings;

import com.google.protobuf.ByteString;

/**
 * Builders for MRP protobuf messages, including hardcoded payload values (the virtual HID
 * event byte layout, the {@code com.apple.TVRemote} device information payload, etc.).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpMessages {

    private static final HexFormat HEX = HexFormat.of();

    /** Fixed mach AbsoluteTime prefix used by the virtual HID event payload. */
    private static final byte[] HID_ABSTIME = HEX.parseHex("438922cf08020000");
    /** Fixed middle section of the virtual HID event payload. */
    private static final byte[] HID_MIDDLE = HEX
            .parseHex("00000000000000000100000000000000020" + "00000200000000300000001000000000000");
    /** Fixed trailer of the virtual HID event payload. */
    private static final byte[] HID_TRAILER = HEX.parseHex("0000000000000001000000");

    private MrpMessages() {
    }

    /**
     * Creates a {@code ProtocolMessage} envelope.
     *
     * @param type message type
     * @return envelope with error code {@code NoError} and a random unique identifier
     */
    public static ProtocolMessage.Builder create(ProtocolMessage.Type type) {
        return create(type, null);
    }

    /**
     * Creates a {@code ProtocolMessage} envelope with a response identifier.
     *
     * @param type message type
     * @param identifier identifier correlating the message with a request, or {@code null}
     * @return envelope with error code {@code NoError} and a random unique identifier
     */
    public static ProtocolMessage.Builder create(ProtocolMessage.Type type, @Nullable String identifier) {
        ProtocolMessage.Builder message = ProtocolMessage.newBuilder().setType(type)
                .setErrorCode(ErrorCode.Enum.NoError).setUniqueIdentifier(UUID.randomUUID().toString().toUpperCase());
        if (identifier != null) {
            message.setIdentifier(identifier);
        }
        return message;
    }

    /**
     * Creates a new {@code DEVICE_INFO_MESSAGE}.
     *
     * @param info identity settings (name and OS build)
     * @param identifier our unique identifier (the pairing id)
     * @param update {@code true} for a {@code DEVICE_INFO_UPDATE_MESSAGE}
     * @return the message
     */
    public static ProtocolMessage deviceInformation(InfoSettings info, String identifier, boolean update) {
        ProtocolMessage.Type type = update ? ProtocolMessage.Type.DEVICE_INFO_UPDATE_MESSAGE
                : ProtocolMessage.Type.DEVICE_INFO_MESSAGE;
        DeviceInfoMessage inner = DeviceInfoMessage.newBuilder().setAllowsPairing(true)
                .setApplicationBundleIdentifier("com.apple.TVRemote").setApplicationBundleVersion("344.28")
                .setLastSupportedMessageType(108).setLocalizedModelName("iPhone").setName(info.name())
                .setProtocolVersion(1).setSharedQueueVersion(2).setSupportsACL(true).setSupportsExtendedMotion(true)
                .setSupportsSharedQueue(true).setSupportsSystemPairing(true).setSystemBuildVersion(info.osBuild())
                .setSystemMediaApplication("com.apple.TVMusic").setUniqueIdentifier(identifier)
                .setDeviceClass(DeviceClass.Enum.iPhone).setLogicalDeviceCount(1).build();
        return create(type).setExtension(DeviceInfoMessageOuterClass.deviceInfoMessage, inner).build();
    }

    /**
     * Creates a new {@code WAKE_DEVICE_MESSAGE}.
     *
     * @return the message
     */
    public static ProtocolMessage wakeDevice() {
        return create(ProtocolMessage.Type.WAKE_DEVICE_MESSAGE).build();
    }

    /**
     * Creates a new {@code SET_CONNECTION_STATE_MESSAGE} with state {@code Connected}.
     *
     * @return the message
     */
    public static ProtocolMessage setConnectionState() {
        SetConnectionStateMessage inner = SetConnectionStateMessage.newBuilder()
                .setState(SetConnectionStateMessage.ConnectionState.Connected).build();
        return create(ProtocolMessage.Type.SET_CONNECTION_STATE_MESSAGE)
                .setExtension(SetConnectionStateMessageOuterClass.setConnectionStateMessage, inner).build();
    }

    /**
     * Creates a new {@code GET_KEYBOARD_SESSION_MESSAGE}.
     *
     * @return the message
     */
    public static ProtocolMessage getKeyboardSession() {
        return create(ProtocolMessage.Type.GET_KEYBOARD_SESSION_MESSAGE).build();
    }

    /**
     * Creates a new {@code CRYPTO_PAIRING_MESSAGE}.
     *
     * @param pairingData already TLV8-encoded pairing data
     * @param isPairing {@code true} during pair-setup ({@code state} field 2), {@code false}
     *            during pair-verify
     * @return the message
     */
    public static ProtocolMessage cryptoPairing(byte[] pairingData, boolean isPairing) {
        CryptoPairingMessage inner = CryptoPairingMessage.newBuilder().setStatus(0)
                .setPairingData(ByteString.copyFrom(pairingData)).setIsRetrying(false).setIsUsingSystemPairing(false)
                .setState(isPairing ? 2 : 0).build();
        return create(ProtocolMessage.Type.CRYPTO_PAIRING_MESSAGE)
                .setExtension(CryptoPairingMessageOuterClass.cryptoPairingMessage, inner).build();
    }

    /**
     * Creates a new {@code CLIENT_UPDATES_CONFIG_MESSAGE} with the default subscriptions
     * (artwork, volume, keyboard and output-device updates but not now-playing updates).
     *
     * @return the message
     */
    public static ProtocolMessage clientUpdatesConfig() {
        return clientUpdatesConfig(true, false, true, true, true);
    }

    /**
     * Creates a new {@code CLIENT_UPDATES_CONFIG_MESSAGE}.
     *
     * @param artwork subscribe to artwork updates
     * @param nowPlaying subscribe to now-playing updates
     * @param volume subscribe to volume updates
     * @param keyboard subscribe to keyboard updates
     * @param outputDeviceUpdates subscribe to output-device updates
     * @return the message
     */
    public static ProtocolMessage clientUpdatesConfig(boolean artwork, boolean nowPlaying, boolean volume,
            boolean keyboard, boolean outputDeviceUpdates) {
        ClientUpdatesConfigMessage inner = ClientUpdatesConfigMessage.newBuilder().setArtworkUpdates(artwork)
                .setNowPlayingUpdates(nowPlaying).setVolumeUpdates(volume).setKeyboardUpdates(keyboard)
                .setOutputDeviceUpdates(outputDeviceUpdates).build();
        return create(ProtocolMessage.Type.CLIENT_UPDATES_CONFIG_MESSAGE)
                .setExtension(ClientUpdatesConfigMessageOuterClass.clientUpdatesConfigMessage, inner).build();
    }

    /**
     * Creates a new {@code PLAYBACK_QUEUE_REQUEST_MESSAGE}.
     *
     * @param location queue location to request
     * @param width requested artwork width (default -1)
     * @param height requested artwork height (default 400)
     * @return the message
     */
    public static ProtocolMessage playbackQueueRequest(int location, double width, double height) {
        PlaybackQueueRequestMessage inner = PlaybackQueueRequestMessage.newBuilder().setLocation(location).setLength(1)
                .setArtworkWidth(width).setArtworkHeight(height).setReturnContentItemAssetsInUserCompletion(true)
                .build();
        return create(ProtocolMessage.Type.PLAYBACK_QUEUE_REQUEST_MESSAGE)
                .setExtension(PlaybackQueueRequestMessageOuterClass.playbackQueueRequestMessage, inner).build();
    }

    /**
     * Creates a new {@code SEND_HID_EVENT_MESSAGE} carrying a virtual HID key press or
     * release.
     *
     * @param usePage HID usage page of the key
     * @param usage HID usage of the key
     * @param down {@code true} for key down, {@code false} for key up
     * @return the message
     */
    public static ProtocolMessage sendHidEvent(int usePage, int usage, boolean down) {
        byte[] data = new byte[6];
        data[0] = (byte) (usePage >>> 8);
        data[1] = (byte) usePage;
        data[2] = (byte) (usage >>> 8);
        data[3] = (byte) usage;
        data[4] = 0;
        data[5] = (byte) (down ? 1 : 0);

        byte[] eventData = new byte[HID_ABSTIME.length + HID_MIDDLE.length + data.length + HID_TRAILER.length];
        int pos = 0;
        System.arraycopy(HID_ABSTIME, 0, eventData, pos, HID_ABSTIME.length);
        pos += HID_ABSTIME.length;
        System.arraycopy(HID_MIDDLE, 0, eventData, pos, HID_MIDDLE.length);
        pos += HID_MIDDLE.length;
        System.arraycopy(data, 0, eventData, pos, data.length);
        pos += data.length;
        System.arraycopy(HID_TRAILER, 0, eventData, pos, HID_TRAILER.length);

        SendHIDEventMessage inner = SendHIDEventMessage.newBuilder().setHidEventData(ByteString.copyFrom(eventData))
                .build();
        return create(ProtocolMessage.Type.SEND_HID_EVENT_MESSAGE)
                .setExtension(SendHIDEventMessageOuterClass.sendHIDEventMessage, inner).build();
    }

    /**
     * Creates a new {@code SEND_BUTTON_EVENT_MESSAGE}.
     *
     * @param usagePage HID usage page of the button
     * @param usage HID usage of the button
     * @param buttonDown {@code true} for button down
     * @return the message
     */
    public static ProtocolMessage sendButton(int usagePage, int usage, boolean buttonDown) {
        SendButtonEventMessage inner = SendButtonEventMessage.newBuilder().setUsagePage(usagePage).setUsage(usage)
                .setButtonDown(buttonDown).build();
        return create(ProtocolMessage.Type.SEND_BUTTON_EVENT_MESSAGE)
                .setExtension(SendButtonEventMessageOuterClass.sendButtonEventMessage, inner).build();
    }

    /**
     * Creates a playback command request without options.
     *
     * @param command command to send
     * @return the message
     */
    public static ProtocolMessage command(Command command) {
        return command(command, options -> {
        });
    }

    /**
     * Creates a playback command request with options.
     *
     * @param command command to send
     * @param optionsCustomizer callback filling in {@code CommandInfo} options
     * @return the message
     */
    public static ProtocolMessage command(Command command, Consumer<CommandOptions.Builder> optionsCustomizer) {
        CommandOptions.Builder options = CommandOptions.newBuilder();
        optionsCustomizer.accept(options);
        SendCommandMessage inner = SendCommandMessage.newBuilder().setCommand(command).setOptions(options).build();
        return create(ProtocolMessage.Type.SEND_COMMAND_MESSAGE)
                .setExtension(SendCommandMessageOuterClass.sendCommandMessage, inner).build();
    }

    /**
     * Creates a successful playback command result.
     *
     * @param identifier identifier of the request being answered
     * @return the message
     */
    public static ProtocolMessage commandResult(String identifier) {
        return commandResult(identifier, SendError.Enum.NoError);
    }

    /**
     * Creates a playback command result.
     *
     * @param identifier identifier of the request being answered
     * @param sendError error to report
     * @return the message
     */
    public static ProtocolMessage commandResult(String identifier, SendError.Enum sendError) {
        SendCommandResultMessage inner = SendCommandResultMessage.newBuilder().setSendError(sendError)
                .setHandlerReturnStatus(HandlerReturnStatus.Enum.Success).build();
        return create(ProtocolMessage.Type.SEND_COMMAND_RESULT_MESSAGE, identifier)
                .setExtension(SendCommandResultMessageOuterClass.sendCommandResultMessage, inner).build();
    }

    /**
     * Creates a command changing the repeat mode of the current player.
     *
     * @param mode new repeat state
     * @return the message
     */
    public static ProtocolMessage repeat(RepeatState mode) {
        return command(Command.ChangeRepeatMode, options -> {
            options.setSendOptions(0);
            switch (mode) {
                case Off -> options.setRepeatMode(RepeatMode.Enum.Off);
                case Track -> options.setRepeatMode(RepeatMode.Enum.One);
                default -> options.setRepeatMode(RepeatMode.Enum.All);
            }
        });
    }

    /**
     * Creates a command changing the shuffle mode of the current player.
     *
     * @param state new shuffle state
     * @return the message
     */
    public static ProtocolMessage shuffle(ShuffleState state) {
        return command(Command.ChangeShuffleMode, options -> {
            options.setSendOptions(0);
            switch (state) {
                case Off -> options.setShuffleMode(ShuffleMode.Enum.Off);
                case Albums -> options.setShuffleMode(ShuffleMode.Enum.Albums);
                default -> options.setShuffleMode(ShuffleMode.Enum.Songs);
            }
        });
    }

    /**
     * Creates a command seeking to an absolute position in the stream.
     *
     * @param position position in seconds
     * @return the message
     */
    public static ProtocolMessage seekToPosition(double position) {
        return command(Command.SeekToPlaybackPosition, options -> options.setPlaybackPosition(position));
    }

    /**
     * Creates a new {@code SET_VOLUME_MESSAGE} changing volume on a device.
     *
     * @param deviceUid UID of the output device
     * @param volume new volume in range [0.0, 1.0]
     * @return the message
     */
    public static ProtocolMessage setVolume(String deviceUid, float volume) {
        SetVolumeMessage inner = SetVolumeMessage.newBuilder().setOutputDeviceUID(deviceUid).setVolume(volume).build();
        return create(ProtocolMessage.Type.SET_VOLUME_MESSAGE)
                .setExtension(SetVolumeMessageOuterClass.setVolumeMessage, inner).build();
    }

    /**
     * Creates a message adding AirPlay devices to the speaker group.
     *
     * @param deviceUids device identifiers to add
     * @return the message
     */
    public static ProtocolMessage addOutputDevices(List<String> deviceUids) {
        ModifyOutputContextRequestMessage.Builder inner = ModifyOutputContextRequestMessage.newBuilder()
                .setType(ModifyOutputContextRequestType.Enum.SharedAudioPresentation);
        for (String deviceUid : deviceUids) {
            inner.addAddingDevices(deviceUid);
            inner.addClusterAwareAddingDevices(deviceUid);
        }
        return create(ProtocolMessage.Type.MODIFY_OUTPUT_CONTEXT_REQUEST_MESSAGE).setExtension(
                ModifyOutputContextRequestMessageOuterClass.modifyOutputContextRequestMessage, inner.build()).build();
    }

    /**
     * Creates a message removing AirPlay devices from the speaker group.
     *
     * @param deviceUids device identifiers to remove
     * @return the message
     */
    public static ProtocolMessage removeOutputDevices(List<String> deviceUids) {
        ModifyOutputContextRequestMessage.Builder inner = ModifyOutputContextRequestMessage.newBuilder()
                .setType(ModifyOutputContextRequestType.Enum.SharedAudioPresentation);
        for (String deviceUid : deviceUids) {
            inner.addRemovingDevices(deviceUid);
            inner.addClusterAwareRemovingDevices(deviceUid);
        }
        return create(ProtocolMessage.Type.MODIFY_OUTPUT_CONTEXT_REQUEST_MESSAGE).setExtension(
                ModifyOutputContextRequestMessageOuterClass.modifyOutputContextRequestMessage, inner.build()).build();
    }

    /**
     * Creates a message setting the AirPlay devices of the speaker group.
     *
     * @param deviceUids device identifiers to use
     * @return the message
     */
    public static ProtocolMessage setOutputDevices(List<String> deviceUids) {
        ModifyOutputContextRequestMessage.Builder inner = ModifyOutputContextRequestMessage.newBuilder()
                .setType(ModifyOutputContextRequestType.Enum.SharedAudioPresentation);
        for (String deviceUid : deviceUids) {
            inner.addSettingDevices(deviceUid);
            inner.addClusterAwareSettingDevices(deviceUid);
        }
        return create(ProtocolMessage.Type.MODIFY_OUTPUT_CONTEXT_REQUEST_MESSAGE).setExtension(
                ModifyOutputContextRequestMessageOuterClass.modifyOutputContextRequestMessage, inner.build()).build();
    }
}
