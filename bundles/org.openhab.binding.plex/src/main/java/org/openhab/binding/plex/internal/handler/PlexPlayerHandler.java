/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.plex.internal.config.PlexPlayerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer.MediaType;
import org.openhab.binding.plex.internal.dto.PlexPlayerState;
import org.openhab.binding.plex.internal.dto.PlexSession;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Plex Player.
 * 
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexPlayerHandler extends BaseThingHandler {
    private @NonNullByDefault({}) String playerID;

    private PlexSession currentSessionData;
    private boolean foundInSession;

    private final Logger logger = LoggerFactory.getLogger(PlexPlayerHandler.class);

    public PlexPlayerHandler(Thing thing) {
        super(thing);
        currentSessionData = new PlexSession();
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
        logger.debug("Initializing PLEX player : {}", playerID);
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Currently only the 'player' channel accepts commands, all others are read-only
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("REFRESH not implemented");
            return;
        }

        Bridge bridge = getBridge();
        PlexServerHandler bridgeHandler = bridge == null ? null : (PlexServerHandler) bridge.getHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_PLAYER_CONTROL:
                    bridgeHandler.getPlexAPIConnector().controlPlayer(command, playerID);
                    break;
                default:
                    logger.debug("Channel {} not implemented/supported to control player {}", channelUID.getId(),
                            this.thing.getUID());
            }
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
        currentSessionData.setRatingKey(sessionData.getRatingKey());
        currentSessionData.setParentRatingKey(sessionData.getParentRatingKey());
        currentSessionData.setGrandparentRatingKey(sessionData.getGrandparentRatingKey());

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
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_STATE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getState() : "Stopped")));
    }

    /**
     * Updates the channel states to match reality.
     */
    public synchronized void updateChannels() {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_STATE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getState() : "Stopped")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_POWER),
                new StringType(String.valueOf(foundInSession ? "ON" : "OFF")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_TITLE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getTitle() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_TYPE),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getType() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_ART),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getArt() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_THUMB),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getThumb() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_PROGRESS),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getProgress() : "0")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_ENDTIME),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getEndTime() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_ENDTIME),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getEndTime() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_USER),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getUserTitle() : "")));
        final String parentRatingKey = currentSessionData.getParentRatingKey();
        final String grandparentRatingKey = currentSessionData.getGrandparentRatingKey();
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_RATING_KEY),
                new StringType(String.valueOf(foundInSession ? currentSessionData.getRatingKey() : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_PARENT_RATING_KEY),
                new StringType(String.valueOf(foundInSession && parentRatingKey != null ? parentRatingKey : "")));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_GRANDPARENT_RATING_KEY), new StringType(
                String.valueOf(foundInSession && grandparentRatingKey != null ? grandparentRatingKey : "")));

        // Make sure player control is in sync with the play state
        if (currentSessionData.getState() == PlexPlayerState.PLAYING) {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_CONTROL), PlayPauseType.PLAY);
        }
        if (currentSessionData.getState() == PlexPlayerState.PAUSED) {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLAYER_CONTROL), PlayPauseType.PAUSE);
        }
    }
}
