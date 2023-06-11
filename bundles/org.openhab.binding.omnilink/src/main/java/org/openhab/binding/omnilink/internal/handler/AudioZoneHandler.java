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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.AudioPlayer;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAudioZoneStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link AudioZoneHandler} defines some methods that are used to
 * interface with an OmniLink Audio Zone. This by extension also defines the
 * Audio Zone thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class AudioZoneHandler extends AbstractOmnilinkStatusHandler<ExtendedAudioZoneStatus> {
    private final Logger logger = LoggerFactory.getLogger(AudioZoneHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public AudioZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateAudioZoneProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Audio Zone!");
        }
    }

    private void updateAudioZoneProperties(OmnilinkBridgeHandler bridgeHandler) {
        ObjectPropertyRequest<AudioZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.AUDIO_ZONE, thingID, 0).selectNamed().build();

        for (AudioZoneProperties audioZoneProperties : objectPropertyRequest) {
            updateProperty(THING_PROPERTIES_NAME, audioZoneProperties.getName());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_AUDIO_ZONE_POWER:
                if (command instanceof OnOffType) {
                    sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_ON_AND_MUTE,
                            OnOffType.OFF.equals(command) ? 0 : 1, thingID);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            case CHANNEL_AUDIO_ZONE_MUTE:
                if (command instanceof OnOffType) {
                    sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_ON_AND_MUTE,
                            OnOffType.OFF.equals(command) ? 2 : 3, thingID);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            case CHANNEL_AUDIO_ZONE_VOLUME:
                if (command instanceof PercentType) {
                    sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_VOLUME, ((PercentType) command).intValue(),
                            thingID);
                } else {
                    logger.debug("Invalid command: {}, must be PercentType", command);
                }
                break;
            case CHANNEL_AUDIO_ZONE_SOURCE:
                if (command instanceof DecimalType) {
                    sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_SOURCE, ((DecimalType) command).intValue(),
                            thingID);
                } else {
                    logger.debug("Invalid command: {}, must be DecimalType", command);
                }
                break;
            case CHANNEL_AUDIO_ZONE_CONTROL:
                if (command instanceof PlayPauseType) {
                    handlePlayPauseCommand(channelUID, (PlayPauseType) command);
                } else if (command instanceof NextPreviousType) {
                    handleNextPreviousCommand(channelUID, (NextPreviousType) command);
                } else {
                    logger.debug("Invalid command: {}, must be PlayPauseType or NextPreviousType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Audio Zone thing: {}", channelUID);
        }
    }

    private void handlePlayPauseCommand(ChannelUID channelUID, PlayPauseType command) {
        logger.debug("handlePlayPauseCommand called for channel: {}, command: {}", channelUID, command);
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();

        if (bridgeHandler != null) {
            Optional<AudioPlayer> audioPlayer = bridgeHandler.getAudioPlayer();
            if (audioPlayer.isPresent()) {
                AudioPlayer player = audioPlayer.get();
                sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_SOURCE,
                        PlayPauseType.PLAY.equals(command) ? player.getPlayCommand() : player.getPauseCommand(),
                        thingID);
            } else {
                logger.warn("No Audio Player was detected!");
            }
        } else {
            logger.debug("Received null bridge while sending Audio Zone command!");
        }
    }

    private void handleNextPreviousCommand(ChannelUID channelUID, NextPreviousType command) {
        logger.debug("handleNextPreviousCommand called for channel: {}, command: {}", channelUID, command);
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();

        if (bridgeHandler != null) {
            Optional<AudioPlayer> audioPlayer = bridgeHandler.getAudioPlayer();
            if (audioPlayer.isPresent()) {
                AudioPlayer player = audioPlayer.get();
                sendOmnilinkCommand(CommandMessage.CMD_AUDIO_ZONE_SET_SOURCE,
                        NextPreviousType.NEXT.equals(command) ? player.getNextCommand() : player.getPreviousCommand(),
                        thingID);
            } else {
                logger.warn("Audio Player could not be found!");
            }
        } else {
            logger.debug("Received null bridge while sending Audio Zone command!");
        }
    }

    @Override
    public void updateChannels(ExtendedAudioZoneStatus status) {
        logger.debug("updateChannels called for Audio Zone status: {}", status);
        updateState(CHANNEL_AUDIO_ZONE_POWER, OnOffType.from(status.isPower()));
        updateState(CHANNEL_AUDIO_ZONE_MUTE, OnOffType.from(status.isMute()));
        updateState(CHANNEL_AUDIO_ZONE_VOLUME, new PercentType(status.getVolume()));
        updateState(CHANNEL_AUDIO_ZONE_SOURCE, new DecimalType(status.getSource()));
    }

    @Override
    protected Optional<ExtendedAudioZoneStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
            if (bridgeHandler != null) {
                ObjectStatus objStatus = bridgeHandler.requestObjectStatus(Message.OBJ_TYPE_AUDIO_ZONE, thingID,
                        thingID, true);
                return Optional.of((ExtendedAudioZoneStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Audio Zone status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Audio Zone status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
