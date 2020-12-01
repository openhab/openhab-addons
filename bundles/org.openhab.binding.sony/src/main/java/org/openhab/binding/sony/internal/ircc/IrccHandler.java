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
package org.openhab.binding.sony.internal.ircc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ProcessingException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.AbstractThingHandler;
import org.openhab.binding.sony.internal.LoginUnsuccessfulResponse;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thing handler for a Sony Ircc device. This is the entry point provides a full two interaction between openhab
 * and the ircc system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccHandler extends AbstractThingHandler<IrccConfig> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(IrccHandler.class);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<@Nullable IrccProtocol<ThingCallback<String>>> protocolHandler = new AtomicReference<>();

    /** The transformation service to use to transform the MAP file */
    private final @Nullable TransformationService transformationService;

    /**
     * Constructs the handler from the {@link Thing} and {@link TransformationService}
     *
     * @param thing a non-null {@link Thing} the handler is for
     * @param transformationService a possibly null {@link TransformationService} to use to transform MAP file
     */
    public IrccHandler(final Thing thing, final @Nullable TransformationService transformationService) {
        super(thing, IrccConfig.class);

        Objects.requireNonNull(thing, "thing cannot be null");
        this.transformationService = transformationService;
    }

    @Override
    protected void handleRefreshCommand(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final IrccProtocol<ThingCallback<String>> localProtocolHandler = protocolHandler.get();
        if (localProtocolHandler == null) {
            logger.debug("Trying to handle a refresh command before a protocol handler has been created");
            return;
        }

        final String groupId = channelUID.getGroupId();
        final String channelId = channelUID.getIdWithoutGroup();

        if (groupId == null) {
            logger.debug("Called with a null group id - ignoring");
            return;
        }

        switch (groupId) {
            case IrccConstants.GRP_PRIMARY:
                switch (channelId) {
                    case IrccConstants.CHANNEL_CONTENTURL:
                        localProtocolHandler.refreshContentUrl();
                        break;
                    case IrccConstants.CHANNEL_TEXT:
                        localProtocolHandler.refreshText();
                        break;
                    case IrccConstants.CHANNEL_INTEXT:
                        localProtocolHandler.refreshInText();
                        break;
                    case IrccConstants.CHANNEL_INBROWSER:
                        localProtocolHandler.refreshInBrowser();
                        break;
                    case IrccConstants.CHANNEL_ISVIEWING:
                        localProtocolHandler.refreshIsViewing();
                        break;
                    default:
                        break;
                }
                break;

            case IrccConstants.GRP_VIEWING:
                localProtocolHandler.refreshStatus();
                break;

            case IrccConstants.GRP_CONTENT:
                localProtocolHandler.refreshContentInformation();
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleSetCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final IrccProtocol<ThingCallback<String>> localProtocolHandler = protocolHandler.get();
        if (localProtocolHandler == null) {
            logger.debug("Trying to handle a channel command before a protocol handler has been created");
            return;
        }

        final String groupId = channelUID.getGroupId();
        final String channelId = channelUID.getIdWithoutGroup();

        if (groupId == null) {
            logger.debug("Called with a null group id - ignoring");
            return;
        }

        switch (groupId) {
            case IrccConstants.GRP_PRIMARY:
                switch (channelId) {
                    case IrccConstants.CHANNEL_CMD:
                        if (command instanceof StringType) {
                            if (StringUtils.isEmpty(command.toString())) {
                                logger.debug("Received a COMMAND channel command that is empty - ignoring");
                            } else {
                                localProtocolHandler.sendCommand(command.toString());
                            }
                        } else {
                            logger.debug("Received a COMMAND channel command with a non StringType: {}", command);
                        }
                        break;

                    case IrccConstants.CHANNEL_POWER:
                        if (command instanceof OnOffType) {
                            localProtocolHandler.sendPower(OnOffType.ON == command);
                        } else {
                            logger.debug("Received a POWER channel command with a non OnOffType: {}", command);
                        }
                        break;
                    case IrccConstants.CHANNEL_CONTENTURL:
                        if (command instanceof StringType) {
                            if (StringUtils.isEmpty(command.toString())) {
                                logger.debug("Received a CONTENTURL channel command that is empty - ignoring");
                            } else {
                                localProtocolHandler.sendContentUrl(command.toString());
                            }
                        } else {
                            logger.debug("Received a CONTENTURL channel command with a non StringType: {}", command);
                        }
                        break;
                    case IrccConstants.CHANNEL_TEXT:
                        if (command instanceof StringType) {
                            if (StringUtils.isEmpty(command.toString())) {
                                logger.debug("Received a TEXT channel command that is empty - ignoring");
                            } else {
                                localProtocolHandler.sendText(command.toString());
                            }
                        } else {
                            logger.debug("Received a TEXT channel command with a non StringType: {}", command);
                        }
                        break;
                    default:
                        logger.debug("Unknown/Unsupported Primary Channel id: {}", channelId);
                        break;
                }

                break;

            case IrccConstants.GRP_VIEWING:
                logger.debug("Unknown/Unsupported Viewing Channel id: {}", channelId);
                break;

            case IrccConstants.GRP_CONTENT:
                logger.debug("Unknown/Unsupported Content Channel id: {}", channelId);
                break;
            default:
                break;
        }
    }

    @Override
    protected void refreshState(boolean initial) {
        final IrccProtocol<ThingCallback<String>> localProtocolHandler = protocolHandler.get();
        if (localProtocolHandler != null) {
            localProtocolHandler.refreshState();
        }
    }

    @Override
    protected void connect() {
        final IrccConfig config = getSonyConfig();

        if (StringUtils.isEmpty(config.getDeviceAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IRCC URL is missing from configuration");
            return;
        }

        logger.debug("Attempting connection to IRCC device...");
        try {
            SonyUtil.checkInterrupt();
            final IrccProtocol<ThingCallback<String>> localProtocolHandler = new IrccProtocol<>(config,
                    transformationService, new ThingCallback<String>() {
                        @Override
                        public void statusChanged(final ThingStatus state, final ThingStatusDetail detail,
                                final @Nullable String msg) {
                            updateStatus(state, detail, msg);
                        }

                        @Override
                        public void stateChanged(final String channelId, final State newState) {
                            updateState(channelId, newState);
                        }

                        @Override
                        public void setProperty(final String propertyName, final @Nullable String propertyValue) {
                            getThing().setProperty(propertyName, propertyValue);
                        }
                    });

            protocolHandler.set(localProtocolHandler);

            SonyUtil.checkInterrupt();
            final LoginUnsuccessfulResponse response = localProtocolHandler.login();
            if (response == null) {
                updateStatus(ThingStatus.ONLINE);
                SonyUtil.checkInterrupt();
                logger.debug("IRCC System now connected");
            } else {
                updateStatus(ThingStatus.OFFLINE, response.getThingStatusDetail(), response.getMessage());
            }
        } catch (IOException | ProcessingException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to IRCC device (may need to turn it on manually): " + e.getMessage());
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
