/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pacomnvr.handler;

import static org.openhab.binding.pacomnvr.PacomNVRBindingConstants.CHANNEL_1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PacomNVRHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author oscar - Initial contribution
 */
public class PacomNVRHandler extends BaseThingHandler {

	private Logger logger = LoggerFactory.getLogger(PacomNVRHandler.class);

	private BigDecimal refresh;

	private BigDecimal camera;

	private ScheduledFuture<?> refreshJob;

	private ChannelUID channelUID;

	public PacomNVRHandler(Thing thing) {
		super(thing);

	}

	@Override
	public void initialize() {
		super.initialize();

		channelUID = new ChannelUID(this.getThing().getUID(), CHANNEL_1);

		final Configuration config = getThing().getConfiguration();
		try {
			this.camera = ((BigDecimal) config.get("camera"));
		} catch (Exception e) {
			camera = new BigDecimal(1);
			logger.error("Failed to read 'camera' config property", e);
		}
		try {
			this.refresh = ((BigDecimal) config.get("refresh"));
		} catch (Exception e) {
			refresh = new BigDecimal(1);
			logger.error("Failed to read 'refresh' config property", e);
		}

		logger.debug("Initialized: channelUID = " + channelUID + " camera = "
				+ camera + ", refresh = " + refresh);

		this.refresh = refresh.multiply(BigDecimal.valueOf(1000));
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		if (channelUID.getId().equals(CHANNEL_1)) {
			if (command instanceof OnOffType) {
				enableVideoStreaming(OnOffType.ON == command);
			}
		}
	}

	private void enableVideoStreaming(boolean enable) {
		if (enable) {
			logger.debug("Video streaming ON");
			final Runnable runnable = new Runnable() {
				public void run() {
					try {
						updateFrame(channelUID);
					} catch (Exception e) {
						logger.error("Failed to udpate frame", e);
					}
				}

			};
			this.refreshJob = this.scheduler.scheduleAtFixedRate(runnable, 0L,
					this.refresh.intValue(), TimeUnit.SECONDS);
		} else {
			logger.debug("Video streaming OFF");
			refreshJob.cancel(true);
		}

	}

	private void updateFrame(ChannelUID channelUID) {
		updateState(channelUID, getStream());
	}

	private State getStream() {
		URLConnection conn = null;
		InputStream in = null;
		try {
			URL url = new URL("http://goo.gl/vQoIQ2");
			conn = url.openConnection();
			in = conn.getInputStream();
			return new RawType(toByteArray(in));
		} catch (Exception e) {
			return UnDefType.UNDEF;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (conn instanceof HttpURLConnection) {
					((HttpURLConnection) conn).disconnect();
				}
			} catch (IOException e) {
			}
		}
	}

	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int n = 0;
		byte[] buffer = new byte[512];
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

	@Override
	public void dispose() {
		this.refreshJob.cancel(true);
	}
}
