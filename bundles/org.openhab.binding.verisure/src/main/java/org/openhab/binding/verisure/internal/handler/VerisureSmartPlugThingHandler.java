/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugsJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for the Smart Plug Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_SMARTPLUG);
    }

    public VerisureSmartPlugThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_SET_SMARTPLUG_STATUS)) {
            handleSmartPlugState(command);
            scheduleImmediateRefresh();
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

	private void handleSmartPlugState(Command command) {
		if (session != null && config.deviceId != null) {
			VerisureSmartPlugsJSON smartPlug = (VerisureSmartPlugsJSON) session
					.getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
			if (smartPlug != null) {
				BigDecimal installationId = smartPlug.getSiteId();
				String deviceId = config.deviceId;
				if (deviceId != null) {
					StringBuilder sb = new StringBuilder(deviceId);
					sb.insert(4, " ");
					String url = START_GRAPHQL;
					String queryQLSmartPlugSetState;

					if (command == OnOffType.OFF) {
						queryQLSmartPlugSetState = "[{\"operationName\":\"UpdateState\",\"variables\":{\"giid\":\""
								+ installationId + "\",\"deviceLabel\":\"" + sb.toString()
								+ "\",\"state\":false},\"query\":\"mutation UpdateState($giid: String!, $deviceLabel: String!, $state: Boolean!) {\\n  SmartPlugSetState(giid: $giid, input: [{deviceLabel: $deviceLabel, state: $state}])\\n}\\n\"}]";
						logger.debug("Trying to set SmartPlug state to off with URL {} and data {}", url,
								queryQLSmartPlugSetState);
					} else if (command == OnOffType.ON) {
						queryQLSmartPlugSetState = "[{\"operationName\":\"UpdateState\",\"variables\":{\"giid\":\""
								+ installationId + "\",\"deviceLabel\":\"" + sb.toString()
								+ "\",\"state\":true},\"query\":\"mutation UpdateState($giid: String!, $deviceLabel: String!, $state: Boolean!) {\\n  SmartPlugSetState(giid: $giid, input: [{deviceLabel: $deviceLabel, state: $state}])\\n}\\n\"}]";
						logger.debug("Trying to set SmartPlug state to on with URL {} and data {}", url,
								queryQLSmartPlugSetState);
					} else {
						logger.debug("Unknown command! {}", command);
						return;
					}
					session.sendCommand(START_GRAPHQL, queryQLSmartPlugSetState, installationId);
					ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
					updateState(cuid, new StringType("pending"));
				}
			}
		}
	}

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMARTPLUG)) {
            VerisureSmartPlugsJSON obj = (VerisureSmartPlugsJSON) thing;
            if (obj != null) {
                updateSmartPlugState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

	private void updateSmartPlugState(VerisureSmartPlugsJSON smartPlugJSON) {
		String smartPlugStatus = smartPlugJSON.getData().getInstallation().getSmartplugs().get(0).getCurrentState();
		if (smartPlugStatus != null) {
			ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTPLUG_STATUS);
			updateState(cuid, new StringType(smartPlugStatus));
			cuid = new ChannelUID(getThing().getUID(), CHANNEL_SET_SMARTPLUG_STATUS);
			if ("ON".equals(smartPlugStatus)) {
				updateState(cuid, OnOffType.ON);
			} else if ("OFF".equals(smartPlugStatus)) {
				updateState(cuid, OnOffType.OFF);
			} else if ("PENDING".equals(smartPlugStatus)) {
				// Schedule another refresh.
				logger.debug("Issuing another immediate refresh since statis is stii PENDING ...");
				this.scheduleImmediateRefresh();
			} else {
				logger.warn("Unknown SmartPlug status: {}", smartPlugStatus);
			}
			cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
			updateState(cuid, new StringType(smartPlugJSON.getData().getInstallation().getSmartplugs().get(0).getDevice().getArea()));
			cuid = new ChannelUID(getThing().getUID(), CHANNEL_HAZARDOUS);
			updateState(cuid, new StringType(smartPlugJSON.getData().getInstallation().getSmartplugs().get(0).isHazardous().toString()));
			cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_ID);
			BigDecimal siteId = smartPlugJSON.getSiteId();
			if (siteId != null) {
				updateState(cuid, new DecimalType(siteId.longValue()));
			}
			cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_NAME);
			StringType instName = new StringType(smartPlugJSON.getSiteName());
			updateState(cuid, instName);
		}
	}
}
