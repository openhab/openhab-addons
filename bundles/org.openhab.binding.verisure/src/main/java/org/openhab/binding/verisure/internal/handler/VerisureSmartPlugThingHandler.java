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
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugsJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugsJSON.Smartplug;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for the Smart Plug Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SMARTPLUG);

    private static final int REFRESH_DELAY_SECONDS = 10;

    public VerisureSmartPlugThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_SMARTPLUG_STATUS)) {
            handleSmartPlugState(command);
            scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    private void handleSmartPlugState(Command command) {
        String deviceId = config.getDeviceId();
        if (session != null && deviceId != null) {
            VerisureSmartPlugsJSON smartPlug = (VerisureSmartPlugsJSON) session.getVerisureThing(deviceId);
            if (smartPlug != null) {
                BigDecimal installationId = smartPlug.getSiteId();
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
                    int httpResultCode = session.sendCommand(url, queryQLSmartPlugSetState, installationId);
                    if (httpResultCode == HttpStatus.OK_200) {
                        logger.debug("Smartplug state successfully changed!");
                    } else {
                        logger.warn("Failed to send command, HTTP result code {}", httpResultCode);
                    }
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
        Smartplug smartplug = smartPlugJSON.getData().getInstallation().getSmartplugs().get(0);
        String smartPlugStatus = smartplug.getCurrentState();
        if (smartPlugStatus != null) {
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTPLUG_STATUS);
            if ("ON".equals(smartPlugStatus)) {
                updateState(cuid, OnOffType.ON);
            } else if ("OFF".equals(smartPlugStatus)) {
                updateState(cuid, OnOffType.OFF);
            } else if ("PENDING".equals(smartPlugStatus)) {
                // Schedule another refresh.
                logger.debug("Issuing another immediate refresh since status is still PENDING ...");
                this.scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
            } else {
                logger.warn("Unknown SmartPlug status: {}", smartPlugStatus);
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
            updateState(cuid, new StringType(smartplug.getDevice().getArea()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_HAZARDOUS);
            updateState(cuid, new StringType(smartplug.isHazardous().toString()));
            super.update(smartPlugJSON);
        }
    }
}
