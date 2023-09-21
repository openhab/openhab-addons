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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.dto.VerisureSmartPlugsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartPlugsDTO.Smartplug;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Handler for the Smart Plug Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugThingHandler extends VerisureThingHandler<VerisureSmartPlugsDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SMARTPLUG);

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
        VerisureSession session = getSession();
        if (session != null) {
            VerisureSmartPlugsDTO smartPlug = session.getVerisureThing(deviceId, getVerisureThingClass());
            if (smartPlug != null) {
                BigDecimal installationId = smartPlug.getSiteId();
                String url = START_GRAPHQL;
                String operation;
                boolean isOperation;
                if (command == OnOffType.OFF) {
                    operation = "false";
                    isOperation = false;
                } else if (command == OnOffType.ON) {
                    operation = "true";
                    isOperation = true;
                } else {
                    logger.debug("Unknown command! {}", command);
                    return;
                }
                String query = "mutation UpdateState($giid: String!, $deviceLabel: String!, $state: Boolean!) {\n SmartPlugSetState(giid: $giid, input: [{deviceLabel: $deviceLabel, state: $state}])\n}\n";
                ArrayList<SmartPlugDTO> list = new ArrayList<>();
                SmartPlugDTO smartPlugJSON = new SmartPlugDTO();
                VariablesDTO variables = new VariablesDTO();

                variables.setDeviceLabel(deviceId);
                variables.setGiid(installationId.toString());
                variables.setState(isOperation);
                smartPlugJSON.setOperationName("UpdateState");
                smartPlugJSON.setVariables(variables);
                smartPlugJSON.setQuery(query);
                list.add(smartPlugJSON);

                String queryQLSmartPlugSetState = gson.toJson(list);
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

    @Override
    public Class<VerisureSmartPlugsDTO> getVerisureThingClass() {
        return VerisureSmartPlugsDTO.class;
    }

    @Override
    public synchronized void update(VerisureSmartPlugsDTO thing) {
        updateSmartPlugState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateSmartPlugState(VerisureSmartPlugsDTO smartPlugJSON) {
        List<Smartplug> smartPlugList = smartPlugJSON.getData().getInstallation().getSmartplugs();
        if (!smartPlugList.isEmpty()) {
            Smartplug smartplug = smartPlugList.get(0);
            String smartPlugStatus = smartplug.getCurrentState();
            if (smartPlugStatus != null) {
                getThing().getChannels().stream().map(Channel::getUID)
                        .filter(channelUID -> isLinked(channelUID) && !"timestamp".equals(channelUID.getId()))
                        .forEach(channelUID -> {
                            State state = getValue(channelUID.getId(), smartplug, smartPlugStatus);
                            updateState(channelUID, state);
                        });
                updateInstallationChannels(smartPlugJSON);
            }
        } else {
            logger.debug("SmartPlugList is empty!");
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
                return OnOffType.from(smartplug.isHazardous());
        }
        return UnDefType.UNDEF;
    }

    private static class SmartPlugDTO {

        @SuppressWarnings("unused")
        private @Nullable String operationName;
        @SuppressWarnings("unused")
        private VariablesDTO variables = new VariablesDTO();
        @SuppressWarnings("unused")
        private @Nullable String query;

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public void setVariables(VariablesDTO variables) {
            this.variables = variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    private static class VariablesDTO {

        @SuppressWarnings("unused")
        private @Nullable String giid;
        @SuppressWarnings("unused")
        private @Nullable String deviceLabel;
        @SuppressWarnings("unused")
        private boolean state;

        public void setGiid(String giid) {
            this.giid = giid;
        }

        public void setDeviceLabel(String deviceLabel) {
            this.deviceLabel = deviceLabel;
        }

        public void setState(boolean state) {
            this.state = state;
        }
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("SmartPlugThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_SMARTPLUG_TRIGGER_CHANNEL, event);
    }
}
