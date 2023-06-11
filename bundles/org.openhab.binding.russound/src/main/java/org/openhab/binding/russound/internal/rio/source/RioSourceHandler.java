/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.russound.internal.rio.source;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractRioHandlerCallback;
import org.openhab.binding.russound.internal.rio.AbstractThingHandler;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioNamedHandler;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.system.RioSystemHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a Russound Source. A source provides source music to the russound system (along with metadata
 * about the streaming music). This implementation must be attached to a {@link RioSystemHandler} bridge.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioSourceHandler extends AbstractThingHandler<RioSourceProtocol> implements RioNamedHandler {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RioSourceHandler.class);

    /**
     * The source identifier for this instance (1-12)
     */
    private final AtomicInteger source = new AtomicInteger(0);

    /**
     * The source name
     */
    private final AtomicReference<String> sourceName = new AtomicReference<>(null);

    /**
     * Constructs the handler from the {@link Thing}
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public RioSourceHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns the source identifier for this instance
     *
     * @return the source identifier
     */
    @Override
    public int getId() {
        return source.get();
    }

    /**
     * Returns the source name for this instance
     *
     * @return the source name
     */
    @Override
    public String getName() {
        final String name = sourceName.get();
        return name == null || name.isEmpty() ? "Source " + getId() : name;
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link RioSourceProtocol}. Basically we validate the type of command for the channel then call the
     * {@link RioSourceProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link RioSourceProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        // if (getThing().getStatus() != ThingStatus.ONLINE) {
        // // Ignore any command if not online
        // return;
        // }

        String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_SOURCEBANKS)) {
            if (command instanceof StringType) {
                // Remove any state for this channel to ensure it's recreated/sent again
                // (clears any bad or deleted favorites information from the channel)
                ((StatefulHandlerCallback) getProtocolHandler().getCallback())
                        .removeState(RioConstants.CHANNEL_SOURCEBANKS);

                // schedule the returned callback in the future (to allow the channel to process and to allow russound
                // to process (before re-retrieving information)
                scheduler.schedule(getProtocolHandler().setBanks(command.toString()), 250, TimeUnit.MILLISECONDS);
            } else {
                logger.debug("Received a BANKS channel command with a non StringType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link RioSourceProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (getProtocolHandler() == null) {
            return;
        }

        // Remove the cache'd value to force a refreshed value
        ((StatefulHandlerCallback) getProtocolHandler().getCallback()).removeState(id);

        if (id.equals(RioConstants.CHANNEL_SOURCENAME)) {
            getProtocolHandler().refreshSourceName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCETYPE)) {
            getProtocolHandler().refreshSourceType();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCECOMPOSERNAME)) {
            getProtocolHandler().refreshSourceComposerName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCECHANNEL)) {
            getProtocolHandler().refreshSourceChannel();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCECHANNELNAME)) {
            getProtocolHandler().refreshSourceChannelName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEGENRE)) {
            getProtocolHandler().refreshSourceGenre();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEARTISTNAME)) {
            getProtocolHandler().refreshSourceArtistName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEALBUMNAME)) {
            getProtocolHandler().refreshSourceAlbumName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCECOVERARTURL)) {
            getProtocolHandler().refreshSourceCoverArtUrl();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEPLAYLISTNAME)) {
            getProtocolHandler().refreshSourcePlaylistName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCESONGNAME)) {
            getProtocolHandler().refreshSourceSongName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEMODE)) {
            getProtocolHandler().refreshSourceMode();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCESHUFFLEMODE)) {
            getProtocolHandler().refreshSourceShuffleMode();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEREPEATMODE)) {
            getProtocolHandler().refreshSourceRepeatMode();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCERATING)) {
            getProtocolHandler().refreshSourceRating();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCEPROGRAMSERVICENAME)) {
            getProtocolHandler().refreshSourceProgramServiceName();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCERADIOTEXT)) {
            getProtocolHandler().refreshSourceRadioText();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCERADIOTEXT2)) {
            getProtocolHandler().refreshSourceRadioText2();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCERADIOTEXT3)) {
            getProtocolHandler().refreshSourceRadioText3();
        } else if (id.startsWith(RioConstants.CHANNEL_SOURCERADIOTEXT4)) {
            getProtocolHandler().refreshSourceRadioText4();
        } else if (id.equals(RioConstants.CHANNEL_SOURCEBANKS)) {
            getProtocolHandler().refreshBanks();
        }
        // Can't refresh any others...
    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioSystemHandler}. Once validated, a {@link RioSystemProtocol} is set via
     * {@link #setProtocolHandler(RioSystemProtocol)} and the bridge comes online.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing");
        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot be initialized without a bridge");
            return;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No handler specified (null) for the bridge!");
            return;
        }

        if (!(handler instanceof RioSystemHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Source must be attached to a System bridge: " + handler.getClass());
            return;
        }

        final RioSourceConfig config = getThing().getConfiguration().as(RioSourceConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        final int configSource = config.getSource();
        if (configSource < 1 || configSource > 12) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Source must be between 1 and 12: " + configSource);
            return;
        }
        source.set(configSource);

        // Get the socket session from the
        final SocketSession socketSession = getSocketSession();
        if (socketSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No socket session found");
            return;
        }

        try {
            setProtocolHandler(new RioSourceProtocol(configSource, socketSession,
                    new StatefulHandlerCallback(new AbstractRioHandlerCallback() {
                        @Override
                        public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                            updateStatus(status, detail, msg);
                        }

                        @Override
                        public void stateChanged(String channelId, State state) {
                            if (channelId.equals(RioConstants.CHANNEL_SOURCENAME)) {
                                sourceName.set(state.toString());
                            }

                            if (state != null) {
                                updateState(channelId, state);
                                fireStateUpdated(channelId, state);
                            }
                        }

                        @Override
                        public void setProperty(String propertyName, String propertyValue) {
                            getThing().setProperty(propertyName, propertyValue);
                        }
                    })));

            updateStatus(ThingStatus.ONLINE);
            getProtocolHandler().postOnline();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
        }
    }
}
