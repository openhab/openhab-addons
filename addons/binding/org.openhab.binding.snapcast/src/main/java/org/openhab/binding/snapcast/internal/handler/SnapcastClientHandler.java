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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.snapcast.internal.data.Client;
import org.openhab.binding.snapcast.internal.data.Client.ClientConfig;
import org.openhab.binding.snapcast.internal.data.Client.Volume;
import org.openhab.binding.snapcast.internal.data.Group;
import org.openhab.binding.snapcast.internal.data.Stream;
import org.openhab.binding.snapcast.internal.protocol.ClientController;
import org.openhab.binding.snapcast.internal.protocol.ClientListener;
import org.openhab.binding.snapcast.internal.protocol.GroupController;
import org.openhab.binding.snapcast.internal.protocol.GroupListener;
import org.openhab.binding.snapcast.internal.protocol.StreamController;
import org.openhab.binding.snapcast.internal.protocol.StreamListener;

/**
 * {@link SnapcastClientHandler} is the handler for a snapcast client.
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class SnapcastClientHandler extends BaseThingHandler {

    private final ClientProtocolHandler clientProtocolHandler = new ClientProtocolHandler();
    private final GroupProtocolHandler groupProtocolHandler = new GroupProtocolHandler();
    private final StreamProtocolHandler streamProtocolHandler = new StreamProtocolHandler();

    private @NonNullByDefault({}) ClientController clientController;
    private @NonNullByDefault({}) GroupController groupController;
    private @NonNullByDefault({}) StreamController streamController;

    private @NonNullByDefault({}) String clientId;
    private @Nullable String groupId;
    private @Nullable String streamId;

    public SnapcastClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_CLIENT_NAME:
                if (command instanceof RefreshType) {
                    clientProtocolHandler.updateName(clientId);
                } else if (clientController != null && command instanceof StringType) {
                    clientController.setName(clientId, command.toFullString());
                }
                break;
            case CHANNEL_CLIENT_VOLUME:
                if (command instanceof RefreshType) {
                    clientProtocolHandler.updateVolumn(clientId);
                } else if (clientController != null && command instanceof DecimalType) {
                    clientController.setVolume(clientId, ((Number) command).intValue(), false);
                }
                break;
            case CHANNEL_CLIENT_MUTE:
                if (command instanceof RefreshType) {
                    clientProtocolHandler.updateMute(clientId);
                } else if (clientController != null && command instanceof OnOffType) {
                    clientController.setVolume(clientId, null, OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_CLIENT_LATENCY:
                if (command instanceof RefreshType) {
                    clientProtocolHandler.updateLatency(clientId);
                } else if (clientController != null && command instanceof DecimalType) {
                    clientController.setLatency(clientId, ((Number) command).intValue());
                }
                break;
            case CHANNEL_STREAM_ID:
                if (command instanceof RefreshType) {
                    clientProtocolHandler.updateGroup(clientId);
                } else if (clientController != null && command instanceof StringType) {
                    clientController.setStream(clientId, command.toFullString());
                }
                break;
            case CHANNEL_STREAM_STATUS:
                if (command instanceof RefreshType) {
                    streamProtocolHandler.updateStatus(streamId);
                }
        }
    }

    @Override
    public void initialize() {
        clientId = (String) getConfig().get(CONFIG_CLIENT_ID);
        groupId = null;
        streamId = null;

        Bridge bridge;
        SnapcastServerHandler bridgeHandler;

        if ((bridge = getBridge()) == null || (bridgeHandler = (SnapcastServerHandler) bridge.getHandler()) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not available");
            return;
        }

        clientController = bridgeHandler.getSnapcastController().clientController();
        clientController.addListener(clientId, clientProtocolHandler);

        groupController = bridgeHandler.getSnapcastController().groupController();

        streamController = bridgeHandler.getSnapcastController().streamController();

        clientProtocolHandler.updateConnection(clientId);
    }

    @Override
    public void dispose() {
        if (clientController != null) {
            clientController.removeListener(clientId, clientProtocolHandler);
        }
        if (groupController != null) {
            groupController.removeListener(groupId, groupProtocolHandler);
        }
        if (streamController != null) {
            streamController.removeListener(streamId, streamProtocolHandler);
        }
        super.dispose();
    }

    /**
     * The {@link ClientProtocolHandler} handle the updates for the client informations.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    private class ClientProtocolHandler implements ClientListener {

        @Override
        public void updateConnection(String clientId) {
            Client client = getThingState(clientId);
            if (client != null) {
                Boolean connected = client.getConnected();
                if (connected != null && connected.booleanValue()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }

        @Override
        public void updateName(String clientId) {
            ClientConfig clientConfig = getClientConfig(clientId);
            if (clientConfig != null) {
                String name = clientConfig.getName();
                if (name != null) {
                    updateState(CHANNEL_CLIENT_NAME, new StringType(name));
                }
            }
        }

        @Override
        public void updateVolumn(String clientId) {
            ClientConfig clientConfig = getClientConfig(clientId);
            if (clientConfig != null) {
                Volume volume = clientConfig.getVolume();
                if (volume != null) {
                    Integer percent = volume.getPercent();
                    if (percent != null) {
                        updateState(CHANNEL_CLIENT_VOLUME, new PercentType(percent));
                    }
                }
            }
        }

        @Override
        public void updateMute(String clientId) {
            ClientConfig clientConfig = getClientConfig(clientId);
            if (clientConfig != null) {
                Volume volume = clientConfig.getVolume();
                if (volume != null) {
                    Boolean muted = volume.getMuted();
                    if (muted != null) {
                        updateState(CHANNEL_CLIENT_MUTE, (muted.booleanValue() ? OnOffType.ON : OnOffType.OFF));
                    }
                }
            }
        }

        @Override
        public void updateLatency(String clientId) {
            ClientConfig clientConfig = getClientConfig(clientId);
            if (clientConfig != null) {
                Integer latency = clientConfig.getLatency();
                if (latency != null) {
                    updateState(CHANNEL_CLIENT_LATENCY, new DecimalType(latency));
                }
            }
        }

        @Override
        public void updateGroup(String clientId) {
            if (groupController != null) {
                Group group = groupController.getThingStateByClientId(clientId);
                if (group != null) {
                    String id = group.getId();
                    if (!id.equals(groupId)) {
                        if (groupController != null) {
                            groupController.removeListener(groupId, groupProtocolHandler);
                        }
                        groupId = id;
                        if (groupController != null) {
                            groupController.addListener(groupId, groupProtocolHandler);
                        }
                    }
                    groupProtocolHandler.updateStream(groupId);
                }
            }
        }

        private @Nullable ClientConfig getClientConfig(String clientId) {
            Client client = getThingState(clientId);
            if (client != null) {
                return client.getConfig();
            } else {
                return null;
            }
        }

        private @Nullable Client getThingState(String clientId) {
            if (clientController != null) {
                return clientController.getThingState(clientId);
            } else {
                return null;
            }
        }

    }

    /**
     * The {@link GroupProtocolHandler} handle the updates for the group informations.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    private class GroupProtocolHandler implements GroupListener {

        @Override
        public void updateStream(@Nullable String groupId) {
            Group group = getThingState(groupId);
            if (group != null) {
                String id = group.getStream();
                if (!id.equals(streamId)) {
                    if (streamController != null) {
                        streamController.removeListener(streamId, streamProtocolHandler);
                    }
                    streamId = id;
                    if (streamController != null) {
                        streamController.addListener(streamId, streamProtocolHandler);
                    }
                    streamProtocolHandler.updateStatus(streamId);
                }
            }
            updateState(CHANNEL_STREAM_ID, new StringType(streamId));
        }

        private @Nullable Group getThingState(@Nullable String groupId) {
            if (groupController != null) {
                return groupController.getThingState(groupId);
            } else {
                return null;
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
        public void updateStatus(@Nullable String streamId) {
            Stream stream = getThingState(streamId);
            if (stream != null) {
                String status = stream.getStatus();
                if (status != null) {
                    updateState(CHANNEL_STREAM_STATUS, new StringType(status));
                }
            }
        }

        private @Nullable Stream getThingState(@Nullable String streamId) {
            if (streamController != null) {
                return streamController.getThingState(streamId);
            } else {
                return null;
            }
        }

    }
}
