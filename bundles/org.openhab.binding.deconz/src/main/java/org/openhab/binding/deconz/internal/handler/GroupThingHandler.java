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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deconz.internal.CommandDescriptionProvider;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.GroupAction;
import org.openhab.binding.deconz.internal.dto.GroupMessage;
import org.openhab.binding.deconz.internal.dto.GroupState;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This group thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupThingHandler extends DeconzBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_LIGHTGROUP);
    private final Logger logger = LoggerFactory.getLogger(GroupThingHandler.class);
    private final CommandDescriptionProvider commandDescriptionProvider;

    private Map<String, String> scenes = Map.of();
    private GroupState groupStateCache = new GroupState();

    public GroupThingHandler(Thing thing, Gson gson, CommandDescriptionProvider commandDescriptionProvider) {
        super(thing, gson, ResourceType.GROUPS);
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        GroupAction newGroupAction = new GroupAction();
        switch (channelId) {
            case CHANNEL_ALL_ON:
            case CHANNEL_ANY_ON:
                if (command instanceof RefreshType) {
                    valueUpdated(channelUID.getId(), groupStateCache);
                    return;
                }
                break;
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    newGroupAction.alert = command.toString();
                } else {
                    return;
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    Integer bri = Util.fromPercentType(hsbCommand.getBrightness());
                    newGroupAction.bri = bri;
                    if (bri > 0) {
                        newGroupAction.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
                        newGroupAction.sat = Util.fromPercentType(hsbCommand.getSaturation());
                    }
                } else if (command instanceof PercentType) {
                    newGroupAction.bri = Util.fromPercentType((PercentType) command);
                } else if (command instanceof DecimalType) {
                    newGroupAction.bri = ((DecimalType) command).intValue();
                } else if (command instanceof OnOffType) {
                    newGroupAction.on = OnOffType.ON.equals(command);
                } else {
                    return;
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof DecimalType) {
                    int miredValue = Util.kelvinToMired(((DecimalType) command).intValue());
                    newGroupAction.ct = Util.constrainToRange(miredValue, ZCL_CT_MIN, ZCL_CT_MAX);
                } else {
                    return;
                }
                break;
            case CHANNEL_SCENE:
                if (command instanceof StringType) {
                    String sceneId = scenes.get(command.toString());
                    if (sceneId != null) {
                        sendCommand(null, command, channelUID, "scenes/" + sceneId + "/recall", null);
                    } else {
                        logger.debug("Ignoring command {} for {}, scene is not found in available scenes: {}", command,
                                channelUID, scenes);
                    }
                }
                return;
            default:
                return;
        }

        Integer bri = newGroupAction.bri;
        if (bri != null) {
            newGroupAction.on = (bri > 0);
        }

        sendCommand(newGroupAction, command, channelUID, null);
    }

    @Override
    protected void processStateResponse(DeconzBaseMessage stateResponse) {
        if (stateResponse instanceof GroupMessage) {
            GroupMessage groupMessage = (GroupMessage) stateResponse;
            scenes = groupMessage.scenes.stream().collect(Collectors.toMap(scene -> scene.name, scene -> scene.id));
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_SCENE);
            commandDescriptionProvider.setDescription(channelUID,
                    CommandDescriptionBuilder.create().withCommandOptions(groupMessage.scenes.stream()
                            .map(scene -> new CommandOption(scene.name, scene.name)).collect(Collectors.toList()))
                            .build());

        }
        messageReceived(config.id, stateResponse);
    }

    private void valueUpdated(String channelId, GroupState newState) {
        switch (channelId) {
            case CHANNEL_ALL_ON:
                updateState(channelId, OnOffType.from(newState.all_on));
                break;
            case CHANNEL_ANY_ON:
                updateState(channelId, OnOffType.from(newState.any_on));
                break;
            default:
        }
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        if (message instanceof GroupMessage) {
            GroupMessage groupMessage = (GroupMessage) message;
            logger.trace("{} received {}", thing.getUID(), groupMessage);
            GroupState groupState = groupMessage.state;
            if (groupState != null) {
                updateStatus(ThingStatus.ONLINE);
                thing.getChannels().stream().map(c -> c.getUID().getId()).forEach(c -> valueUpdated(c, groupState));
                groupStateCache = groupState;
            }
        }
    }
}
