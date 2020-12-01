/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.simpleip;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.AbstractThingHandler;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thing handler for a Sony Simple IP device. This is the entry point provides a full two interaction between
 * openhab and the simple IP system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SimpleIpHandler extends AbstractThingHandler<SimpleIpConfig> {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SimpleIpHandler.class);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<@Nullable SimpleIpProtocol> protocolHandler = new AtomicReference<>();

    /** The transformation service to use to transform the MAP file */
    private final @Nullable TransformationService transformationService;

    /**
     * Constructs the handler from the {@link Thing} and {@link TransformationService}
     *
     * @param thing a non-null {@link Thing} the handler is for
     * @param transformationService a possibly null {@link TransformationService} to use to transform MAP file
     */
    public SimpleIpHandler(final Thing thing, final @Nullable TransformationService transformationService) {
        super(thing, SimpleIpConfig.class);

        Objects.requireNonNull(thing, "thing cannot be null");
        this.transformationService = transformationService;
    }

    @Override
    protected void handleRefreshCommand(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        final String id = channelUID.getId();

        Validate.notEmpty(id, "id cannot be empty");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final SimpleIpProtocol handler = protocolHandler.get();
        if (handler == null) {
            logger.debug("Protocol handler wasn't set for handleRefresh - ignoring '{}'", id);
            return;
        }

        switch (id) {
            case SimpleIpConstants.CHANNEL_POWER:
                handler.refreshPower();
                break;
            case SimpleIpConstants.CHANNEL_VOLUME:
                handler.refreshVolume();
                break;
            case SimpleIpConstants.CHANNEL_AUDIOMUTE:
                handler.refreshAudioMute();
                break;
            case SimpleIpConstants.CHANNEL_CHANNEL:
                handler.refreshChannel();
                break;
            case SimpleIpConstants.CHANNEL_TRIPLETCHANNEL:
                handler.refreshTripletChannel();
                break;
            case SimpleIpConstants.CHANNEL_INPUTSOURCE:
                handler.refreshInputSource();
                break;
            case SimpleIpConstants.CHANNEL_INPUT:
                handler.refreshInput();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREMUTE:
                handler.refreshPictureMute();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREINPICTURE:
                handler.refreshPictureInPicture();
                break;

        }
    }

    @Override
    protected void handleSetCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final String id = channelUID.getId();

        final SimpleIpProtocol handler = protocolHandler.get();
        if (handler == null) {
            logger.debug("Protocol handler wasn't set for handleCommand - ignoring '{}': '{}'", channelUID, command);
            return;
        }

        switch (id) {
            case SimpleIpConstants.CHANNEL_IR:
                if (command instanceof StringType) {
                    handler.setIR(command.toString());
                } else {
                    logger.debug("Received a IR channel command with a non StringType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    handler.setPower(command == OnOffType.ON);
                } else {
                    logger.debug("Received a POWER channel command with a non OnOffType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_TOGGLEPOWER:
                handler.togglePower();
                break;

            case SimpleIpConstants.CHANNEL_VOLUME:
                if (command instanceof OnOffType) {
                    handler.setAudioMute(command == OnOffType.ON);
                } else if (command instanceof IncreaseDecreaseType) {
                    handler.setIR(command == IncreaseDecreaseType.INCREASE ? "Volume Up" : "Volume Down");
                } else if (command instanceof PercentType) {
                    handler.setAudioVolume(((PercentType) command).intValue());
                } else {
                    logger.debug(
                            "Received a AUDIO VOLUME channel command with a non OnOffType/IncreaseDecreaseType/PercentType: {}",
                            command);
                }

                break;

            case SimpleIpConstants.CHANNEL_AUDIOMUTE:
                if (command instanceof OnOffType) {
                    handler.setAudioMute(command == OnOffType.ON);
                } else {
                    logger.debug("Received a AUDIO MUTE channel command with a non OnOffType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_CHANNEL:
                if (command instanceof StringType) {
                    handler.setChannel(command.toString());
                } else {
                    logger.debug("Received a CHANNEL channel command with a non StringType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_TRIPLETCHANNEL:
                if (command instanceof StringType) {
                    handler.setTripletChannel(command.toString());
                } else {
                    logger.debug("Received a TRIPLET CHANNEL channel command with a non StringType: {}", command);
                }

                break;
            case SimpleIpConstants.CHANNEL_INPUTSOURCE:
                if (command instanceof StringType) {
                    handler.setInputSource(command.toString());
                } else {
                    logger.debug("Received a INPUT SOURCE channel command with a non StringType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_INPUT:
                if (command instanceof StringType) {
                    handler.setInput(command.toString());
                } else {
                    logger.debug("Received a INPUT channel command with a non StringType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_SCENE:
                if (command instanceof StringType) {
                    handler.setScene(command.toString());
                } else {
                    logger.debug("Received a SCENE channel command with a non StringType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_PICTUREMUTE:
                if (command instanceof OnOffType) {
                    handler.setPictureMute(command == OnOffType.ON);
                } else {
                    logger.debug("Received a PICTURE MUTE channel command with a non OnOffType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPICTUREMUTE:
                handler.togglePictureMute();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREINPICTURE:
                if (command instanceof OnOffType) {
                    handler.setPictureInPicture(command == OnOffType.ON);
                } else {
                    logger.debug("Received a PICTURE IN PICTURE channel command with a non OnOffType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPICTUREINPICTURE:
                handler.togglePictureInPicture();
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPIPPOSITION:
                handler.togglePipPosition();
                break;

            default:
                logger.debug("Unknown/Unsupported Channel id: {}", id);
                break;
        }
    }

    @Override
    public void refreshState(boolean initial) {
        final SimpleIpProtocol protocol = protocolHandler.get();
        if (protocol != null) {
            protocol.refreshState(true);
        }
    }

    /**
     * Attempts to connect to the system via {@link SimpleIpProtocol#login()}. Once completed, a ping job will be
     * created
     * to keep the connection
     * alive and a refresh job to refresh state (although the broadcast address and mac address is refreshed immediately
     * and only once). If a connection cannot be established (or login failed), the connection attempt will be retried
     * later
     */
    @Override
    protected void connect() {
        final SimpleIpConfig config = getSonyConfig();

        if (StringUtils.isEmpty(config.getDeviceIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address of Sony is missing from configuration");
            return;
        }

        try {
            SonyUtil.checkInterrupt();

            final SimpleIpProtocol localProtocolHandler = new SimpleIpProtocol(config, transformationService,
                    new ThingCallback<String>() {
                        @Override
                        public void statusChanged(final ThingStatus status, final ThingStatusDetail detail,
                                final @Nullable String msg) {
                            updateStatus(status, detail, msg);
                        }

                        @Override
                        public void stateChanged(final String channelId, final State state) {
                            updateState(channelId, state);
                        }

                        @Override
                        public void setProperty(final String propertyName, final @Nullable String propertyValue) {
                            getThing().setProperty(propertyName, propertyValue);
                        }
                    });

            SonyUtil.checkInterrupt();
            final String response = localProtocolHandler.login();
            if (response == null) {
                SonyUtil.close(protocolHandler.getAndSet(localProtocolHandler));

                logger.debug("Simple IP TV System now connected");
                updateStatus(ThingStatus.ONLINE);

                SonyUtil.checkInterrupt();
                localProtocolHandler.postLogin();

                SonyUtil.checkInterrupt();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
            }

        } catch (final IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to simple IP tv");
        } catch (final InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Initialization was interrupted");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        SonyUtil.close(protocolHandler.getAndSet(null));
    }
}
