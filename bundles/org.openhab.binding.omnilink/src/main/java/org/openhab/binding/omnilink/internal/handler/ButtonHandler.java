/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ButtonProperties;

/**
 * The {@link ButtonHandler} defines some methods that are used to
 * interface with an OmniLink Button. This by extension also defines the
 * Button thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class ButtonHandler extends AbstractOmnilinkHandler {
    private final Logger logger = LoggerFactory.getLogger(ButtonHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public ButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateStatus(ThingStatus.ONLINE);
            updateChannels();
            updateButtonProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Button!");
        }
    }

    private void updateButtonProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ButtonProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(bridgeHandler, ObjectPropertyRequests.BUTTONS, thingID, 0).selectNamed()
                        .areaFilter(areaFilter).build();

                for (ButtonProperties buttonProperties : objectPropertyRequest) {
                    Map<String, String> properties = editProperties();
                    properties.put(THING_PROPERTIES_NAME, buttonProperties.getName());
                    properties.put(THING_PROPERTIES_AREA, Integer.toString(areaProperties.getNumber()));
                    updateProperties(properties);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            updateChannels();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_BUTTON_PRESS:
                if (command instanceof OnOffType) {
                    sendOmnilinkCommand(CommandMessage.CMD_BUTTON, 0, thingID);
                    updateChannels();
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Button thing: {}", channelUID);
        }
    }

    public void buttonActivated() {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_BUTTON_ACTIVATED_EVENT);
        triggerChannel(activateChannel);
    }

    public void updateChannels() {
        updateState(CHANNEL_BUTTON_PRESS, OnOffType.OFF);
    }
}
