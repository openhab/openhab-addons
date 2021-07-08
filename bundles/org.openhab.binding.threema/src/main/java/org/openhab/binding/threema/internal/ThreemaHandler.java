/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.internal;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.threema.apitool.APIConnector;
import ch.threema.apitool.PublicKeyStore;

/**
 * The {@link ThreemaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai K. - Initial contribution
 */
@NonNullByDefault
public class ThreemaHandler extends BaseThingHandler {

	private final Logger logger = LoggerFactory.getLogger(ThreemaHandler.class);

	private @Nullable ThreemaConfiguration config;
	private @Nullable APIConnector apiConnector;

	public ThreemaHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		// no commands to handle
	}

	@Override
	public void initialize() {
		config = getConfigAs(ThreemaConfiguration.class);

		apiConnector = new APIConnector(config.gatewayId, config.secret, new PublicKeyStore() {
			@Override
			protected byte[] fetchPublicKey(String threemaId) {
				// TODO: implement public key fetch
				// (e.g. fetch from a locally saved file)
				return null;
			}

			@Override
			protected void save(String threemaId, byte[] publicKey) {
				// TODO: implement public key saving
				// (e.g. save to a locally saved file)
			}
		});
		
		updateStatus(ThingStatus.UNKNOWN);

		scheduler.execute(() -> {
			try {
				int credits = apiConnector.lookupCredits();
				if (credits > 0) {
					logger.info("{} Threema.Gateway credits available.", credits);
					updateStatus(ThingStatus.ONLINE);
				} else {
					updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "No more Threema.Gateway credits available.");
				}
			} catch (IOException e) {
				logger.error("Failed to query credits", e);
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
			}
			
		});
	}

	boolean sendTextMessageSimple(String threemaId, String textMessage) {
		try {
			apiConnector.sendTextMessageSimple(threemaId, textMessage);
			return true;
		} catch (IOException e) {
			logger.error("Failed to send text message in basic mode to {}", threemaId, e);
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
			return false;
		}
	}

	boolean sendTextMessageSimple(String message) {
		return Optional.ofNullable(config.recipientIds)
				.map(recipientIds -> recipientIds.parallelStream()
						.map(threemaId -> sendTextMessageSimple(threemaId, message)).allMatch(Boolean.TRUE::equals))
				.orElse(false);
	}
}
