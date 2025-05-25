/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal.discovery;

import static org.openhab.binding.squeezebox.internal.SqueezeBoxBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayer;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerEventListener;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerHandler;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxServerHandler;
import org.openhab.binding.squeezebox.internal.model.Favorite;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a {@link SqueezeBoxServerHandler} finds a new SqueezeBox Player we will
 * add it to the system.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - added method to cancel request player job, and to set thing properties
 * @author Mark Hilbush - Added duration channel
 * @author Mark Hilbush - Added event to update favorites list
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SqueezeBoxPlayerDiscoveryService.class)
@NonNullByDefault
public class SqueezeBoxPlayerDiscoveryService extends AbstractThingHandlerDiscoveryService<SqueezeBoxServerHandler>
        implements SqueezeBoxPlayerEventListener {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerDiscoveryService.class);

    private static final int TIMEOUT = 60;
    private static final int TTL = 60;

    private @Nullable ScheduledFuture<?> requestPlayerJob;

    /**
     * Discovers SqueezeBox Players attached to a SqueezeBox Server
     */
    public SqueezeBoxPlayerDiscoveryService() {
        super(SqueezeBoxServerHandler.class, SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        setupRequestPlayerJob();
    }

    @Override
    public void initialize() {
        super.initialize();

        // Register the PlayerListener with the SqueezeBoxServerHandler
        thingHandler.registerSqueezeBoxPlayerListener(this);
    }

    @Override
    public void dispose() {
        // Unregister the PlayerListener from the SqueezeBoxServerHandler
        thingHandler.unregisterSqueezeBoxPlayerListener(this);

        cancelRequestPlayerJob();

        super.dispose();
    }

    @Override
    protected void startScan() {
        logger.debug("startScan invoked in SqueezeBoxPlayerDiscoveryParticipant");
        thingHandler.requestPlayers();
        thingHandler.requestFavorites();
    }

    /*
     * Allows request player job to be canceled when server handler is removed
     */
    private void cancelRequestPlayerJob() {
        logger.debug("canceling RequestPlayerJob");
        ScheduledFuture<?> requestPlayerJob = this.requestPlayerJob;
        if (requestPlayerJob != null) {
            requestPlayerJob.cancel(true);
            this.requestPlayerJob = null;
        }
    }

    @Override
    public void playerAdded(SqueezeBoxPlayer player) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();

        ThingUID thingUID = new ThingUID(SQUEEZEBOXPLAYER_THING_TYPE, bridgeUID, player.macAddress.replace(":", ""));

        if (!playerThingExists(thingUID)) {
            logger.debug("Player added {}: {} ", player.macAddress, player.name);

            Map<String, Object> properties = new HashMap<>(1);
            String representationPropertyName = "mac";
            properties.put(representationPropertyName, player.macAddress);

            // Added other properties
            String model = player.model;
            if (model != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, model);
            }
            String name = player.name;
            if (name != null) {
                properties.put(PROPERTY_NAME, name);
            }
            String uuid = player.uuid;
            if (uuid != null) {
                properties.put(PROPERTY_UID, uuid);
            }
            String ipAddr = player.ipAddr;
            if (ipAddr != null) {
                properties.put(PROPERTY_IP, ipAddr);
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(representationPropertyName).withBridge(bridgeUID).withLabel(player.name)
                    .build();

            thingDiscovered(discoveryResult);
        }
    }

    private boolean playerThingExists(ThingUID newThingUID) {
        return thingHandler.getThing().getThing(newThingUID) != null;
    }

    /**
     * Tells the bridge to request a list of players
     */
    private void setupRequestPlayerJob() {
        logger.debug("Request player job scheduled to run every {} seconds", TTL);
        requestPlayerJob = scheduler.scheduleWithFixedDelay(() -> {
            thingHandler.requestPlayers();
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
    public void albumArtistChangeEvent(String mac, String albumArtist) {
    }

    @Override
    public void trackArtistChangeEvent(String mac, String trackArtist) {
    }

    @Override
    public void bandChangeEvent(String mac, String band) {
    }

    @Override
    public void composerChangeEvent(String mac, String composer) {
    }

    @Override
    public void conductorChangeEvent(String mac, String conductor) {
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

    @Override
    public void sourceChangeEvent(String mac, String source) {
    }

    @Override
    public void buttonsChangeEvent(String mac, @Nullable String likeCommand, @Nullable String unlikeCommand) {
    }

    @Override
    public void connectedStateChangeEvent(String mac, boolean connected) {
    }
}
