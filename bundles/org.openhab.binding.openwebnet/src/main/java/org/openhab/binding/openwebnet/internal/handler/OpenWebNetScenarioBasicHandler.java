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
package org.openhab.binding.openwebnet.internal.handler;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Scenario.WhatScenario;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetScenarioBasicHandler} is responsible for handling Basic Scenario (WHO=0) messages.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetScenarioBasicHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetScenarioBasicHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.SCENARIO_BASIC_SUPPORTED_THING_TYPES;

    public OpenWebNetScenarioBasicHandler(Thing thing) {
        super(thing);
        logger.debug("created Basic Scenario device for thing: {}", getThing().getUID());
    }

    @Override
    protected String ownIdPrefix() {
        return Who.SCENARIO.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        if (msg.isCommand()) {
            WhatScenario scenario = (WhatScenario) msg.getWhat();
            if (scenario == null) {
                logger.warn("Invalid Basic Scenario: {}. Ignoring message {}", scenario, msg);
                return;
            }
            logger.debug("Basic Scenario {} has been activated", scenario);
            triggerChannel(OpenWebNetBindingConstants.CHANNEL_SCENARIO, scenario.toString());
        } else {
            logger.debug("handleMessage() Ignoring unsupported DIM for thing {}. Frame={}", getThing().getUID(), msg);
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.debug("Basic Scenario are read-only channels. Ignoring command {} for channel {}", command, channel);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() Basic Scenario channels are trigger channels and do not have state.");
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        logger.debug("Basic Scenario channels are trigger channels and do not have state. Setting it ONLINE");
        // put basic scenario things to ONLINE automatically as they do not have state
        ThingStatus ts = getThing().getStatus();
        if (ThingStatus.ONLINE != ts && ThingStatus.REMOVING != ts && ThingStatus.REMOVED != ts) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }
}
