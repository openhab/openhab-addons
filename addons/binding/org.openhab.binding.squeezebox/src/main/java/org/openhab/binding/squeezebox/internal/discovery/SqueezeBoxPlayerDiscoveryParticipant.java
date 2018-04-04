/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.discovery;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.SQUEEZEBOXPLAYER_THING_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayer;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerEventListener;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerHandler;
import org.openhab.binding.squeezebox.handler.SqueezeBoxServerHandler;
import org.openhab.binding.squeezebox.internal.model.Favorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a {@link SqueezeBoxServerHandler} finds a new SqueezeBox Player we will
 * add it to the system.
 *
 * @author Dan Cunningham
 * @author Mark Hilbush - added method to cancel request player job, and to set thing properties
 * @author Mark Hilbush - Added duration channel
 * @author Mark Hilbush - Added event to update favorites list
 *
 */
public class SqueezeBoxPlayerDiscoveryParticipant extends AbstractDiscoveryService
        implements SqueezeBoxPlayerEventListener {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerDiscoveryParticipant.class);

    private static final int TIMEOUT = 60;
    private static final int TTL = 60;

    private SqueezeBoxServerHandler squeezeBoxServerHandler;
    private ScheduledFuture<?> requestPlayerJob;

    /**
     * Discovers SqueezeBox Players attached to a SqueezeBox Server
     *
     * @param squeezeBoxServerHandler
     */
    public SqueezeBoxPlayerDiscoveryParticipant(SqueezeBoxServerHandler squeezeBoxServerHandler) {
        super(SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        this.squeezeBoxServerHandler = squeezeBoxServerHandler;
        setupRequestPlayerJob();
    }

    @Override
    protected void startScan() {
        logger.debug("startScan invoked in SqueezeBoxPlayerDiscoveryParticipant");
        this.squeezeBoxServerHandler.requestPlayers();
    }

    /*
     * Allows request player job to be canceled when server handler is removed
     */
    public void cancelRequestPlayerJob() {
        logger.debug("canceling RequestPlayerJob");
        if (requestPlayerJob != null) {
            requestPlayerJob.cancel(true);
            requestPlayerJob = null;
        }
    }

    @Override
    public void playerAdded(SqueezeBoxPlayer player) {
        ThingUID bridgeUID = squeezeBoxServerHandler.getThing().getUID();

        ThingUID thingUID = new ThingUID(SQUEEZEBOXPLAYER_THING_TYPE, bridgeUID,
                player.getMacAddress().replace(":", ""));

        if (!playerThingExists(thingUID)) {
            logger.debug("player added {} : {} ", player.getMacAddress(), player.getName());

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("mac", player.getMacAddress());

            // Added other properties
            properties.put("modelId", player.getModel());
            properties.put("name", player.getName());
            properties.put("uid", player.getUuid());
            properties.put("ip", player.getIpAddr());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(player.getName()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private boolean playerThingExists(ThingUID newThingUID) {
        return squeezeBoxServerHandler.getThingByUID(newThingUID) != null ? true : false;
    }

    /**
     * Tells the bridge to request a list of players
     */
    private void setupRequestPlayerJob() {
        logger.debug("Request player job scheduled to run every {} seconds", TTL);
        requestPlayerJob = scheduler.scheduleWithFixedDelay(() -> {
            squeezeBoxServerHandler.requestPlayers();
        }, 10, TTL, TimeUnit.SECONDS);
    }

    // we can ignore the other events
    @Override
    public void powerChangeEvent(String mac, boolean power) {
    }

    @Override
    public void modeChangeEvent(String mac, String mode) {
    }

    @Override
    public void absoluteVolumeChangeEvent(String mac, int volume) {
    }

    @Override
    public void relativeVolumeChangeEvent(String mac, int volumeChange) {
    }

    @Override
    public void muteChangeEvent(String mac, boolean mute) {
    }

    @Override
    public void currentPlaylistIndexEvent(String mac, int index) {
    }

    @Override
    public void currentPlayingTimeEvent(String mac, int time) {
    }

    @Override
    public void durationEvent(String mac, int duration) {
    }

    @Override
    public void numberPlaylistTracksEvent(String mac, int track) {
    }

    @Override
    public void currentPlaylistShuffleEvent(String mac, int shuffle) {
    }

    @Override
    public void currentPlaylistRepeatEvent(String mac, int repeat) {
    }

    @Override
    public void titleChangeEvent(String mac, String title) {
    }

    @Override
    public void albumChangeEvent(String mac, String album) {
    }

    @Override
    public void artistChangeEvent(String mac, String artist) {
    }

    @Override
    public void coverArtChangeEvent(String mac, String coverArtUrl) {
    }

    @Override
    public void yearChangeEvent(String mac, String year) {
    }

    @Override
    public void genreChangeEvent(String mac, String genre) {
    }

    @Override
    public void remoteTitleChangeEvent(String mac, String title) {
    }

    @Override
    public void irCodeChangeEvent(String mac, String ircode) {
    }

    @Override
    public void updateFavoritesListEvent(List<Favorite> favorites) {
    }
}
