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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.core.MessageDispatcher;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.Command;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.CommandInfo;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.PlaybackState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemMetadataOuterClass.ContentItemMetadata;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemOuterClass.ContentItem;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NowPlayingClientOuterClass.NowPlayingClient;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.NowPlayingPlayerOuterClass.NowPlayingPlayer;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlaybackQueueOuterClass.PlaybackQueue;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.PlayerPathOuterClass.PlayerPath;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemoveClientMessageOuterClass.RemoveClientMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.RemovePlayerMessageOuterClass.RemovePlayerMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetDefaultSupportedCommandsMessageOuterClass.SetDefaultSupportedCommandsMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetNowPlayingClientMessageOuterClass.SetNowPlayingClientMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetNowPlayingPlayerMessageOuterClass.SetNowPlayingPlayerMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetStateMessageOuterClass.SetStateMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateClientMessageOuterClass.UpdateClientMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.UpdateContentItemMessageOuterClass.UpdateContentItemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors;

/**
 * Keeps track of media player states, including the notification rule that a state
 * change is announced when it concerns the active client, the active player, or when
 * neither a client nor a player was involved.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class PlayerStateManager {

    /** Identifier of the implicit default player. */
    public static final String DEFAULT_PLAYER_ID = "MediaRemote-DefaultPlayer";

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerStateManager.class);

    /** Callback announced whenever the observed state was updated. */
    public interface Listener {

        /** State was updated for the active media player. */
        void stateUpdated();
    }

    /** Represents what is currently playing on a device. */
    public static final class PlayerState {

        private PlaybackState.@Nullable Enum playbackState;
        private List<CommandInfo> supportedCommands = new ArrayList<>();
        private List<ContentItem> items = new ArrayList<>();
        private int location;

        private final String identifier;
        private @Nullable String displayName;
        private @Nullable Client parent;

        PlayerState(Client parent, NowPlayingPlayer player) {
            this.identifier = player.getIdentifier();
            this.parent = parent;
            update(player);
        }

        /** Identifier of the player (empty when unknown). */
        public String identifier() {
            return identifier;
        }

        /** Display name of the player, or {@code null} if never received. */
        public @Nullable String displayName() {
            return displayName;
        }

        /** Owning client, or {@code null} once the player has been removed. */
        public @Nullable Client parent() {
            return parent;
        }

        /** Returns if the player has a valid identifier. */
        public boolean isValid() {
            return identifier != null && !identifier.isEmpty();
        }

        /** Updates player metadata (keeps the previous display name when absent). */
        void update(NowPlayingPlayer player) {
            String newName = player.getDisplayName();
            if (!newName.isEmpty()) {
                displayName = newName;
            }
        }

        /**
         * Playback state of the device: {@code null} means idle; paused with nothing in
         * the queue resolves to idle; playing with a playback rate of 0.0 stays playing,
         * 1.0 is playing and anything else is seeking.
         *
         * @return the resolved playback state, or {@code null} for idle
         */
        public PlaybackState.@Nullable Enum playbackState() {
            // If playback state has not been received, assume player is not
            // playing anything (i.e. idle)
            if (playbackState == null) {
                return null;
            }

            // If player is considered paused, no content is playing
            if (playbackState == PlaybackState.Enum.Paused) {
                // ...unless something is in the queue...
                if (metadata() != null) {
                    return PlaybackState.Enum.Paused;
                }
                return null;
            }

            // All other states than playing (and paused) should pass through
            if (playbackState != PlaybackState.Enum.Playing) {
                return playbackState;
            }

            Object playbackRate = metadataField("playbackRate");
            if (playbackRate == null) {
                return playbackState;
            }

            double rate = ((Number) playbackRate).doubleValue();
            if (Math.abs(rate - 0.0) < 1e-9) {
                return PlaybackState.Enum.Playing;
            }
            if (Math.abs(rate - 1.0) < 1e-9) {
                return PlaybackState.Enum.Playing;
            }
            return PlaybackState.Enum.Seeking;
        }

        /**
         * Metadata of the currently playing item.
         *
         * @return metadata or {@code null} when the queue has no item at the current
         *         location
         */
        public @Nullable ContentItemMetadata metadata() {
            if (items.size() >= location + 1) {
                return items.get(location).getMetadata();
            }
            return null;
        }

        /** Queue location of the currently playing item. */
        public int location() {
            return location;
        }

        /**
         * Identifier of the current item in the queue.
         *
         * @return identifier or {@code null} when the queue has no item at the current
         *         location
         */
        public @Nullable String itemIdentifier() {
            if (items.size() >= location + 1) {
                String id = items.get(location).getIdentifier();
                return id.isEmpty() ? null : id;
            }
            return null;
        }

        /**
         * Returns a specific metadata field by protobuf field name, or {@code null} if
         * missing (the field must be explicitly present).
         *
         * @param field protobuf field name on {@code ContentItemMetadata}
         * @return the field value or {@code null}
         */
        public @Nullable Object metadataField(String field) {
            @Nullable
            ContentItemMetadata metadata = metadata();
            if (metadata == null) {
                return null;
            }
            Descriptors.FieldDescriptor descriptor = ContentItemMetadata.getDescriptor().findFieldByName(field);
            if (descriptor == null) {
                throw new IllegalArgumentException("unknown metadata field: " + field);
            }
            if (metadata.hasField(descriptor)) {
                return metadata.getField(descriptor);
            }
            return null;
        }

        /**
         * Returns supported command info for a command, looking first at the player's
         * own commands and then at the client's default commands.
         *
         * @param command command to look up
         * @return command info or {@code null} when the command is not supported
         */
        public @Nullable CommandInfo commandInfo(Command command) {
            for (CommandInfo info : supportedCommands) {
                if (info.getCommand() == command) {
                    return info;
                }
            }
            if (parent != null) {
                for (CommandInfo info : parent.supportedCommands()) {
                    if (info.getCommand() == command) {
                        return info;
                    }
                }
            }
            return null;
        }

        /** Updates current state with new data from a {@code SetStateMessage}. */
        void handleSetState(SetStateMessage setState) {
            if (setState.hasPlaybackState()) {
                playbackState = setState.getPlaybackState();
            }
            if (setState.hasSupportedCommands()) {
                supportedCommands = new ArrayList<>(setState.getSupportedCommands().getSupportedCommandsList());
            }
            if (setState.hasPlaybackQueue()) {
                PlaybackQueue queue = setState.getPlaybackQueue();
                items = new ArrayList<>(queue.getContentItemsList());
                location = queue.getLocation();
            }
        }

        /** Updates current state with new data from a {@code ContentItemUpdate}. */
        void handleContentItemUpdate(UpdateContentItemMessage itemUpdate) {
            for (ContentItem updatedItem : itemUpdate.getContentItemsList()) {
                for (int i = 0; i < items.size(); i++) {
                    ContentItem existing = items.get(i);
                    if (updatedItem.getIdentifier().equals(existing.getIdentifier())) {
                        // Other parts of the ContentItem should be merged as well, but
                        // those are not used right now. Note that merge appends repeated
                        // fields rather than replacing them.
                        ContentItemMetadata merged = existing.getMetadata().toBuilder()
                                .mergeFrom(updatedItem.getMetadata()).build();
                        items.set(i, existing.toBuilder().setMetadata(merged).build());
                    }
                }
            }
        }

        /** Equality is based on identifier. */
        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof PlayerState that && Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(identifier);
        }
    }

    /** Represents an MRP media player client, e.g. an app. */
    public static final class Client {

        private final String bundleIdentifier;
        private @Nullable String displayName;
        private @Nullable PlayerState activePlayer;
        private final Map<String, PlayerState> players = new LinkedHashMap<>();
        private List<CommandInfo> supportedCommands = new ArrayList<>();

        Client(NowPlayingClient client) {
            this.bundleIdentifier = client.getBundleIdentifier();
            update(client);
        }

        /** Bundle identifier of the client. */
        public String bundleIdentifier() {
            return bundleIdentifier;
        }

        /** Display name of the client, or {@code null} if never received. */
        public @Nullable String displayName() {
            return displayName;
        }

        /** Default supported commands of the client. */
        public List<CommandInfo> supportedCommands() {
            return supportedCommands;
        }

        /** Players known for this client, keyed by identifier. */
        public Map<String, PlayerState> players() {
            return players;
        }

        /**
         * Returns the currently active player: the explicitly set one, the default
         * player if known, or an empty placeholder.
         */
        public PlayerState activePlayer() {
            PlayerState current = activePlayer;
            if (current == null) {
                PlayerState defaultPlayer = players.get(DEFAULT_PLAYER_ID);
                if (defaultPlayer != null) {
                    return defaultPlayer;
                }
                return new PlayerState(this, NowPlayingPlayer.getDefaultInstance());
            }
            return current;
        }

        void setActivePlayer(@Nullable PlayerState player) {
            this.activePlayer = player;
        }

        /** Gets (or creates) the state for a player. */
        PlayerState getPlayer(NowPlayingPlayer player) {
            return Objects.requireNonNull(
                    players.computeIfAbsent(player.getIdentifier(), id -> new PlayerState(this, player)));
        }

        void handleSetDefaultSupportedCommands(SetDefaultSupportedCommandsMessage message) {
            supportedCommands = new ArrayList<>(message.getSupportedCommands().getSupportedCommandsList());
        }

        void handleSetNowPlayingPlayer(NowPlayingPlayer player) {
            activePlayer = getPlayer(player);
            if (activePlayer.isValid()) {
                LOGGER.debug("Active player is now {} ({})", activePlayer.identifier(), activePlayer.displayName());
            } else {
                LOGGER.debug("Active player no longer set");
            }
        }

        /** Updates client metadata (keeps the previous display name when absent). */
        void update(NowPlayingClient client) {
            String newName = client.getDisplayName();
            if (!newName.isEmpty()) {
                displayName = newName;
            }
        }
    }

    private final Map<String, Client> clients = new LinkedHashMap<>();
    private @Nullable Client activeClient;
    private volatile @Nullable Listener listener;

    /**
     * Creates a manager listening for player state messages on the given dispatcher.
     *
     * @param protocol dispatcher of incoming protocol messages (normally the
     *            {@link MrpProtocol})
     */
    public PlayerStateManager(MessageDispatcher<ProtocolMessage.Type, ProtocolMessage> protocol) {
        protocol.listenTo(ProtocolMessage.Type.SET_STATE_MESSAGE, this::handleSetState);
        protocol.listenTo(ProtocolMessage.Type.UPDATE_CONTENT_ITEM_MESSAGE, this::handleContentItemUpdate);
        protocol.listenTo(ProtocolMessage.Type.SET_NOW_PLAYING_CLIENT_MESSAGE, this::handleSetNowPlayingClient);
        protocol.listenTo(ProtocolMessage.Type.SET_NOW_PLAYING_PLAYER_MESSAGE, this::handleSetNowPlayingPlayer);
        protocol.listenTo(ProtocolMessage.Type.UPDATE_CLIENT_MESSAGE, this::handleUpdateClient);
        protocol.listenTo(ProtocolMessage.Type.REMOVE_CLIENT_MESSAGE, this::handleRemoveClient);
        protocol.listenTo(ProtocolMessage.Type.REMOVE_PLAYER_MESSAGE, this::handleRemovePlayer);
        protocol.listenTo(ProtocolMessage.Type.SET_DEFAULT_SUPPORTED_COMMANDS_MESSAGE,
                this::handleSetDefaultSupportedCommands);
    }

    /**
     * Returns the client for a now-playing client message, creating it when unknown.
     *
     * @param client protobuf now-playing client
     * @return the tracked client
     */
    public Client getClient(NowPlayingClient client) {
        return Objects.requireNonNull(clients.computeIfAbsent(client.getBundleIdentifier(), id -> new Client(client)));
    }

    /**
     * Returns the player state for a player path, creating client and player when
     * unknown.
     *
     * @param playerPath the player path
     * @return the tracked player state
     */
    public PlayerState getPlayer(PlayerPath playerPath) {
        return getClient(playerPath.getClient()).getPlayer(playerPath.getPlayer());
    }

    /** Returns the current listener, or {@code null}. */
    public @Nullable Listener listener() {
        return listener;
    }

    /** Changes the current listener ({@code null} to remove). */
    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    /** Returns the currently active client, or {@code null}. */
    public @Nullable Client client() {
        return activeClient;
    }

    /**
     * Returns the player state for the active media player (an empty placeholder when no
     * client is active).
     */
    public PlayerState playing() {
        if (activeClient != null) {
            return activeClient.activePlayer();
        }
        return new PlayerState(new Client(NowPlayingClient.getDefaultInstance()),
                NowPlayingPlayer.getDefaultInstance());
    }

    private void handleSetState(ProtocolMessage message) {
        SetStateMessage setState = (SetStateMessage) MrpExtensions.extractInner(message);
        PlayerState player = getPlayer(setState.getPlayerPath());
        player.handleSetState(setState);
        stateUpdated(null, player);
    }

    private void handleContentItemUpdate(ProtocolMessage message) {
        UpdateContentItemMessage itemUpdate = (UpdateContentItemMessage) MrpExtensions.extractInner(message);
        PlayerState player = getPlayer(itemUpdate.getPlayerPath());
        player.handleContentItemUpdate(itemUpdate);
        stateUpdated(null, player);
    }

    private void handleSetNowPlayingClient(ProtocolMessage message) {
        SetNowPlayingClientMessage inner = (SetNowPlayingClientMessage) MrpExtensions.extractInner(message);
        activeClient = getClient(inner.getClient());
        LOGGER.debug("Active client is now {}", activeClient.bundleIdentifier());
        stateUpdated(null, null);
    }

    private void handleSetNowPlayingPlayer(ProtocolMessage message) {
        SetNowPlayingPlayerMessage inner = (SetNowPlayingPlayerMessage) MrpExtensions.extractInner(message);
        Client client = getClient(inner.getPlayerPath().getClient());
        client.handleSetNowPlayingPlayer(inner.getPlayerPath().getPlayer());
        stateUpdated(client, null);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void handleRemoveClient(ProtocolMessage message) {
        RemoveClientMessage inner = (RemoveClientMessage) MrpExtensions.extractInner(message);
        String bundleIdentifier = inner.getClient().getBundleIdentifier();
        Client client = clients.remove(bundleIdentifier);
        if (client != null && client == activeClient) {
            activeClient = null;
            stateUpdated(null, null);
        }
    }

    private void handleRemovePlayer(ProtocolMessage message) {
        RemovePlayerMessage inner = (RemovePlayerMessage) MrpExtensions.extractInner(message);
        PlayerPath playerToRemove = inner.getPlayerPath();
        PlayerState player = getPlayer(playerToRemove);
        if (player.isValid()) {
            Client client = getClient(playerToRemove.getClient());
            client.players().remove(player.identifier());
            player.parent = null;
            if (player.equals(client.activePlayer())) {
                client.setActivePlayer(null);
                stateUpdated(client, null);
            }
        }
    }

    private void handleSetDefaultSupportedCommands(ProtocolMessage message) {
        SetDefaultSupportedCommandsMessage inner = (SetDefaultSupportedCommandsMessage) MrpExtensions
                .extractInner(message);
        Client client = getClient(inner.getPlayerPath().getClient());
        client.handleSetDefaultSupportedCommands(inner);
        stateUpdated(null, null);
    }

    private void handleUpdateClient(ProtocolMessage message) {
        UpdateClientMessage inner = (UpdateClientMessage) MrpExtensions.extractInner(message);
        Client client = getClient(inner.getClient());
        client.update(inner.getClient());
        stateUpdated(client, null);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void stateUpdated(@Nullable Client client, @Nullable PlayerState player) {
        // A null client compares equal to a null active client
        boolean isActiveClient = client == activeClient;
        boolean isActivePlayer = player != null && player.equals(playing());
        boolean isAlways = client == null && player == null;

        if (isActiveClient || isActivePlayer || isAlways) {
            @Nullable
            Listener current = listener;
            if (current != null) {
                current.stateUpdated();
            }
        }
    }
}
