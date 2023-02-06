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
package org.openhab.binding.plex.internal.handler;

import static org.openhab.binding.plex.internal.PlexBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plex.internal.PlexBindingConstants;
import org.openhab.binding.plex.internal.config.PlexPlayerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer.MediaType;
import org.openhab.binding.plex.internal.dto.PlexPlayerState;
import org.openhab.binding.plex.internal.dto.PlexSession;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlexBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexPlayerHandler extends BaseThingHandler {
    private @NonNullByDefault({}) String playerID;

    private @Nullable PlexServerHandler bridgeHandler;
    private PlexSession currentSessionData;
    private boolean foundInSession;

    private final Logger logger = LoggerFactory.getLogger(PlexPlayerHandler.class);

    private PlexApiConnector plexAPIConnector;

    public PlexPlayerHandler(Thing thing) {
        super(thing);
        currentSessionData = new PlexSession();
        plexAPIConnector = new PlexApiConnector(scheduler);
    }

    /**
     * Initialize the player thing, check the bridge status and hang out waiting
     * for the session data to get polled.
     */
    @Override
    public void initialize() {
        PlexPlayerConfiguration config = getConfigAs(PlexPlayerConfiguration.class);
        foundInSession = false;
        playerID = config.playerID;
        logger.warn("Initializing PLEX player : {}", playerID);
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Currently readonly, but this will handle events back from the channels at some point
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        assert bridgeHandler != null;
        PlexApiConnector plexApiConnector = bridgeHandler.getPlexAPIConnector();
        switch (channelUID.getId()) {
            case CHANNEL_PLAYER_CONTROL:
                plexApiConnector.controlPlayer(command, playerID);
                break;
            default:
                logger.debug("Channel {} not implemented/supported to control player {}", channelUID.getId(),
                        this.thing.getUID());
        }
    }

    /**
     * This is really just to set these all back to false so when we refresh the data it's
     * updated for Power On/Off. This is only called from the Server Bridge.
     *
     * @param foundInSession Will always be false, so this can probably be changed.
     */
    public void setFoundInSession(boolean foundInSession) {
        this.foundInSession = foundInSession;
    }

    /**
     * Returns the session key from the current player
     *
     * @return
     */
    public String getSessionKey() {
        return currentSessionData.getSessionKey();
    }

    /**
     * Called when this thing gets its configuration changed.
     */
    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    /**
     * Refreshes all the data from the session XML call. This is called from the bridge
     *
     * @param sessionData The Video section of the XML(which is what pertains to the player)
     */
    public void refreshSessionData(MediaType sessionData) {
        currentSessionData.setState(PlexPlayerState.of(sessionData.getPlayer().getState()));
        currentSessionData.setDuration(sessionData.getMedia().getDuration());
        currentSessionData.setMachineIdentifier(sessionData.getPlayer().getMachineIdentifier());
        currentSessionData.setViewOffset(sessionData.getViewOffset());
        currentSessionData.setTitle(sessionData.getTitle());
        currentSessionData.setType(sessionData.getType());
        currentSessionData.setThumb(sessionData.getThumb());
        currentSessionData.setArt(sessionData.getArt());
        currentSessionData.setLocal(sessionData.getPlayer().getLocal());
        currentSessionData.setSessionKey(sessionData.getSessionKey());
        currentSessionData.setUserId(sessionData.getUser().getId());
        currentSessionData.setUserTitle(sessionData.getUser().getTitle());

        foundInSession = true;
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update just the state, this status comes from the websocket.
     *
     * @param state - The state to update it to.
     */
    public synchronized void updateStateChannel(String state) {
        currentSessionData.setState(PlexPlayerState.of(state));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_STATE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getState() : "Stopped")));
    }

    /**
     * Updates the channel states to match reality.
     */
    public synchronized void updateChannels() {
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_STATE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getState() : "Stopped")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_POWER),
                new StringType(String.valueOf(foundInSession ? "ON" : "OFF")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_TITLE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getTitle() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_TYPE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getType() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_ART),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getArt() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_THUMB),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getThumb() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_PROGRESS),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getProgress() : "0")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_ENDTIME),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getEndTime() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_ENDTIME),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getEndTime() : "")));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_USER),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getUserTitle() : "")));

        // Make sure player control is in sync with the play state
        if (currentSessionData.getState() == PlexPlayerState.PLAYING) {
            updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_CONTROL),
                    PlayPauseType.PLAY);
        }
        if (currentSessionData.getState() == PlexPlayerState.PAUSED) {
            updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_PLAYER_CONTROL),
                    PlayPauseType.PAUSE);
        }
    }
}
