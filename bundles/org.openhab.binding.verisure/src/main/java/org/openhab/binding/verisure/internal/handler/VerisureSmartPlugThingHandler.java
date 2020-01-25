/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugs;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugs.Smartplug;
import org.openhab.binding.verisure.internal.model.VerisureThing;

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
            VerisureSmartPlugs smartPlug = (VerisureSmartPlugs) session.getVerisureThing(deviceId);
            if (smartPlug != null) {
                BigDecimal installationId = smartPlug.getSiteId();
                if (deviceId != null) {
                    StringBuilder sb = new StringBuilder(deviceId);
                    sb.insert(4, " ");
                    String url = START_GRAPHQL;
                    String operation;
                    if (command == OnOffType.OFF) {
                        operation = "false";
                    } else if (command == OnOffType.ON) {
                        operation = "true";
                    } else {
                        logger.debug("Unknown command! {}", command);
                        return;
                    }
                    String queryQLSmartPlugSetState = "[{\"operationName\":\"UpdateState\",\"variables\":{\"giid\":\""
                            + installationId + "\",\"deviceLabel\":\"" + sb.toString() + "\",\"state\":" + operation
                            + "},\"query\":\"mutation UpdateState($giid: String!, $deviceLabel: String!, $state: Boolean!) {\\n  SmartPlugSetState(giid: $giid, input: [{deviceLabel: $deviceLabel, state: $state}])\\n}\\n\"}]";
                    logger.debug("Trying to set SmartPlug state to {} with URL {} and data {}", operation, url,
                            queryQLSmartPlugSetState);
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
    public synchronized void update(@Nullable VerisureThing thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMARTPLUG)) {
            VerisureSmartPlugs obj = (VerisureSmartPlugs) thing;
            if (obj != null) {
                updateSmartPlugState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateSmartPlugState(VerisureSmartPlugs smartPlugJSON) {
        Smartplug smartplug = smartPlugJSON.getData().getInstallation().getSmartplugs().get(0);
        String smartPlugStatus = smartplug.getCurrentState();
        if (smartPlugStatus != null) {
            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), smartplug, smartPlugStatus);
                        updateState(channelUID, state);
                    });
            super.update(smartPlugJSON);
        }
    }

    public State getValue(String channelId, Smartplug smartplug, String smartPlugStatus) {
        switch (channelId) {
            case CHANNEL_SMARTPLUG_STATUS:
                if ("ON".equals(smartPlugStatus)) {
                    return OnOffType.ON;
                } else if ("OFF".equals(smartPlugStatus)) {
                    return OnOffType.OFF;
                } else if ("PENDING".equals(smartPlugStatus)) {
                    // Schedule another refresh.
                    logger.debug("Issuing another immediate refresh since status is still PENDING ...");
                    this.scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                }
                break;
            case CHANNEL_LOCATION:
                String location = smartplug.getDevice().getArea();
                return location != null ? new StringType(location) : UnDefType.NULL;
            case CHANNEL_HAZARDOUS:
                return smartplug.isHazardous() ? OnOffType.ON : OnOffType.OFF;
        }
        return UnDefType.UNDEF;
    }
}
