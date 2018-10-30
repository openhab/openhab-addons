/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.handler;

import static org.openhab.binding.snapcast.internal.SnapcastBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.snapcast.internal.data.Stream;
import org.openhab.binding.snapcast.internal.protocol.ServerController;
import org.openhab.binding.snapcast.internal.protocol.ServerListener;
import org.openhab.binding.snapcast.internal.protocol.SnapcastController;
import org.openhab.binding.snapcast.internal.protocol.StreamController;
import org.openhab.binding.snapcast.internal.protocol.StreamListener;

/**
 * {@link SnapcastClientHandler} is the handler for a snapcast server.
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class SnapcastServerHandler extends BaseBridgeHandler {

    private final SnapcastController snapcastController = new SnapcastController();
    private final ServerProtocolHandler serverProtocolHandler = new ServerProtocolHandler();
    private final StreamProtocolHandler streamProtocolHandler = new StreamProtocolHandler();

    private @NonNullByDefault({}) ServerController serverController;
    private @NonNullByDefault({}) StreamController streamController;

    public SnapcastServerHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_SERVER_STREAMS:
            case CHANNEL_SERVER_STREAMS_PLAYING:
            case CHANNEL_SERVER_STREAMS_IDLE:
                if (command instanceof RefreshType) {
                    streamProtocolHandler.updateStatus(null);
                }
                break;
        }
    }

    @Override
    public void initialize() {
        String host = (String) getConfig().get(CONFIG_SERVER_HOST);
        int port = ((BigDecimal) getConfig().get(CONFIG_SERVER_PORT)).intValue();

        updateStatus(ThingStatus.UNKNOWN);

        serverController = snapcastController.serverController();
        serverController.addListener(null, serverProtocolHandler);

        streamController = snapcastController.streamController();
        streamController.addListener(null, streamProtocolHandler);

        snapcastController.connect(host, port);
    }

    @Override
    public void dispose() {
        if (serverController != null) {
            serverController.removeListener(null, serverProtocolHandler);
        }
        if (streamController != null) {
            streamController.removeListener(null, streamProtocolHandler);
        }
        snapcastController.dispose();
        super.dispose();
    }

    /**
     * Provides the main controller who handles the connection to the snapcast server
     *
     * @return the main snapcast controller
     */
    public SnapcastController getSnapcastController() {
        return snapcastController;
    }

    /**
     * The {@link ServerProtocolHandler} handle the updates for the stream informations.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    private class ServerProtocolHandler implements ServerListener {

        @Override
        public void updateConnection(boolean established) {
            if (established) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }

    }

    /**
     * The {@link StreamProtocolHandler} handle the updates for the stream informations.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    private class StreamProtocolHandler implements StreamListener {

        @Override
        public void updateStatus(@Nullable String id) {
            if (streamController != null) {
                List<String> streams = new ArrayList<>();
                List<String> streamsPlaying = new ArrayList<>();
                List<String> streamsIdle = new ArrayList<>();

                for (Stream stream : streamController.listThingState()) {
                    final String streamId = stream.getId();
                    final String status = stream.getStatus();
                    if (streamId != null) {
                        streams.add(streamId);
                        if (status != null) {
                            switch (status) {
                                case STREAM_STATE_PLAYING:
                                    streamsPlaying.add(streamId);
                                    break;
                                case STREAM_STATE_IDLE:
                                    streamsIdle.add(streamId);
                                    break;
                            }
                        }
                    }
                }

                updateState(CHANNEL_SERVER_STREAMS, new StringType(String.join(STREAM_STATE_DELIMITER, streams)));

                updateState(CHANNEL_SERVER_STREAMS_PLAYING,
                        new StringType(String.join(STREAM_STATE_DELIMITER, streamsPlaying)));

                updateState(CHANNEL_SERVER_STREAMS_IDLE,
                        new StringType(String.join(STREAM_STATE_DELIMITER, streamsIdle)));
            }
        }

    }

}
