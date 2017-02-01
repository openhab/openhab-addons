/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.discovery;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.*;

import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a {@link SqueezeBoxServerHandler} finds a new SqueezeBox Player we will
 * add it to the system.
 * 
 * @author Dan Cunningham
 *
 */
public class SqueezeBoxPlayerDiscoveryParticipant extends AbstractDiscoveryService
		implements SqueezeBoxPlayerEventListener {
	private final Logger logger = LoggerFactory
			.getLogger(SqueezeBoxPlayerDiscoveryParticipant.class);

	private final static int TIMEOUT = 60;
	private final static int TTL = 60;

	private SqueezeBoxServerHandler squeezeBoxServerHandler;
	private ScheduledFuture<?> requestPlayerJob;

	/**
	 * Discovers SqueezeBox Players attached to a SqueezeBox Server
	 * 
	 * @param squeezeBoxServerHandler
	 */
	public SqueezeBoxPlayerDiscoveryParticipant(
			SqueezeBoxServerHandler squeezeBoxServerHandler) {
		super(SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
		this.squeezeBoxServerHandler = squeezeBoxServerHandler;
		setupRequestPlayerJob();
	}

	@Override
	protected void startScan() {
		this.squeezeBoxServerHandler.requestPlayers();
	}

	@Override
	protected void startBackgroundDiscovery() {
		this.squeezeBoxServerHandler.requestPlayers();
	};

	@Override
	protected void deactivate() {
		super.deactivate();
		if (requestPlayerJob != null) {
			requestPlayerJob.cancel(true);
			requestPlayerJob = null;
		}
	}

	@Override
	public void playerAdded(SqueezeBoxPlayer player) {
		logger.debug("Player added {} {} ", player.getMacAddress(),
				player.getName());
		ThingUID bridgeUID = squeezeBoxServerHandler.getThing().getUID();
		ThingUID thingUID = new ThingUID(SQUEEZEBOXPLAYER_THING_TYPE,
				bridgeUID, player.getMacAddress().replace(":", ""));
		Map<String, Object> properties = new HashMap<>(1);
		properties.put("mac", player.getMacAddress());
		DiscoveryResult discoveryResult = DiscoveryResultBuilder
				.create(thingUID).withProperties(properties)
				.withBridge(bridgeUID)
				.withLabel(player.getName()).build();
		thingDiscovered(discoveryResult);
		
	}

	/**
	 * Tells the bridge to request a list of players
	 */
	private void setupRequestPlayerJob() {
		Runnable runnable = new Runnable() {
			public void run() {
				squeezeBoxServerHandler.requestPlayers();
			}
		};
		requestPlayerJob = scheduler.scheduleWithFixedDelay(runnable, 10, TTL,
				TimeUnit.SECONDS);
	}

	// we can ignore the other events
	@Override
	public void powerChangeEvent(String mac, boolean power) {
	}

	@Override
	public void modeChangeEvent(String mac, String mode) {
	}

	@Override
	public void volumeChangeEvent(String mac, int volume) {
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
}
