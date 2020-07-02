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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO.CommunicationState;

/**
 * Handler for the Gateway thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureGatewayThingHandler extends VerisureThingHandler<VerisureGatewayDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_GATEWAY);

    public VerisureGatewayThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureGatewayDTO> getVerisureThingClass() {
        return VerisureGatewayDTO.class;
    }

    @Override
    public synchronized void update(VerisureGatewayDTO thing) {
        updateGatewayState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateGatewayState(VerisureGatewayDTO gatewayJSON) {
        List<CommunicationState> communicationStateList = gatewayJSON.getData().getInstallation()
                .getCommunicationState();
        if (!communicationStateList.isEmpty()) {
            communicationStateList.forEach(communicationState -> {
                getThing().getChannels().stream().map(Channel::getUID).filter(channelUID -> isLinked(channelUID))
                        .forEach(channelUID -> {
                            if (!channelUID.getId().contains("testTime")) {
                                State state = getValue(channelUID.getId(), gatewayJSON, communicationState);
                                updateState(channelUID, state);
                            } else {
                                String timestamp = communicationState.getTestDate();
                                if (timestamp != null && channelUID.toString()
                                        .contains(communicationState.getHardwareCarrierType())) {
                                    updateTimeStamp(timestamp, channelUID);
                                }
                            }
                        });
            });
            updateInstallationChannels(gatewayJSON);
        } else {
            logger.debug("Empty communication state list.");
        }
    }

    public State getValue(String channelId, VerisureGatewayDTO verisureGateway, CommunicationState communicationState) {
        switch (channelId) {
            case CHANNEL_STATUS_GSM_OVER_UDP:
            case CHANNEL_STATUS_GSM_OVER_SMS:
            case CHANNEL_STATUS_GPRS_OVER_UDP:
            case CHANNEL_STATUS_ETH_OVER_UDP:
                String state = communicationState.getResult();
                return state != null ? new StringType(state) : UnDefType.NULL;
            case CHANNEL_GATEWAY_MODEL:
                String model = communicationState.getDevice().getGui().getLabel();
                return model != null ? new StringType(model) : UnDefType.NULL;
            case CHANNEL_LOCATION:
                String location = communicationState.getDevice().getArea();
                return location != null ? new StringType(location) : UnDefType.NULL;
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("GatewayThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_GATEWAY_TRIGGER_CHANNEL, event);
    }
}
